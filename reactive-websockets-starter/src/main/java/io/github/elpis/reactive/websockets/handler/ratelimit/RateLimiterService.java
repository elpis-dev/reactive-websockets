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
 * <p>This service creates and manages rate limiter registries based on endpoint path. Each registry
 * shares the same configuration but provides individual rate limiters per identifier (session ID,
 * user ID, or IP address based on scope).
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
   * Registers a rate limiter registry for the specified path.
   *
   * <p>This method should be called during handler bean construction to register the rate limiter
   * configuration for an endpoint. The registry will be used to create individual rate limiters per
   * identifier.
   *
   * @param pathTemplate the WebSocket endpoint path template (used as the registry key)
   * @param limitForPeriod the maximum number of requests allowed within the refresh period
   * @param limitRefreshPeriod the duration of the refresh period
   * @param timeUnit the time unit for the refresh period
   * @param timeoutDuration the timeout duration in milliseconds
   */
  public void register(
      final String pathTemplate,
      final int limitForPeriod,
      final long limitRefreshPeriod,
      final TimeUnit timeUnit,
      final long timeoutDuration) {

    registryCache.computeIfAbsent(
        pathTemplate,
        key -> {
          final RateLimiterConfig config =
              RateLimiterConfig.custom()
                  .limitRefreshPeriod(Duration.ofMillis(timeUnit.toMillis(limitRefreshPeriod)))
                  .limitForPeriod(limitForPeriod)
                  .timeoutDuration(Duration.ofMillis(timeoutDuration))
                  .build();

          log.info(
              "Registering RateLimiterRegistry for path: {}, limitForPeriod: {}, refreshPeriod: {} {}, timeout: {} ms",
              pathTemplate,
              limitForPeriod,
              limitRefreshPeriod,
              timeUnit,
              timeoutDuration);

          return RateLimiterRegistry.of(config);
        });
  }

  /**
   * Gets or creates a rate limiter for the specified path and identifier.
   *
   * <p>The identifier determines the scope of rate limiting (e.g., session ID, user ID, or IP).
   * Rate limiters with the same identifier share the same rate limit bucket.
   *
   * @param pathTemplate the WebSocket endpoint path template
   * @param identifier the unique identifier (session ID, user ID, IP, or "default" for broadcast)
   * @return the rate limiter for the given identifier, or null if no registry exists for the path
   */
  public RateLimiter get(final String pathTemplate, final String identifier) {
    final RateLimiterRegistry registry = registryCache.get(pathTemplate);
    if (registry == null) {
      log.warn("No rate limiter registry found for path: {}", pathTemplate);
      return null;
    }
    return registry.rateLimiter(identifier);
  }

  /** Clears all cached rate limiter registries. Useful for testing. */
  public void clearCache() {
    registryCache.clear();
  }
}
