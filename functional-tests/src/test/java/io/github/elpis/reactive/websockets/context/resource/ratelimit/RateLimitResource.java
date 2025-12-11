package io.github.elpis.reactive.websockets.context.resource.ratelimit;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@MessageEndpoint(value = "/ratelimit", rateLimit = @RateLimit(limitForPeriod = 5))
public class RateLimitResource {
  private static final Logger log = LoggerFactory.getLogger(RateLimitResource.class);

  /** Endpoint that inherits rate limit from @MessageEndpoint. Allows 5 messages per second. */
  @OnMessage(value = "/default", mode = Mode.BROADCAST)
  public void defaultRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /**
   * Endpoint with custom rate limit that overrides @MessageEndpoint. Allows 10 messages per second.
   */
  @OnMessage(value = "/custom", mode = Mode.BROADCAST, rateLimit = @RateLimit(limitForPeriod = 10))
  public void customRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /** Endpoint with rate limiting disabled. */
  @OnMessage(value = "/disabled", mode = Mode.BROADCAST, rateLimit = @RateLimit(enabled = false))
  public void disabledRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }

  /** Endpoint with rate limit by USER scope. */
  @OnMessage(
      value = "/by-user",
      mode = Mode.BROADCAST,
      rateLimit = @RateLimit(limitForPeriod = 3, scope = RateLimit.RateLimitScope.USER))
  public void userScopedRateLimit(@RequestBody final Flux<WebSocketMessage> webSocketMessageFlux) {
    webSocketMessageFlux.subscribe(
        message -> log.info("Received message: {}", message.getPayloadAsText()));
  }
}
