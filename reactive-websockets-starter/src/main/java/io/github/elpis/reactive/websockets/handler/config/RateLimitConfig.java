package io.github.elpis.reactive.websockets.handler.config;

import java.util.Optional;

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
      int limitForPeriod, long limitRefreshPeriod, String timeUnit, long timeout, String scope) {}

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
      int limitForPeriod, long limitRefreshPeriod, String timeUnit, long timeout, String scope) {
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
  public String getTimeUnit() {
    return config.map(RateLimitConfigData::timeUnit).orElse("SECONDS");
  }

  /** Gets the timeout duration. */
  public long getTimeout() {
    return config.map(RateLimitConfigData::timeout).orElse(25L);
  }

  /** Gets the rate limiting scope. */
  public String getScope() {
    return config.map(RateLimitConfigData::scope).orElse("SESSION");
  }
}
