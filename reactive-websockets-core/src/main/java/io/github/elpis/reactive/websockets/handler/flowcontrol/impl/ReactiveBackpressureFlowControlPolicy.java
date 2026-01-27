package io.github.elpis.reactive.websockets.handler.flowcontrol.impl;

import static io.github.elpis.reactive.websockets.Constants.DEFAULT_KEY;

import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveBackpressureFlowControlRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import java.util.Optional;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

/**
 * Flow control policy that applies backpressure strategies to outbound WebSocket message streams.
 *
 * <p>This policy checks for backpressure configuration in the {@link
 * ReactiveBackpressureFlowControlRegistry} based on the WebSocket path. If a configuration is found
 * and enabled, it applies the specified backpressure strategy to the outgoing message Flux.
 *
 * <p>Supported backpressure strategies include:
 *
 * <ul>
 *   <li>BUFFER - Buffers messages up to a specified size.
 *   <li>DROP_OLDEST - Drops the oldest messages when overwhelmed.
 *   <li>DROP_LATEST - Drops the latest messages when overwhelmed.
 *   <li>ERROR - Emits an error when overwhelmed.
 * </ul>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveBackpressureFlowControlPolicy implements FlowControlPolicy {
  private final ReactiveBackpressureFlowControlRegistry registry;

  public ReactiveBackpressureFlowControlPolicy(
      final ReactiveBackpressureFlowControlRegistry registry) {
    this.registry = registry;
  }

  /**
   * Applies backpressure strategy to the outgoing WebSocket message Flux based on the path.
   *
   * @param path the WebSocket endpoint path
   * @param __ the WebSocket session context (not used)
   * @param webSocketMessageFlux the original Flux of WebSocket messages
   * @return the Flux with backpressure applied if configured, otherwise the original Flux
   */
  @Override
  public Flux<WebSocketMessage> apply(
      final String path,
      final WebSocketSessionContext __,
      final Flux<WebSocketMessage> webSocketMessageFlux) {
    return this.resolveBackpressureConfig(path)
        .map(config -> applyBackpressure(config, webSocketMessageFlux))
        .orElse(webSocketMessageFlux);
  }

  /**
   * Specifies that this flow control policy applies to the output (outgoing messages).
   *
   * @return FlowControlPlacement.OUTPUT
   */
  @Override
  public FlowControlPlacement placement() {
    return FlowControlPlacement.OUTPUT;
  }

  /**
   * Applies the configured backpressure strategy to the given Flux.
   *
   * @param flux the Flux to apply backpressure to
   * @return the Flux with backpressure applied, or the original Flux if disabled
   */
  protected Flux<WebSocketMessage> applyBackpressure(
      final BackpressureConfig config, final Flux<WebSocketMessage> flux) {

    final Backpressure.BackpressureStrategy strategy = config.getStrategy();
    final int bufferSize = config.getBufferSize();

    return switch (strategy) {
      case BUFFER -> flux.onBackpressureBuffer(bufferSize);
      case DROP_OLDEST -> flux.onBackpressureLatest();
      case DROP_LATEST -> flux.onBackpressureDrop();
      case ERROR -> flux.onBackpressureError();
    };
  }

  private Optional<BackpressureConfig> resolveBackpressureConfig(final String path) {
    return Optional.ofNullable(this.registry.get(path))
        .or(() -> Optional.ofNullable(this.registry.get(DEFAULT_KEY)))
        .filter(BackpressureConfig::isEnabled);
  }
}
