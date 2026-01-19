package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;

public class ReactiveRateLimitFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<RateLimit, RateLimitConfig> {

  public ReactiveRateLimitFlowControlRegistry(
      final AnnotationMapper<RateLimit, RateLimitConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  @Override
  public Class<RateLimit> getAnnotationType() {
    return RateLimit.class;
  }

  @Override
  public boolean shouldRegister(final RateLimit rateLimit) {
    return rateLimit != null && rateLimit.enabled();
  }
}
