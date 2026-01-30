package io.github.elpis.reactive.websockets.context.resource.flowcontrol;

import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@MessageEndpoint("/ratelimit")
@RateLimit(
    limitForPeriod = 5,
    limitRefreshPeriod = 10,
    timeUnit = TimeUnit.SECONDS,
    timeoutDuration = 1000L)
public class RateLimitResource {
  private static final Logger log = LoggerFactory.getLogger(RateLimitResource.class);

  /** Endpoint that inherits rate limit from class level. Allows 5 messages per 10 seconds. */
  @OnMessage(value = "/default")
  public Flux<String> defaultRateLimit(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    return webSocketMessageFlux
        .doOnNext(msg -> log.info("Received default limited message: {}", msg.getPayloadAsText()))
        .map(__ -> "Message Processed");
  }

  /**
   * Endpoint with custom rate limit that overrides class level. Allows 5 messages per 10 seconds.
   */
  @OnMessage(value = "/custom")
  @RateLimit(
      limitForPeriod = 5,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.SESSION)
  public Flux<String> customRateLimit(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    return webSocketMessageFlux
        .doOnNext(msg -> log.info("Received custom message: {}", msg.getPayloadAsText()))
        .map(__ -> "Message Processed");
  }

  /** Endpoint with rate limiting disabled. */
  @OnMessage(value = "/disabled")
  @RateLimit(enabled = false, scope = RateLimit.RateLimitScope.INHERIT)
  public Flux<String> disabledRateLimit(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    return webSocketMessageFlux
        .doOnNext(msg -> log.info("Received no rate-limited message: {}", msg.getPayloadAsText()))
        .map(__ -> "Message Processed");
  }

  /** Endpoint with rate limit by USER scope. Allows 3 messages per 10 seconds. */
  @OnMessage(value = "/by-user")
  @RateLimit(
      limitForPeriod = 3,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.USER)
  public Flux<String> userScopedRateLimit(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    return webSocketMessageFlux
        .doOnNext(msg -> log.info("Received user scoped message: {}", msg.getPayloadAsText()))
        .map(__ -> "Message Processed");
  }

  /** Endpoint with rate limit by IP scope. Allows 5 messages per 10 seconds. */
  @OnMessage(value = "/by-ip")
  @RateLimit(
      limitForPeriod = 5,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.IP)
  public Flux<String> ipScopedRateLimit(
      @RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    return webSocketMessageFlux
        .doOnNext(msg -> log.info("Received ip scoped message: {}", msg.getPayloadAsText()))
        .map(__ -> "Message Processed");
  }
}
