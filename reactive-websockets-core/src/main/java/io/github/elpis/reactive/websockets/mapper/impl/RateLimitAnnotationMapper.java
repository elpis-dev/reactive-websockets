package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;

public class RateLimitAnnotationMapper implements AnnotationMapper<RateLimit, RateLimitConfig> {

  @Override
  public RateLimitConfig mapTo(final RateLimit annotation) {
    return annotation.enabled()
        ? RateLimitConfig.of(
            annotation.limitForPeriod(),
            annotation.limitRefreshPeriod(),
            annotation.timeUnit(),
            annotation.timeoutDuration(),
            annotation.scope())
        : RateLimitConfig.disabled();
  }
}
