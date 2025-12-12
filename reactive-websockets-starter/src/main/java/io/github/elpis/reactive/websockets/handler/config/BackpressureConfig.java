package io.github.elpis.reactive.websockets.handler.config;

import java.util.Optional;

/**
 * Configuration for WebSocket backpressure handling.
 *
 * @param config optional backpressure configuration
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public record BackpressureConfig(Optional<BackpressureConfigData> config) {

  /**
   * Backpressure configuration data.
   *
   * @param strategy the backpressure strategy (BUFFER, DROP_OLDEST, DROP_LATEST, ERROR)
   * @param bufferSize the buffer size for BUFFER strategy
   */
  public record BackpressureConfigData(String strategy, int bufferSize) {}

  /** Creates a BackpressureConfig with backpressure handling disabled. */
  public static BackpressureConfig disabled() {
    return new BackpressureConfig(Optional.empty());
  }

  /**
   * Creates a BackpressureConfig with the specified parameters.
   *
   * @param strategy the backpressure strategy
   * @param bufferSize the buffer size for BUFFER strategy
   * @return configured BackpressureConfig
   */
  public static BackpressureConfig of(String strategy, int bufferSize) {
    return new BackpressureConfig(Optional.of(new BackpressureConfigData(strategy, bufferSize)));
  }

  /** Checks if backpressure handling is enabled. */
  public boolean isEnabled() {
    return config.isPresent();
  }

  /** Gets the backpressure strategy. */
  public String getStrategy() {
    return config.map(BackpressureConfigData::strategy).orElse("BUFFER");
  }

  /** Gets the buffer size. */
  public int getBufferSize() {
    return config.map(BackpressureConfigData::bufferSize).orElse(256);
  }
}
