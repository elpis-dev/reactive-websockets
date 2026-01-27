package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;

/**
 * Registry for reactive backpressure flow control policies.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveBackpressureFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<Backpressure, BackpressureConfig> {

  public ReactiveBackpressureFlowControlRegistry(
      final AnnotationMapper<Backpressure, BackpressureConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  /**
   * Returns the annotation type handled by this registry.
   *
   * @return Backpressure.class
   */
  @Override
  public Class<Backpressure> getAnnotationType() {
    return Backpressure.class;
  }

  /**
   * Determines if the given Backpressure annotation should be registered based on its enabled
   * status.
   *
   * @param backpressure the Backpressure annotation
   * @return true if the backpressure is not null and enabled, false otherwise
   */
  @Override
  public boolean shouldRegister(final Backpressure backpressure) {
    return backpressure != null && backpressure.enabled();
  }
}
