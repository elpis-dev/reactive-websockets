package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;

/**
 * Maps the {@link Backpressure} annotation to a {@link BackpressureConfig} instance.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class BackpressureAnnotationMapper
    implements AnnotationMapper<Backpressure, BackpressureConfig> {

  /**
   * Maps the {@link Backpressure} annotation to a {@link BackpressureConfig}.
   *
   * @param annotation the Backpressure annotation
   * @return the corresponding BackpressureConfig
   */
  @Override
  public BackpressureConfig mapTo(final Backpressure annotation) {
    return annotation.enabled()
        ? BackpressureConfig.of(annotation.strategy(), annotation.bufferSize())
        : BackpressureConfig.disabled();
  }
}
