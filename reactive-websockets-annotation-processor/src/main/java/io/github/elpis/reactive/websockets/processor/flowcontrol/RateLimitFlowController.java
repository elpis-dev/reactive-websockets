package io.github.elpis.reactive.websockets.processor.flowcontrol;

import io.github.elpis.reactive.websockets.processor.WebSocketHandlerAutoProcessor.RateLimitConfigData;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;

/**
 * Controller for resolving and generating rate limit configuration during annotation processing.
 *
 * <p>Handles the precedence logic for rate limit configuration:
 *
 * <ol>
 *   <li>If @OnMessage has @RateLimit with scope != INHERIT, use it (enabled or disabled)
 *   <li>Else if @MessageEndpoint has @RateLimit with enabled=true, use it
 *   <li>Else rate limiting is disabled (returns null)
 * </ol>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class RateLimitFlowController {

  private RateLimitFlowController() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Resolves rate limit configuration from annotations with proper precedence.
   *
   * @param onMessage the @OnMessage annotation
   * @param resource the @MessageEndpoint annotation
   * @return the resolved rate limit configuration data, or null if disabled
   */
  public static RateLimitConfigData resolveRateLimitConfig(
      final OnMessage onMessage, final MessageEndpoint resource) {
    final RateLimit onMessageRateLimit = onMessage.rateLimit();
    final RateLimit endpointRateLimit = resource.rateLimit();

    // Check if @OnMessage has explicitly defined a @RateLimit by checking if it's not the default
    // The default @RateLimit uses scope=INHERIT to indicate inheritance
    final boolean hasOnMessageRateLimit = !isDefaultRateLimit(onMessageRateLimit);

    if (hasOnMessageRateLimit) {
      // @OnMessage explicitly defines @RateLimit, use it (even if disabled)
      if (onMessageRateLimit.enabled()) {
        return new RateLimitConfigData(
            onMessageRateLimit.limitForPeriod(),
            onMessageRateLimit.limitRefreshPeriod(),
            onMessageRateLimit.timeUnit().name(),
            onMessageRateLimit.timeoutDuration(),
            onMessageRateLimit.scope().name());
      } else {
        // Explicitly disabled at method level
        return null;
      }
    } else if (endpointRateLimit.enabled()) {
      // Fall back to @MessageEndpoint rate limit if enabled
      return new RateLimitConfigData(
          endpointRateLimit.limitForPeriod(),
          endpointRateLimit.limitRefreshPeriod(),
          endpointRateLimit.timeUnit().name(),
          endpointRateLimit.timeoutDuration(),
          endpointRateLimit.scope().name());
    } else {
      // Return null to indicate disabled
      return null;
    }
  }

  /**
   * Checks if a @RateLimit annotation is using the default INHERIT scope. The
   * default @OnMessage.rateLimit() uses scope = INHERIT to indicate "inherit
   * from @MessageEndpoint".
   *
   * @param rateLimit the rate limit annotation to check
   * @return true if using default INHERIT scope, false otherwise
   */
  private static boolean isDefaultRateLimit(final RateLimit rateLimit) {
    return rateLimit.scope() == RateLimit.RateLimitScope.INHERIT;
  }

  /**
   * Generates code block string for RateLimitConfig creation.
   *
   * @param config the rate limit configuration data, or null if disabled
   * @return Java code string for creating RateLimitConfig
   */
  public static String generateRateLimitConfig(final RateLimitConfigData config) {
    if (config == null) {
      return "io.github.elpis.reactive.websockets.handler.config.RateLimitConfig.disabled()";
    }
    return String.format(
        "io.github.elpis.reactive.websockets.handler.config.RateLimitConfig.of(%d, %dL, \"%s\", %dL, \"%s\")",
        config.limitForPeriod(),
        config.limitRefreshPeriod(),
        config.timeUnit(),
        config.timeout(),
        config.scope());
  }
}
