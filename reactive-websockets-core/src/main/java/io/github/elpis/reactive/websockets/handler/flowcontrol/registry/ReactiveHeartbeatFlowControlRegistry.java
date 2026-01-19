package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;

public final class ReactiveHeartbeatFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<Heartbeat, HeartbeatConfig> {

  public ReactiveHeartbeatFlowControlRegistry(
      final AnnotationMapper<Heartbeat, HeartbeatConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  @Override
  public Class<Heartbeat> getAnnotationType() {
    return Heartbeat.class;
  }

  @Override
  public boolean shouldRegister(final Heartbeat heartbeat) {
    return heartbeat != null && heartbeat.enabled();
  }
}
