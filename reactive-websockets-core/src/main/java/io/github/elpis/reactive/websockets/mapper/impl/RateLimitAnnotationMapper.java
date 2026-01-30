package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;

/**
 * Maps the {@link RateLimit} annotation to a {@link RateLimitConfig} instance.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class RateLimitAnnotationMapper implements AnnotationMapper<RateLimit, RateLimitConfig> {

  /**
   * Maps the {@link RateLimit} annotation to a {@link RateLimitConfig}.
   *
   * @param annotation the RateLimit annotation
   * @return the corresponding RateLimitConfig
   */
  @Override
  public RateLimitConfig mapTo(final RateLimit annotation) {
    return annotation.enabled()
        ? RateLimitConfig.of(
            annotation.limitForPeriod(),
            annotation.limitRefreshPeriod(),
            annotation.timeUnit(),
            annotation.timeoutDuration(),
            annotation.scope(),
            null)
        : RateLimitConfig.disabled();
  }
}
