package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.config.CloseInitiator;
import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.ServerSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import io.github.elpis.reactive.websockets.handler.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.security.Principal;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base WebSocket handler providing raw session management.
 *
 * <p>Single Responsibility: Manage WebSocket session lifecycle and stream wiring.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Create WebSocketSessionContext from handshake info
 *   <li>Create SessionStreams and register in Registry
 *   <li>Wire WebSocket receive → inboundSink
 *   <li>Wire outboundSink → mapOutput() → WebSocket send
 *   <li>Fire lifecycle events (connect, close)
 *   <li>Handle ErrorResponseException from outboundSink
 * </ul>
 *
 * <p>This class does NOT handle:
 *
 * <ul>
 *   <li>Flow control (heartbeat, rate limiting, backpressure) - see AdaptiveWebSocketHandler
 *   <li>Type conversion beyond mapOutput() - annotation processor generates this
 *   <li>Exception handling - annotation processor generates this
 * </ul>
 *
 * @since 1.0.0
 */
public abstract class BaseWebSocketHandler implements WebSocketHandler {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final WebSocketEventManagerFactory eventManagerFactory;
  private final WebSocketSessionRegistry sessionRegistry;
  private final String pathTemplate;

  /**
   * Creates a BaseWebSocketHandler with minimal dependencies.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   * @param pathTemplate the WebSocket path template (e.g., "/chat/{room}")
   */
  protected BaseWebSocketHandler(
      final WebSocketEventManagerFactory eventManagerFactory,
      final WebSocketSessionRegistry sessionRegistry,
      final String pathTemplate) {

    this.eventManagerFactory = eventManagerFactory;
    this.sessionRegistry = sessionRegistry;
    this.pathTemplate = pathTemplate;
  }

  @Override
  public Mono<Void> handle(final WebSocketSession session) {
    final WebSocketEventManager<ClientSessionClosedEvent> closedEventManager =
        this.eventManagerFactory.getEventManager(ClientSessionClosedEvent.class);
    final WebSocketEventManager<ServerSessionClosedEvent> serverSessionClosedEventManager =
        this.eventManagerFactory.getEventManager(ServerSessionClosedEvent.class);
    final WebSocketEventManager<SessionConnectedEvent> sessionConnectedEventManager =
        this.eventManagerFactory.getEventManager(SessionConnectedEvent.class);

    return Mono.deferContextual(Mono::just)
        .flatMap(contextView -> (Mono<String>) contextView.get("sessionId"))
        .flatMap(
            sessionId -> {
              final HandshakeInfo handshakeInfo = session.getHandshakeInfo();

              log.trace(
                  "Establishing WebSocketSession: id => {}, uri => {}, address => {}",
                  sessionId,
                  handshakeInfo.getUri(),
                  handshakeInfo.getRemoteAddress());

              final ReactiveWebSocketSession reactiveWebSocketSession =
                  ReactiveWebSocketSession.builder()
                      .isOpen(session::isOpen)
                      .sessionId(sessionId)
                      .onClose(
                          (eventSessionId, closeStatus) ->
                              Mono.just(this.getServerClosedEvent(eventSessionId, closeStatus))
                                  .doOnNext(serverSessionClosedEventManager::fire)
                                  .then(Mono.empty()))
                      .build();

              sessionConnectedEventManager.fire(
                  SessionConnectedEvent.builder()
                      .webSocketSessionInfo(reactiveWebSocketSession)
                      .build());

              final Mono<Void> clientClosedSessionListener =
                  session
                      .closeStatus()
                      .doOnNext(
                          closeStatus -> {
                            final SessionCloseInfo sessionCloseInfo =
                                SessionCloseInfo.builder()
                                    .closeStatus(closeStatus)
                                    .session(reactiveWebSocketSession)
                                    .build();
                            final ClientSessionClosedEvent event =
                                new ClientSessionClosedEvent(sessionCloseInfo);

                            closedEventManager.fire(event);
                          })
                      .then();

              final Flux<Void> serverClosedSessionListener =
                  serverSessionClosedEventManager
                      .asFlux()
                      .map(ServerSessionClosedEvent::payload)
                      .filter(
                          sessionCloseInfo ->
                              sessionCloseInfo.getSession().getSessionId().equals(sessionId))
                      .flatMap(
                          sessionCloseInfo -> session.close(sessionCloseInfo.getCloseStatus()));

              return session
                  .getHandshakeInfo()
                  .getPrincipal()
                  .switchIfEmpty(Mono.just(new Anonymous()))
                  .flatMap(
                      principal -> {
                        final WebSocketSessionContext webSocketSessionContext =
                            this.getSessionContext(
                                pathTemplate, sessionId, handshakeInfo, principal);
                        final Mono<Void> chain = this.buildChain(session, webSocketSessionContext);

                        return Mono.when(
                            chain, clientClosedSessionListener, serverClosedSessionListener.then());
                      })
                  .doOnError(throwable -> log.error(throwable.getMessage()));
            });
  }

  private ServerSessionClosedEvent getServerClosedEvent(
      final String sessionId, final CloseStatus closeStatus) {
    final ReactiveWebSocketSession webSocketSession =
        ReactiveWebSocketSession.builder().sessionId(sessionId).build();

    final SessionCloseInfo sessionCloseInfo =
        SessionCloseInfo.builder()
            .closeStatus(closeStatus)
            .session(webSocketSession)
            .closeInitiator(CloseInitiator.SERVER)
            .build();

    return new ServerSessionClosedEvent(sessionCloseInfo);
  }

  /**
   * Converts output objects to WebSocketMessages.
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>WebSocketMessage - passed through unchanged
   *   <li>byte[] - converted to binary message
   *   <li>InputStream - streamed as binary message
   *   <li>Any other object - serialized to JSON text message
   * </ul>
   *
   * @param session the WebSocket session for message creation
   * @param publisher the publisher of objects to convert
   * @return Flux of WebSocketMessages
   */
  protected Flux<WebSocketMessage> mapOutput(
      final WebSocketSession session, final Publisher<?> publisher) {
    return Flux.from(publisher)
        .flatMap(
            any -> {
              if (any instanceof WebSocketMessage webSocketMessage) {
                return Flux.just(webSocketMessage);
              } else if (any instanceof byte[] binary) {
                return Mono.just(session.binaryMessage(factory -> factory.wrap(binary)));
              } else if (InputStream.class.isAssignableFrom(any.getClass())) {
                return DataBufferUtils.readByteChannel(
                        () -> Channels.newChannel((InputStream) any),
                        DefaultDataBufferFactory.sharedInstance,
                        4096)
                    .map(dataBuffer -> session.binaryMessage(factory -> dataBuffer));
              }

              return JsonMapper.applyWithFlux(any).map(session::textMessage);
            });
  }

  /**
   * Builds the WebSocket message processing chain.
   *
   * <p>This method follows the Subscribe Safety Guide:
   *
   * <ol>
   *   <li>Creates SessionStreams and registers in the registry
   *   <li>Wires WebSocket receive → inboundSink
   *   <li>Wires outboundSink → mapOutput() → WebSocket send
   *   <li>Handles ErrorResponseException from outboundSink
   *   <li>Calls processMessages() which returns Mono&lt;Void&gt;
   *   <li>Coordinates all chains with Mono.when() for proper lifecycle
   *   <li>Cleans up on close via doFinally()
   * </ol>
   *
   * <p><b>CRITICAL:</b> This returns Mono&lt;Void&gt; and lets the framework subscribe. Never call
   * .subscribe() manually in handlers!
   *
   * <p>Subclasses (like AdaptiveWebSocketHandler) can override this to add flow control.
   *
   * @param session the WebSocket session
   * @param webSocketSessionContext the session context with path/query params
   * @return Mono&lt;Void&gt; that completes when session closes
   */
  protected Mono<Void> buildChain(
      final WebSocketSession session, final WebSocketSessionContext webSocketSessionContext) {

    final String sessionId = webSocketSessionContext.getSessionId();
    final String path = this.getPathTemplate();

    final ReactiveWebSocketSession reactiveSession =
        ReactiveWebSocketSession.builder()
            .sessionId(sessionId)
            .isOpen(session::isOpen)
            .onClose((id, status) -> session.close(status))
            .build();

    final SessionStreams streams = SessionStreams.create(reactiveSession);
    sessionRegistry.registerSession(path, sessionId, streams);

    log.debug("Registered session {} for path {}", sessionId, path);

    Flux<WebSocketMessage> incomingMessages =
        session
            .receive()
            .filter(
                msg ->
                    msg.getType() != WebSocketMessage.Type.PING
                        && msg.getType() != WebSocketMessage.Type.PONG);

    final Mono<Void> input =
        incomingMessages
            .doOnNext(msg -> streams.inboundSink().tryEmitNext(msg))
            .doOnError(
                e -> log.error("Inbound error for session {}: {}", sessionId, e.getMessage()))
            .then();

    Flux<WebSocketMessage> outboundMessages =
        mapOutput(session, streams.outboundFlux())
            .onErrorResume(
                ErrorResponseException.class,
                e -> {
                  log.debug("Sending error response to session {}", sessionId);
                  return mapOutput(session, Flux.just(e.getPayload()));
                });

    final Mono<Void> output =
        session
            .send(outboundMessages)
            .doOnError(
                e -> log.error("Outbound error for session {}: {}", sessionId, e.getMessage()));

    final Publisher<?> processing =
        Flux.from(processMessages(webSocketSessionContext, streams))
            .doOnError(
                e -> log.error("Processing error for session {}: {}", sessionId, e.getMessage()));

    return Mono.when(input, output, processing)
        .doFinally(
            signal -> {
              log.debug("Session {} terminating with signal: {}", sessionId, signal);
              sessionRegistry.unregisterSession(path, sessionId);
              streams.close();
            });
  }

  /**
   * Process messages using the session streams.
   *
   * <p><b>CRITICAL:</b> This method MUST return Mono&lt;Void&gt; for proper lifecycle management.
   * Never call .subscribe() inside this method!
   *
   * <p>Subclasses override this to implement their message handling logic:
   *
   * <ul>
   *   <li>Get messages from streams.inboundFlux()
   *   <li>Process through user handlers
   *   <li>Send via streams.outboundSink() or ReactiveWebSocketTemplate
   *   <li>Return Mono&lt;Void&gt; from the reactive chain
   * </ul>
   *
   * <p>Example:
   *
   * <pre>{@code
   * protected Mono<Void> processMessages(context, streams) {
   *   return streams.inboundFlux()
   *       .map(this::deserialize)
   *       .transform(messages -> userHandler.handleMessages(messages))
   *       .doOnNext(result -> streams.outboundSink().tryEmitNext(result))
   *       .onErrorResume(e -> {
   *           log.error("Error: {}", e.getMessage());
   *           return Mono.empty();
   *       })
   *       .then();
   * }
   * }</pre>
   *
   * @param context the WebSocket session context
   * @param streams the session streams with inbound/outbound Sinks
   * @return Mono&lt;Void&gt; that completes when processing chain completes
   */
  protected abstract Publisher<?> processMessages(
      final WebSocketSessionContext context, final SessionStreams streams);

  private WebSocketSessionContext getSessionContext(
      final String pathTemplate,
      final String sessionId,
      final HandshakeInfo handshakeInfo,
      final Principal principal) {

    final String uriPath = handshakeInfo.getUri().getPath();
    final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

    final var pathParameters = uriTemplate.match(uriPath);
    final var queryParameters =
        UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build().getQueryParams();
    final var headers = handshakeInfo.getHeaders();

    final String remoteAddress =
        handshakeInfo.getRemoteAddress() != null
            ? handshakeInfo.getRemoteAddress().getAddress().getHostAddress()
            : null;

    return WebSocketSessionContext.builder()
        .authentication(principal)
        .pathParameters(pathParameters)
        .queryParameters(queryParameters)
        .headers(headers)
        .sessionId(sessionId)
        .remoteAddress(remoteAddress)
        .build();
  }

  /**
   * Gets the WebSocket path template.
   *
   * @return the path template (e.g., "/chat/{room}")
   */
  public String getPathTemplate() {
    return pathTemplate;
  }

  /**
   * Gets the session registry.
   *
   * @return the WebSocketSessionRegistry
   */
  protected WebSocketSessionRegistry getSessionRegistry() {
    return sessionRegistry;
  }
}
