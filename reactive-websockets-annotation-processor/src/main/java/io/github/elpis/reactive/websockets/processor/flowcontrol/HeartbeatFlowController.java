package io.github.elpis.reactive.websockets.processor.flowcontrol;

import io.github.elpis.reactive.websockets.processor.WebSocketHandlerAutoProcessor.HeartbeatConfigData;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;

/**
 * Controller for resolving and generating heartbeat configuration during annotation processing.
 *
 * <p>Handles the precedence logic for heartbeat configuration:
 *
 * <ol>
 *   <li>If @OnMessage has @Heartbeat with enabled=true, use it
 *   <li>Else if @MessageEndpoint has @Heartbeat with enabled=true, use it
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
   * @param onMessage the @OnMessage annotation
   * @param resource the @MessageEndpoint annotation
   * @return the resolved heartbeat configuration data, or null if disabled
   */
  public static HeartbeatConfigData resolveHeartbeatConfig(
      final OnMessage onMessage, final MessageEndpoint resource) {
    final Heartbeat onMessageHeartbeat = onMessage.heartbeat();
    final Heartbeat endpointHeartbeat = resource.heartbeat();

    if (onMessageHeartbeat.enabled()) {
      return new HeartbeatConfigData(onMessageHeartbeat.interval(), onMessageHeartbeat.timeout());
    } else if (endpointHeartbeat.enabled()) {
      return new HeartbeatConfigData(endpointHeartbeat.interval(), endpointHeartbeat.timeout());
    } else {
      // Return null to indicate disabled
      return null;
    }
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
