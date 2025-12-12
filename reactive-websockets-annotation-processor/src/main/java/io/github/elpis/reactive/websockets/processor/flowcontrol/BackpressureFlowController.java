package io.github.elpis.reactive.websockets.processor.flowcontrol;

import io.github.elpis.reactive.websockets.processor.WebSocketHandlerAutoProcessor.BackpressureConfigData;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import javax.lang.model.element.Element;

/**
 * Controller for resolving and generating backpressure configuration during annotation processing.
 *
 * <p>Handles the precedence logic for backpressure configuration:
 *
 * <ol>
 *   <li>If @Backpressure is directly on the method, use it (enabled or disabled)
 *   <li>Else if @Backpressure is directly on the class, use it (enabled or disabled)
 *   <li>Else backpressure handling is disabled (returns null)
 * </ol>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class BackpressureFlowController {

  private BackpressureFlowController() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Resolves backpressure configuration from annotations with proper precedence.
   *
   * @param method the method element (for direct @Backpressure on method)
   * @param classElement the class element (for direct @Backpressure on class)
   * @return the resolved backpressure configuration data, or null if disabled
   */
  public static BackpressureConfigData resolveBackpressureConfig(
      final Element method, final Element classElement) {
    // Priority 1: @Backpressure directly on method
    final Backpressure methodBackpressure = method.getAnnotation(Backpressure.class);
    if (methodBackpressure != null) {
      if (methodBackpressure.enabled()) {
        return createBackpressureConfigData(methodBackpressure);
      } else {
        // Explicitly disabled at method level
        return null;
      }
    }

    // Priority 2: @Backpressure directly on class
    final Backpressure classBackpressure = classElement.getAnnotation(Backpressure.class);
    if (classBackpressure != null) {
      if (classBackpressure.enabled()) {
        return createBackpressureConfigData(classBackpressure);
      } else {
        // Explicitly disabled at class level
        return null;
      }
    }

    // Return null to indicate disabled
    return null;
  }

  /**
   * Creates BackpressureConfigData from a Backpressure annotation.
   *
   * @param backpressure the backpressure annotation
   * @return the backpressure configuration data
   */
  private static BackpressureConfigData createBackpressureConfigData(
      final Backpressure backpressure) {
    return new BackpressureConfigData(backpressure.strategy().name(), backpressure.bufferSize());
  }

  /**
   * Generates code block string for BackpressureConfig creation.
   *
   * @param config the backpressure configuration data, or null if disabled
   * @return Java code string for creating BackpressureConfig
   */
  public static String generateBackpressureConfig(final BackpressureConfigData config) {
    if (config == null) {
      return "io.github.elpis.reactive.websockets.handler.config.BackpressureConfig.disabled()";
    }
    return String.format(
        "io.github.elpis.reactive.websockets.handler.config.BackpressureConfig.of(\"%s\", %d)",
        config.strategy(), config.bufferSize());
  }
}
