package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.flowcontrol.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;

/**
 * Registry for reactive heartbeat flow control policies.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveHeartbeatFlowControlRegistry
    extends GenericReactiveFlowControlRegistry<Heartbeat, HeartbeatConfig> {

  public ReactiveHeartbeatFlowControlRegistry(
      final AnnotationMapper<Heartbeat, HeartbeatConfig> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {
    super(annotationMapper, endpointProcessor);
  }

  /**
   * Returns the annotation type handled by this registry.
   *
   * @return Heartbeat.class
   */
  @Override
  public Class<Heartbeat> getAnnotationType() {
    return Heartbeat.class;
  }

  /**
   * Determines if the given Heartbeat annotation should be registered based on its enabled status.
   *
   * @param heartbeat the Heartbeat annotation
   * @return true if the heartbeat is not null and enabled, false otherwise
   */
  @Override
  public boolean shouldRegister(final Heartbeat heartbeat) {
    return heartbeat != null && heartbeat.enabled();
  }
}
