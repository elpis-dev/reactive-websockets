package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;

public final class ReactiveBackpressureFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<Backpressure, BackpressureConfig> {

  public ReactiveBackpressureFlowControlRegistry(
      final AnnotationMapper<Backpressure, BackpressureConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  @Override
  public Class<Backpressure> getAnnotationType() {
    return Backpressure.class;
  }

  @Override
  public boolean shouldRegister(final Backpressure backpressure) {
    return backpressure != null && backpressure.enabled();
  }
}
