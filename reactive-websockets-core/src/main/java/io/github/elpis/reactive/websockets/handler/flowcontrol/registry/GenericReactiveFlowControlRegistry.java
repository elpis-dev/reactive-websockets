package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericReactiveFlowControlRegistry<A extends Annotation, T>
    extends ConcurrentHashMap<String, T> {
  private static final Logger log =
      LoggerFactory.getLogger(GenericReactiveFlowControlRegistry.class);

  private final AnnotationMapper<A, T> annotationMapper;
  private final ReactiveWebsocketMessageEndpointResolver endpointProcessor;

  public GenericReactiveFlowControlRegistry(
      final AnnotationMapper<A, T> annotationMapper,
      final ReactiveWebsocketMessageEndpointResolver endpointProcessor) {

    super();
    this.annotationMapper = annotationMapper;
    this.endpointProcessor = endpointProcessor;

    this.initialize();
  }

  public abstract Class<A> getAnnotationType();

  public abstract boolean shouldRegister(final A annotation);

  void initialize() {
    endpointProcessor
        .getHandlerMethods()
        .forEach(
            (path, handlerMethod) -> {
              final A annotation = handlerMethod.getAnnotation(this.getAnnotationType());
              if (this.shouldRegister(annotation)) {
                final T t = annotationMapper.mapTo(annotation);
                this.put(path, t);

                if (log.isTraceEnabled()) {
                  log.trace("Registered handler method {} for path {}", handlerMethod, path);
                }
              } else {
                if (log.isTraceEnabled()) {
                  log.trace("Ignoring invalid handler method {} for path {}", handlerMethod, path);
                }
              }
            });
  }
}
