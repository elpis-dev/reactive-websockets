package io.github.elpis.reactive.websockets.handler;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_KEY;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.time.Duration;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptive WebSocket handler that adds flow control on top of BaseWebSocketHandler.
 *
 * <p>Single Responsibility: Apply flow control decorators around streams.
 *
 * <p>Flow control features:
 *
 * <ul>
 *   <li>Rate limiting on inbound messages via @RateLimit
 *   <li>Backpressure strategies on outbound messages via @Backpressure
 *   <li>Heartbeat (ping) messages merged with outbound via @Heartbeat
 * </ul>
 *
 * <p>This class does NOT handle:
 *
 * <ul>
 *   <li>Session lifecycle - handled by BaseWebSocketHandler
 *   <li>Type conversion beyond what BaseWebSocketHandler provides
 *   <li>Exception handling - annotation processor generates this
 * </ul>
 *
 * <p>Generated handlers from @MessageEndpoint extend this class to get flow control based on their
 * annotation configuration.
 *
 * @since 1.0.0
 */
public abstract class AdaptiveReactiveWebSocketHandler extends BaseReactiveWebSocketHandler {
  private static final Logger log = LoggerFactory.getLogger(AdaptiveReactiveWebSocketHandler.class);

  private final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry;
  private final ReactiveFlowControlChain reactiveFlowControlChain;

  /**
   * Creates an AdaptiveWebSocketHandler with flow control configuration.
   *
   * <p>If rate limiting is enabled, registers a rate limiter for this endpoint's path.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   * @param pathTemplate the WebSocket path template (e.g., "/chat/{room}")
   */
  protected AdaptiveReactiveWebSocketHandler(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final String pathTemplate,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain) {

    super(eventManagerFactory, sessionRegistry, jsonMapper, pathTemplate);
    this.reactiveHeartbeatFlowControlRegistry = reactiveHeartbeatFlowControlRegistry;
    this.reactiveFlowControlChain = reactiveFlowControlChain;
  }

  /**
   * Builds the WebSocket message processing chain with flow control applied.
   *
   * <p>Extends the base implementation by:
   *
   * <ol>
   *   <li>Applying rate limiting to inbound messages (if enabled)
   *   <li>Applying backpressure strategy to outbound messages (if enabled)
   *   <li>Merging heartbeat pings with outbound messages (if enabled)
   * </ol>
   *
   * @param session the WebSocket session
   * @param webSocketSessionContext the session context with path/query params
   * @return Mono&lt;Void&gt; that completes when session closes
   */
  @Override
  protected Mono<Void> buildChain(
      final WebSocketSession session,
      final WebSocketSessionContext webSocketSessionContext,
      final SessionStreams streams) {
    final String sessionId = webSocketSessionContext.sessionId();
    final String path = this.getPathTemplate();

    if (log.isDebugEnabled()) {
      log.debug("Registered session {} for path {}", sessionId, path);
    }

    final Mono<Void> input =
        session
            .receive()
            .filter(
                message ->
                    message.getType() == WebSocketMessage.Type.TEXT
                        || message.getType() == WebSocketMessage.Type.BINARY)
            .doOnNext(message -> streams.inboundSink().tryEmitNext(message))
            // TODO: Check if this is enough
            .doOnError(
                e -> {
                  if (log.isErrorEnabled()) {
                    log.error("Inbound error for session {}: {}", sessionId, e.getMessage());
                  }
                })
            .then();

    final Optional<HeartbeatConfig> heartbeatConfig =
        Optional.ofNullable(this.reactiveHeartbeatFlowControlRegistry.get(path))
            .or(
                () ->
                    Optional.ofNullable(this.reactiveHeartbeatFlowControlRegistry.get(DEFAULT_KEY)))
            .filter(HeartbeatConfig::isEnabled);

    final Flux<WebSocketMessage> outboundMessages =
        this.mapOutput(session, streams.outboundFlux())
            .onErrorResume(
                ErrorResponseException.class,
                e -> {
                  // TODO: Consider a better log message
                  if (log.isDebugEnabled()) {
                    log.debug("Sending error response to session {}", sessionId);
                  }

                  return this.mapOutput(session, Flux.just(e.getPayload()));
                })
            .transform(
                origin ->
                    this.reactiveFlowControlChain.getOutputPolicies().stream()
                        .reduce(
                            origin,
                            (stream, policy) -> policy.apply(path, webSocketSessionContext, stream),
                            (flux, __) -> flux))
            .transform(
                origin ->
                    heartbeatConfig
                        .map(config -> this.applyHeartbeat(config, session, origin))
                        .orElse(origin));

    final Mono<Void> output =
        session
            .send(outboundMessages)
            .doOnError(
                e -> {
                  if (log.isErrorEnabled()) {
                    log.error("Outbound error for session {}: {}", sessionId, e.getMessage());
                  }
                });

    final Flux<WebSocketMessage> inboundStream =
        streams
            .inboundFlux()
            .transform(
                origin ->
                    this.reactiveFlowControlChain.getInputPolicies().stream()
                        .reduce(
                            origin,
                            (stream, policy) -> policy.apply(path, webSocketSessionContext, stream),
                            (flux, __) -> flux));

    final Publisher<?> processing =
        this.getProcessingPublisher(
            webSocketSessionContext, inboundStream, streams.outboundSink(), session);

    return Mono.when(input, output, processing)
        .doFinally(
            signal -> {
              if (log.isTraceEnabled()) {
                log.trace("Session {} terminating with signal: {}", sessionId, signal);
              }

              getSessionRegistry().unregister(path, sessionId);
              streams.close();
            });
  }

  /**
   * Merges heartbeat ping messages with the outbound stream.
   *
   * @param config the heartbeat configuration
   * @param session the WebSocket session
   * @param outbound the outbound message stream
   * @return the merged stream with heartbeat pings
   */
  private Flux<WebSocketMessage> applyHeartbeat(
      final HeartbeatConfig config,
      final WebSocketSession session,
      final Flux<WebSocketMessage> outbound) {

    final Flux<WebSocketMessage> serverPings =
        Flux.interval(Duration.ofSeconds(config.getInterval()))
            .map(
                tick ->
                    session.pingMessage(factory -> session.bufferFactory().allocateBuffer(256)));

    return Flux.merge(outbound, serverPings);
  }
}
