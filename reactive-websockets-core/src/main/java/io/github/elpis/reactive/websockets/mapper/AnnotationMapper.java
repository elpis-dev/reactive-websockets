package io.github.elpis.reactive.websockets.mapper;

import java.lang.annotation.Annotation;

public interface AnnotationMapper<A extends Annotation, T> {
  T mapTo(final A annotation);
}
