package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Configures rate limiting for WebSocket message processing.
 *
 * <p>Limits the number of messages processed within a time period to prevent abuse. Uses
 * Resilience4j rate limiter internally.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Class-level: 100 messages per second for all handlers
 * @MessageEndpoint("/ws")
 * @RateLimit(limitForPeriod = 100, limitRefreshPeriod = 1)
 * public class ChatEndpoint {
 *     @OnMessage("/chat")
 *     public Flux<String> chat(Flux<WebSocketMessage> messages) { ... }
 * }
 *
 * // Method-level with custom scope
 * @MessageEndpoint("/ws")
 * public class MixedEndpoint {
 *     @OnMessage("/api")
 *     @RateLimit(limitForPeriod = 10, scope = RateLimitScope.USER)
 *     public Flux<Response> api(Flux<WebSocketMessage> messages) { ... }
 *
 *     @OnMessage("/public")
 *     @RateLimit(limitForPeriod = 5, scope = RateLimitScope.IP)
 *     public Flux<Response> publicApi(Flux<WebSocketMessage> messages) { ... }
 * }
 * }</pre>
 *
 * <p>Precedence (highest to lowest):
 *
 * <ol>
 *   <li>@RateLimit on method
 *   <li>@RateLimit on class
 *   <li>Disabled (default)
 * </ol>
 *
 * @author Phillip J. Fry
 * @see RateLimitScope
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

  /**
   * The maximum number of requests allowed within the refresh period.
   *
   * @return the limit for the period
   */
  int limitForPeriod() default 10;

  /**
   * The duration of the refresh period in which the limit applies.
   *
   * @return the refresh period duration
   */
  long limitRefreshPeriod() default 1;

  /**
   * The time unit for the refresh period.
   *
   * @return the time unit
   */
  TimeUnit timeUnit() default TimeUnit.SECONDS;

  /**
   * The maximum time to wait for permission to proceed with a request.
   *
   * @return the timeout duration in milliseconds
   */
  long timeoutDuration() default 25;

  /**
   * Whether rate limiting is enabled.
   *
   * @return true if enabled, false otherwise
   */
  boolean enabled() default true;

  /**
   * The scope of the rate limiter. Determines whether rate limiting is applied per session, per
   * user (based on authentication principal), or per IP address.
   *
   * @return the rate limit scope
   */
  RateLimitScope scope() default RateLimitScope.SESSION;

  /** Enum defining the scope of rate limiting. */
  enum RateLimitScope {
    /** Rate limit per WebSocket session. */
    SESSION,
    /** Rate limit per authenticated user (based on principal). */
    USER,
    /** Rate limit per IP address. */
    IP,
    /** Inherit rate limit configuration from @MessageEndpoint (only valid in @OnMessage). */
    INHERIT
  }
}
