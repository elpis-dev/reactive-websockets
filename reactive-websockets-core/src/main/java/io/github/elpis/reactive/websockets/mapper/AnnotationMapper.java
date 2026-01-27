package io.github.elpis.reactive.websockets.mapper;

import java.lang.annotation.Annotation;

/**
 * A generic interface for mapping annotations to configuration objects.
 *
 * @param <A> the type of annotation
 * @param <T> the type of configuration object
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public interface AnnotationMapper<A extends Annotation, T> {
  /**
   * Maps the given annotation to a configuration object.
   *
   * @param annotation the annotation to map
   * @return the corresponding configuration object
   */
  T mapTo(final A annotation);
}
