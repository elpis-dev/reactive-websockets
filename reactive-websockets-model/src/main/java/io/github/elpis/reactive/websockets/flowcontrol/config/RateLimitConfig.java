package io.github.elpis.reactive.websockets.flowcontrol.config;

import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebSocket rate limiting.
 *
 * @param config optional rate limiting configuration
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public record RateLimitConfig(Optional<RateLimitConfigData> config) {

  /**
   * Rate limiting configuration data.
   *
   * @param limitForPeriod maximum requests allowed in the period
   * @param limitRefreshPeriod duration of the refresh period
   * @param timeUnit time unit for the refresh period
   * @param timeout timeout duration in milliseconds
   * @param scope scope of rate limiting (SESSION, USER, IP)
   */
  public record RateLimitConfigData(
      int limitForPeriod,
      long limitRefreshPeriod,
      TimeUnit timeUnit,
      long timeout,
      RateLimit.RateLimitScope scope) {}

  /** Creates a RateLimitConfig with rate limiting disabled. */
  public static RateLimitConfig disabled() {
    return new RateLimitConfig(Optional.empty());
  }

  /**
   * Creates a RateLimitConfig with the specified parameters.
   *
   * @param limitForPeriod maximum requests allowed in the period
   * @param limitRefreshPeriod duration of the refresh period
   * @param timeUnit time unit for the refresh period
   * @param timeout timeout duration in milliseconds
   * @param scope scope of rate limiting (SESSION, USER, IP)
   * @return configured RateLimitConfig
   */
  public static RateLimitConfig of(
      int limitForPeriod,
      long limitRefreshPeriod,
      TimeUnit timeUnit,
      long timeout,
      RateLimit.RateLimitScope scope) {
    return new RateLimitConfig(
        Optional.of(
            new RateLimitConfigData(limitForPeriod, limitRefreshPeriod, timeUnit, timeout, scope)));
  }

  /** Checks if rate limiting is enabled. */
  public boolean isEnabled() {
    return config.isPresent();
  }

  /** Gets the limit for period. */
  public int getLimitForPeriod() {
    return config.map(RateLimitConfigData::limitForPeriod).orElse(10);
  }

  /** Gets the limit refresh period. */
  public long getLimitRefreshPeriod() {
    return config.map(RateLimitConfigData::limitRefreshPeriod).orElse(1L);
  }

  /** Gets the time unit. */
  // TODO: Extract to Constants or Enum
  public TimeUnit getTimeUnit() {
    return config.map(RateLimitConfigData::timeUnit).orElse(TimeUnit.SECONDS);
  }

  /** Gets the timeout duration. */
  public long getTimeout() {
    return config.map(RateLimitConfigData::timeout).orElse(25L);
  }

  /** Gets the rate limiting scope. */
  // TODO: Extract to Constants or Enum
  public RateLimit.RateLimitScope getScope() {
    return config.map(RateLimitConfigData::scope).orElse(RateLimit.RateLimitScope.SESSION);
  }
}
