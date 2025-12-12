package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting configuration for WebSocket endpoints. Limits the number of messages that can be
 * processed within a specified time period per user/IP address.
 *
 * <p>Uses Resilience4j rate limiter to prevent abuse and DoS attacks. When the rate limit is
 * exceeded, an error response is sent to the client.
 *
 * <p>Can be applied at:
 *
 * <ul>
 *   <li>Class level - applies to all methods in the @MessageEndpoint
 *   <li>Method level - applies to specific @OnMessage method, overrides class-level
 * </ul>
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
