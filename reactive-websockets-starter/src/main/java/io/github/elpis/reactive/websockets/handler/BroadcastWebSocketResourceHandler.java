package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.exception.RateLimitExceededException;
import io.github.elpis.reactive.websockets.handler.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.handler.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public abstract class BroadcastWebSocketResourceHandler extends BaseWebSocketHandler {
  private static final Logger log =
      LoggerFactory.getLogger(BroadcastWebSocketResourceHandler.class);

  private final Sinks.Many<WebSocketMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

  private final Sinks.Many<WebSocketMessage> pongMessages =
      Sinks.many().multicast().onBackpressureBuffer();

  private final RateLimiterService rateLimiterService;

  protected BroadcastWebSocketResourceHandler(
      final WebSocketEventManagerFactory eventManagerFactory,
      final WebSocketSessionRegistry webSocketSessionRegistry,
      final RateLimiterService rateLimiterService,
      final String pathTemplate,
      final HeartbeatConfig heartbeatConfig,
      final RateLimitConfig rateLimitConfig,
      final BackpressureConfig backpressureConfig) {

    super(
        eventManagerFactory,
        webSocketSessionRegistry,
        pathTemplate,
        heartbeatConfig,
        rateLimitConfig,
        backpressureConfig);
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  protected Flux<Void> buildChain(
      WebSocketSession session, WebSocketSessionContext webSocketSessionContext) {
    final Flux<WebSocketMessage> socketMessageFlux = this.sink.asFlux().share();
    final Flux<WebSocketMessage> messages =
        this.getMessages(session, webSocketSessionContext, socketMessageFlux);

    Flux<WebSocketMessage> incomingMessages =
        session
            .receive()
            .filter(
                webSocketMessage ->
                    webSocketMessage.getType() != WebSocketMessage.Type.PING
                        && webSocketMessage.getType() != WebSocketMessage.Type.PONG);

    incomingMessages = applyRateLimiting(session, webSocketSessionContext, incomingMessages);

    final Mono<Void> input = incomingMessages.doOnNext(sink::tryEmitNext).then();

    return messages != null ? Flux.merge(input, session.send(messages)) : input.flux();
  }

  private Flux<WebSocketMessage> applyRateLimiting(
      final WebSocketSession session,
      final WebSocketSessionContext webSocketSessionContext,
      final Flux<WebSocketMessage> incomingMessages) {

    if (!this.isRateLimitEnabled()) {
      return incomingMessages;
    }

    final String rateLimiterId = this.getRateLimiterIdentifier(session, webSocketSessionContext);
    final RateLimiter rateLimiter =
        rateLimiterService.getRateLimiter(
            this.getPathTemplate(),
            this.getRateLimitForPeriod(),
            this.getRateLimitRefreshPeriod(),
            TimeUnit.valueOf(this.getRateLimitTimeUnit()),
            this.getRateLimitTimeout(),
            rateLimiterId);

    log.debug(
        "Rate limiting enabled for path: {}, identifier: {}, limit: {}/{} {}",
        this.getPathTemplate(),
        rateLimiterId,
        this.getRateLimitForPeriod(),
        this.getRateLimitRefreshPeriod(),
        this.getRateLimitTimeUnit());

    return incomingMessages.flatMap(
        msg ->
            Mono.fromCallable(() -> msg)
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorMap(
                    RequestNotPermitted.class,
                    ex ->
                        new RateLimitExceededException(
                            String.format(
                                "Rate limit exceeded for %s. Maximum %d requests per %d %s allowed.",
                                rateLimiterId,
                                this.getRateLimitForPeriod(),
                                this.getRateLimitRefreshPeriod(),
                                this.getRateLimitTimeUnit().toLowerCase())))
                .doOnError(
                    RateLimitExceededException.class,
                    ex -> log.warn("Rate limit exceeded: {}", ex.getMessage()))
                .onErrorResume(RateLimitExceededException.class, ex -> Mono.empty()));
  }

  private Flux<WebSocketMessage> getMessages(
      final WebSocketSession session,
      final WebSocketSessionContext webSocketSessionContext,
      final Flux<WebSocketMessage> socketMessageFlux) {

    final Publisher<?> publisher =
        this.apply(
            webSocketSessionContext,
            socketMessageFlux.filter(
                webSocketMessage ->
                    webSocketMessage.getType() == WebSocketMessage.Type.TEXT
                        || webSocketMessage.getType() == WebSocketMessage.Type.BINARY));

    final Flux<WebSocketMessage> serverPings =
        Flux.interval(Duration.ofSeconds(this.getHeartbeatInterval()))
            .map(
                aLong ->
                    session.pingMessage(
                        dataBufferFactory -> session.bufferFactory().allocateBuffer(256)));
    final Flux<WebSocketMessage> pongs = this.pongMessages.asFlux();
    if (publisher != null) {
      Flux<WebSocketMessage> messages = this.mapOutput(session, publisher);

      // Apply backpressure strategy to outgoing messages
      messages = this.applyBackpressure(messages);

      return this.isHeartbeatEnabled() ? Flux.merge(messages, serverPings, pongs) : messages;
    } else {
      this.run(webSocketSessionContext, socketMessageFlux);
    }

    return this.isHeartbeatEnabled() ? Flux.merge(serverPings, pongs) : null;
  }

  /**
   * Determines the rate limiter identifier based on the configured scope.
   *
   * @param session the WebSocket session
   * @param context the WebSocket session context
   * @return the identifier for rate limiting
   */
  private String getRateLimiterIdentifier(
      final WebSocketSession session, final WebSocketSessionContext context) {
    final RateLimit.RateLimitScope scope =
        RateLimit.RateLimitScope.valueOf(this.getRateLimitScope());

    return switch (scope) {
      case SESSION -> context.getSessionId();
      case USER -> {
        final String principalName =
            context.getAuthentication() != null
                    && !(context.getAuthentication() instanceof Anonymous)
                ? context.getAuthentication().getName()
                : "anonymous";
        yield this.getPathTemplate() + ":" + principalName;
      }
      case IP -> {
        final String ipAddress =
            session.getHandshakeInfo().getRemoteAddress() != null
                ? session.getHandshakeInfo().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        yield this.getPathTemplate() + ":" + ipAddress;
      }
      case INHERIT ->
          throw new IllegalStateException(
              "INHERIT scope should be resolved during annotation processing and never reach runtime");
    };
  }
}
