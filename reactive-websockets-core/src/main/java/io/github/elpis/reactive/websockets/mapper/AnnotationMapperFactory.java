package io.github.elpis.reactive.websockets.mapper;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * Factory for retrieving appropriate {@link AnnotationMapper} instances based on annotation types.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class AnnotationMapperFactory {
  private final Map<Class<? extends Annotation>, AnnotationMapper<? extends Annotation, ?>> mappers;

  public AnnotationMapperFactory(final List<AnnotationMapper<? extends Annotation, ?>> mappers) {
    this.mappers =
        mappers.stream()
            .collect(Collectors.toMap(this::extractAnnotationType, Function.identity()));
  }

  /**
   * Retrieves the appropriate {@link AnnotationMapper} for the given annotation type.
   *
   * @param annotationType the annotation type
   * @param <A> the type of annotation
   * @param <C> the type of configuration object
   * @return the corresponding AnnotationMapper
   * @throws IllegalArgumentException if no mapper is found for the given annotation type
   */
  @SuppressWarnings("unchecked")
  public <A extends Annotation, C> AnnotationMapper<A, C> getMapper(final Class<A> annotationType) {
    final AnnotationMapper<?, ?> mapper = mappers.get(annotationType);
    Assert.notNull(mapper, "No mapper for annotation type " + annotationType.getName());
    return (AnnotationMapper<A, C>) mapper;
  }

  /**
   * Extracts the annotation type that the given mapper handles.
   *
   * @param mapper the AnnotationMapper
   * @return the annotation type
   */
  @SuppressWarnings("unchecked")
  private Class<? extends Annotation> extractAnnotationType(AnnotationMapper<?, ?> mapper) {
    return (Class<? extends Annotation>)
        ResolvableType.forClass(mapper.getClass())
            .as(AnnotationMapper.class)
            .getGeneric(0)
            .resolve();
  }
}
