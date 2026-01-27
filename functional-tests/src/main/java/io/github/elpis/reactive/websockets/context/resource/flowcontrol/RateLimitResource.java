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
  public void defaultRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /**
   * Endpoint with custom rate limit that overrides class level. Allows 10 messages per 10 seconds.
   */
  @OnMessage(value = "/custom")
  @RateLimit(
      limitForPeriod = 5,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.SESSION)
  public void customRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /** Endpoint with rate limiting disabled. */
  @OnMessage(value = "/disabled")
  @RateLimit(enabled = false)
  public void disabledRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /** Endpoint with rate limit by USER scope. Allows 3 messages per 10 seconds. */
  @OnMessage(value = "/by-user")
  @RateLimit(
      limitForPeriod = 3,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.USER)
  public void userScopedRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /** Endpoint with rate limit by IP scope. Allows 5 messages per 10 seconds. */
  @OnMessage(value = "/by-ip")
  @RateLimit(
      limitForPeriod = 5,
      limitRefreshPeriod = 10,
      timeUnit = TimeUnit.SECONDS,
      scope = RateLimit.RateLimitScope.IP)
  public void ipScopedRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }
}
