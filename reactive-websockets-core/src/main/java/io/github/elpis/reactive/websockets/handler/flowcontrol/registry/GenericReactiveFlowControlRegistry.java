package io.github.elpis.reactive.websockets.handler.flowcontrol.registry;

import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.mapper.AnnotationMapper;
import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic registry for reactive flow control configurations based on annotations.
 *
 * <p>This class scans WebSocket handler methods for a specific annotation type and registers
 * corresponding flow control configurations.
 *
 * @param <A> the type of annotation used for flow control configuration
 * @param <T> the type of flow control configuration to be registered
 */
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

  /**
   * Gets the annotation type used for flow control configuration.
   *
   * @return the annotation class
   */
  public abstract Class<A> getAnnotationType();

  /**
   * Determines whether a given annotation should be registered.
   *
   * @param annotation the annotation instance
   * @return true if the annotation should be registered, false otherwise
   */
  public abstract boolean shouldRegister(final A annotation);

  private void initialize() {
    endpointProcessor
        .getHandlerMethods()
        .forEach(
            (path, handlerMethod) -> {
              if (handlerMethod.hasAnnotation(this.getAnnotationType())) {
                final A annotation = handlerMethod.getAnnotation(this.getAnnotationType());
                if (this.shouldRegister(annotation)) {
                  final T t = annotationMapper.mapTo(annotation);
                  this.put(path, t);

                  if (log.isTraceEnabled()) {
                    log.trace("Registered handler method {} for path {}", handlerMethod, path);
                  }
                } else {
                  if (log.isTraceEnabled()) {
                    log.trace(
                        "Ignoring invalid handler method {} for path {}", handlerMethod, path);
                  }
                }
              } else {
                if (log.isTraceEnabled()) {
                  log.trace(
                      "Ignoring handler method {} for path {}, since it doesn't have an annotation {}",
                      handlerMethod,
                      path,
                      this.getAnnotationType().getSimpleName());
                }
              }
            });
  }
}
