package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.handler.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
 * @since 1.1.0
 */
public abstract class AdaptiveWebSocketHandler extends BaseWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(AdaptiveWebSocketHandler.class);

  private final HeartbeatConfig heartbeatConfig;
  private final RateLimitConfig rateLimitConfig;
  private final BackpressureConfig backpressureConfig;
  private final RateLimiterService rateLimiterService;

  /**
   * Creates an AdaptiveWebSocketHandler with flow control configuration.
   *
   * <p>If rate limiting is enabled, registers a rate limiter for this endpoint's path.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   * @param pathTemplate the WebSocket path template (e.g., "/chat/{room}")
   * @param heartbeatConfig heartbeat (ping) configuration
   * @param rateLimitConfig rate limiting configuration
   * @param backpressureConfig backpressure strategy configuration
   * @param rateLimiterService service for creating rate limiters
   */
  protected AdaptiveWebSocketHandler(
      final WebSocketEventManagerFactory eventManagerFactory,
      final WebSocketSessionRegistry sessionRegistry,
      final String pathTemplate,
      final HeartbeatConfig heartbeatConfig,
      final RateLimitConfig rateLimitConfig,
      final BackpressureConfig backpressureConfig,
      final RateLimiterService rateLimiterService) {

    super(eventManagerFactory, sessionRegistry, pathTemplate);
    this.heartbeatConfig = heartbeatConfig;
    this.rateLimitConfig = rateLimitConfig;
    this.backpressureConfig = backpressureConfig;
    this.rateLimiterService = rateLimiterService;

    // Register rate limiter during bean construction if enabled
    if (rateLimitConfig.isEnabled() && rateLimiterService != null) {
      rateLimiterService.register(
          pathTemplate,
          rateLimitConfig.getLimitForPeriod(),
          rateLimitConfig.getLimitRefreshPeriod(),
          TimeUnit.valueOf(rateLimitConfig.getTimeUnit()),
          rateLimitConfig.getTimeout());
      log.debug("Registered rate limiter for path: {}", pathTemplate);
    }
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
    getSessionRegistry().registerSession(path, sessionId, streams);

    log.debug("Registered session {} for path {}", sessionId, path);

    final Mono<Void> input =
        session
            .receive()
            .filter(
                msg ->
                    msg.getType() == WebSocketMessage.Type.TEXT
                        || msg.getType() == WebSocketMessage.Type.BINARY)
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

    outboundMessages = applyBackpressure(outboundMessages);

    if (isHeartbeatEnabled()) {
      outboundMessages = applyHeartbeat(session, outboundMessages);
    }

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
              getSessionRegistry().unregisterSession(path, sessionId);
              streams.close();
            });
  }

  /**
   * Gets the rate limiter for this endpoint and the given identifier.
   *
   * <p>This method is used internally by applyRateLimit to retrieve the rate limiter.
   *
   * @param identifier the rate limit identifier (session ID, user ID, IP, or "default")
   * @return Optional containing the rate limiter if enabled and registered, empty otherwise
   */
  protected Optional<RateLimiter> getRateLimiter(final String identifier) {
    if (!rateLimitConfig.isEnabled() || rateLimiterService == null) {
      return Optional.empty();
    }
    final RateLimiter rateLimiter = rateLimiterService.get(getPathTemplate(), identifier);
    return Optional.ofNullable(rateLimiter);
  }

  /**
   * Resolves the rate limit identifier based on the configured scope.
   *
   * <p>This method is used internally by applyRateLimit to determine the rate limit key.
   *
   * @param context the session context
   * @return the identifier for rate limiting based on scope
   */
  protected String resolveRateLimitIdentifier(final WebSocketSessionContext context) {
    final String scope = rateLimitConfig.getScope();

    return switch (scope) {
      case "USER" -> {
        final Object principal = context.getPrincipal("", false, Object.class);
        yield principal != null ? principal.toString() : context.getSessionId();
      }
      case "IP" -> {
        final String remoteAddress = context.getRemoteAddress();
        yield remoteAddress != null ? remoteAddress : context.getSessionId();
      }
      default -> context.getSessionId(); // SESSION scope or fallback
    };
  }

  /**
   * Applies the configured rate limiting strategy to the given Flux.
   *
   * @param flux the Flux to apply rate limit to
   * @param <T> the type of elements in the Flux
   * @return the Flux with rate limit applied, or the original Flux if disabled
   */
  protected <T> Flux<T> applyRateLimit(final Flux<T> flux, final WebSocketSessionContext context) {
    if (!rateLimitConfig.isEnabled()) {
      return flux;
    }

    final String identifier = this.resolveRateLimitIdentifier(context);
    return this.getRateLimiter(identifier)
        .map(
            limiter ->
                flux.flatMap(
                    msg -> {
                      final boolean permitted = limiter.acquirePermission();
                      if (permitted) {
                        return Mono.just(msg);
                      } else {
                        log.warn("Rate limit exceeded for identifier: {}", identifier);
                        return Mono.empty();
                      }
                    }))
        .orElse(flux);
  }

  /**
   * Applies the configured backpressure strategy to the given Flux.
   *
   * @param flux the Flux to apply backpressure to
   * @param <T> the type of elements in the Flux
   * @return the Flux with backpressure applied, or the original Flux if disabled
   */
  protected <T> Flux<T> applyBackpressure(final Flux<T> flux) {
    if (!backpressureConfig.isEnabled()) {
      return flux;
    }

    final String strategy = backpressureConfig.getStrategy();
    final int bufferSize = backpressureConfig.getBufferSize();

    return switch (strategy) {
      case "BUFFER" -> flux.onBackpressureBuffer(bufferSize);
      case "DROP_OLDEST" -> flux.onBackpressureLatest();
      case "DROP_LATEST" -> flux.onBackpressureDrop();
      case "ERROR" -> flux.onBackpressureError();
      default -> {
        log.warn("Unknown backpressure strategy: {}. No backpressure applied.", strategy);
        yield flux;
      }
    };
  }

  /**
   * Merges heartbeat ping messages with the outbound stream.
   *
   * @param session the WebSocket session
   * @param outbound the outbound message stream
   * @return the merged stream with heartbeat pings
   */
  protected Flux<WebSocketMessage> applyHeartbeat(
      final WebSocketSession session, final Flux<WebSocketMessage> outbound) {

    final Flux<WebSocketMessage> serverPings =
        Flux.interval(Duration.ofSeconds(getHeartbeatInterval()))
            .map(
                tick ->
                    session.pingMessage(factory -> session.bufferFactory().allocateBuffer(256)));

    return Flux.merge(outbound, serverPings);
  }

  /** Checks if heartbeat is enabled. */
  protected boolean isHeartbeatEnabled() {
    return heartbeatConfig.isEnabled();
  }

  /** Gets the heartbeat interval in seconds. */
  protected long getHeartbeatInterval() {
    return heartbeatConfig.getInterval();
  }

  /** Gets the heartbeat timeout in seconds. */
  protected long getHeartbeatTimeout() {
    return heartbeatConfig.getTimeout();
  }

  /** Checks if rate limiting is enabled. */
  protected boolean isRateLimitEnabled() {
    return rateLimitConfig.isEnabled();
  }

  /** Gets the rate limit for the period. */
  protected int getRateLimitForPeriod() {
    return rateLimitConfig.getLimitForPeriod();
  }

  /** Gets the rate limit refresh period. */
  protected long getRateLimitRefreshPeriod() {
    return rateLimitConfig.getLimitRefreshPeriod();
  }

  /** Gets the rate limit time unit. */
  protected String getRateLimitTimeUnit() {
    return rateLimitConfig.getTimeUnit();
  }

  /** Gets the rate limit timeout. */
  protected long getRateLimitTimeout() {
    return rateLimitConfig.getTimeout();
  }

  /** Gets the rate limit scope (SESSION, USER, IP). */
  protected String getRateLimitScope() {
    return rateLimitConfig.getScope();
  }

  /** Gets the heartbeat configuration. */
  protected HeartbeatConfig getHeartbeatConfig() {
    return heartbeatConfig;
  }

  /** Gets the rate limit configuration. */
  protected RateLimitConfig getRateLimitConfig() {
    return rateLimitConfig;
  }

  /** Gets the backpressure configuration. */
  protected BackpressureConfig getBackpressureConfig() {
    return backpressureConfig;
  }

  /** Checks if backpressure is enabled. */
  protected boolean isBackpressureEnabled() {
    return backpressureConfig.isEnabled();
  }
}
