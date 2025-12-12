package io.github.elpis.reactive.websockets.processor.flowcontrol;

import io.github.elpis.reactive.websockets.processor.WebSocketHandlerAutoProcessor.RateLimitConfigData;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import javax.lang.model.element.Element;

/**
 * Controller for resolving and generating rate limit configuration during annotation processing.
 *
 * <p>Handles the precedence logic for rate limit configuration:
 *
 * <ol>
 *   <li>If @RateLimit is directly on the method, use it (enabled or disabled)
 *   <li>Else if @RateLimit is directly on the class, use it (enabled or disabled)
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
   * @param method the method element (for direct @RateLimit on method)
   * @param classElement the class element (for direct @RateLimit on class)
   * @return the resolved rate limit configuration data, or null if disabled
   */
  public static RateLimitConfigData resolveRateLimitConfig(
      final Element method, final Element classElement) {
    // Priority 1: @RateLimit directly on method
    final RateLimit methodRateLimit = method.getAnnotation(RateLimit.class);
    if (methodRateLimit != null) {
      if (methodRateLimit.enabled()) {
        return createRateLimitConfigData(methodRateLimit);
      } else {
        // Explicitly disabled at method level
        return null;
      }
    }

    // Priority 2: @RateLimit directly on class
    final RateLimit classRateLimit = classElement.getAnnotation(RateLimit.class);
    if (classRateLimit != null) {
      if (classRateLimit.enabled()) {
        return createRateLimitConfigData(classRateLimit);
      } else {
        // Explicitly disabled at class level
        return null;
      }
    }

    // Return null to indicate disabled
    return null;
  }

  /**
   * Creates RateLimitConfigData from a RateLimit annotation.
   *
   * @param rateLimit the rate limit annotation
   * @return the rate limit configuration data
   */
  private static RateLimitConfigData createRateLimitConfigData(final RateLimit rateLimit) {
    return new RateLimitConfigData(
        rateLimit.limitForPeriod(),
        rateLimit.limitRefreshPeriod(),
        rateLimit.timeUnit().name(),
        rateLimit.timeoutDuration(),
        rateLimit.scope().name());
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
