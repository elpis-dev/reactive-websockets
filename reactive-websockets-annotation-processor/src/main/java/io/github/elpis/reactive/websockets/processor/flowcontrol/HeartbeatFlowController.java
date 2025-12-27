package io.github.elpis.reactive.websockets.processor.flowcontrol;

import io.github.elpis.reactive.websockets.processor.WebSocketHandlerAutoProcessor.HeartbeatConfigData;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import javax.lang.model.element.Element;

/**
 * Controller for resolving and generating heartbeat configuration during annotation processing.
 *
 * <p>Handles the precedence logic for heartbeat configuration:
 *
 * <ol>
 *   <li>If @Heartbeat is directly on the method, use it
 *   <li>Else if @Heartbeat is directly on the class, use it
 *   <li>Else heartbeat is disabled (returns null)
 * </ol>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class HeartbeatFlowController {

  private HeartbeatFlowController() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Resolves heartbeat configuration from annotations with proper precedence.
   *
   * @param method the method element (for direct @Heartbeat on method)
   * @param classElement the class element (for direct @Heartbeat on class)
   * @return the resolved heartbeat configuration data, or null if disabled
   */
  public static HeartbeatConfigData resolveHeartbeatConfig(
      final Element method, final Element classElement) {
    final Heartbeat methodHeartbeat = method.getAnnotation(Heartbeat.class);
    if (methodHeartbeat != null && methodHeartbeat.enabled()) {
      return new HeartbeatConfigData(methodHeartbeat.interval(), methodHeartbeat.timeout());
    }

    final Heartbeat classHeartbeat = classElement.getAnnotation(Heartbeat.class);
    if (classHeartbeat != null && classHeartbeat.enabled()) {
      return new HeartbeatConfigData(classHeartbeat.interval(), classHeartbeat.timeout());
    }

    return null;
  }

  /**
   * Generates code block string for HeartbeatConfig creation.
   *
   * @param config the heartbeat configuration data, or null if disabled
   * @return Java code string for creating HeartbeatConfig
   */
  public static String generateHeartbeatConfig(final HeartbeatConfigData config) {
    if (config == null) {
      return "io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig.disabled()";
    }
    return String.format(
        "io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig.of(%dL, %dL)",
        config.interval(), config.timeout());
  }
}
