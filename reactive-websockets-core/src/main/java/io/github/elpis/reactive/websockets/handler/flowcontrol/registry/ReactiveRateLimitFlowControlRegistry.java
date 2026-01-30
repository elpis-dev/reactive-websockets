package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;

/**
 * Registry for reactive rate limit flow control policies.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveRateLimitFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<RateLimit, RateLimitConfig> {

  public ReactiveRateLimitFlowControlRegistry(
      final AnnotationMapper<RateLimit, RateLimitConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  /**
   * Returns the annotation type handled by this registry.
   *
   * @return RateLimit.class
   */
  @Override
  public Class<RateLimit> getAnnotationType() {
    return RateLimit.class;
  }

  /**
   * Determines if the given RateLimit annotation should be registered.
   *
   * @param rateLimit the RateLimit annotation
   * @return true if the rate limit is not null and enabled, false otherwise
   */
  @Override
  public boolean shouldRegister(final RateLimit rateLimit) {
    return rateLimit != null;
  }
}
