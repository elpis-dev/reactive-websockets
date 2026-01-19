package io.github.elpis.reactive.websockets.mapper.impl;

import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;

public class HeartbeatAnnotationMapper implements AnnotationMapper<Heartbeat, HeartbeatConfig> {

  @Override
  public HeartbeatConfig mapTo(final Heartbeat annotation) {
    return annotation.enabled()
        ? HeartbeatConfig.of(annotation.interval(), annotation.timeout())
        : HeartbeatConfig.disabled();
  }
}
