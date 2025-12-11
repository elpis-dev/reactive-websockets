package io.github.elpis.reactive.websockets.handler.config;

import java.util.Optional;

/**
 * Configuration for WebSocket heartbeat (ping/pong).
 *
 * @param config optional heartbeat configuration containing interval and timeout
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public record HeartbeatConfig(Optional<HeartbeatConfigData> config) {

  /**
   * Heartbeat configuration data.
   *
   * @param interval heartbeat interval in seconds
   * @param timeout heartbeat timeout in seconds
   */
  public record HeartbeatConfigData(long interval, long timeout) {}

  /** Creates a HeartbeatConfig with heartbeat disabled. */
  public static HeartbeatConfig disabled() {
    return new HeartbeatConfig(Optional.empty());
  }

  /**
   * Creates a HeartbeatConfig with the specified interval and timeout.
   *
   * @param interval heartbeat interval in seconds
   * @param timeout heartbeat timeout in seconds
   * @return configured HeartbeatConfig
   */
  public static HeartbeatConfig of(long interval, long timeout) {
    return new HeartbeatConfig(Optional.of(new HeartbeatConfigData(interval, timeout)));
  }

  /** Checks if heartbeat is enabled. */
  public boolean isEnabled() {
    return config.isPresent();
  }

  /** Gets the heartbeat interval in seconds. */
  public long getInterval() {
    return config.map(HeartbeatConfigData::interval).orElse(30L);
  }

  /** Gets the heartbeat timeout in seconds. */
  public long getTimeout() {
    return config.map(HeartbeatConfigData::timeout).orElse(60L);
  }
}
