package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;

public class BackpressureAnnotationMapper
    implements AnnotationMapper<Backpressure, BackpressureConfig> {

  @Override
  public BackpressureConfig mapTo(final Backpressure annotation) {
    return annotation.enabled()
        ? BackpressureConfig.of(annotation.strategy(), annotation.bufferSize())
        : BackpressureConfig.disabled();
  }
}
