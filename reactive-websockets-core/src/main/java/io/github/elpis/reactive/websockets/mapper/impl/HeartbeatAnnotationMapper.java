package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;

/**
 * Maps the {@link Heartbeat} annotation to a {@link HeartbeatConfig} instance.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class HeartbeatAnnotationMapper implements AnnotationMapper<Heartbeat, HeartbeatConfig> {

  /**
   * Maps the {@link Heartbeat} annotation to a {@link HeartbeatConfig}.
   *
   * @param annotation the Heartbeat annotation
   * @return the corresponding HeartbeatConfig
   */
  @Override
  public HeartbeatConfig mapTo(final Heartbeat annotation) {
    return annotation.enabled()
        ? HeartbeatConfig.of(annotation.interval(), annotation.timeout())
        : HeartbeatConfig.disabled();
  }
}
