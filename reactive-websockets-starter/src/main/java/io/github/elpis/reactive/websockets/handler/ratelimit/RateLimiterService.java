package io.github.elpis.reactive.websockets.handler.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing rate limiters for WebSocket endpoints.
 *
 * <p>This service creates and manages individual rate limiters based on endpoint configuration and
 * scope (session, user, or IP).
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Service
public class RateLimiterService {
  private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

  private final ConcurrentHashMap<String, RateLimiterRegistry> registryCache =
      new ConcurrentHashMap<>();

  /**
   * Gets or creates a rate limiter for the specified endpoint and identifier.
   *
   * @param pathTemplate the WebSocket endpoint path template
   * @param limitForPeriod the maximum number of requests allowed within the refresh period
   * @param limitRefreshPeriod the duration of the refresh period
   * @param timeUnit the time unit for the refresh period
   * @param timeoutDuration the timeout duration in milliseconds
   * @param identifier the unique identifier (session ID, user ID, or IP address)
   * @return the rate limiter instance
   */
  public RateLimiter getRateLimiter(
      final String pathTemplate,
      final int limitForPeriod,
      final long limitRefreshPeriod,
      final TimeUnit timeUnit,
      final long timeoutDuration,
      final String identifier) {

    final String registryKey =
        buildRegistryKey(
            pathTemplate, limitForPeriod, limitRefreshPeriod, timeUnit, timeoutDuration);

    final RateLimiterRegistry registry =
        registryCache.computeIfAbsent(
            registryKey,
            key -> {
              final RateLimiterConfig config =
                  RateLimiterConfig.custom()
                      .limitRefreshPeriod(Duration.ofMillis(timeUnit.toMillis(limitRefreshPeriod)))
                      .limitForPeriod(limitForPeriod)
                      .timeoutDuration(Duration.ofMillis(timeoutDuration))
                      .build();

              log.debug(
                  "Creating new RateLimiterRegistry for path: {}, limitForPeriod: {}, refreshPeriod: {} {}",
                  pathTemplate,
                  limitForPeriod,
                  limitRefreshPeriod,
                  timeUnit);

              return RateLimiterRegistry.of(config);
            });

    return registry.rateLimiter(identifier);
  }

  /** Builds a unique key for the registry cache based on rate limiter configuration. */
  private String buildRegistryKey(
      final String pathTemplate,
      final int limitForPeriod,
      final long limitRefreshPeriod,
      final TimeUnit timeUnit,
      final long timeoutDuration) {
    return String.format(
        "%s:%d:%d:%s:%d",
        pathTemplate, limitForPeriod, limitRefreshPeriod, timeUnit, timeoutDuration);
  }

  /** Clears all cached rate limiter registries. Useful for testing. */
  public void clearCache() {
    registryCache.clear();
  }
}
