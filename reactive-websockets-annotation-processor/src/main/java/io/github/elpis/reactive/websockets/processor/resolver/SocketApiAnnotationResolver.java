package io.github.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketResolverException;
import java.lang.annotation.Annotation;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class SocketApiAnnotationResolver<A extends Annotation> {
  private final Elements elements;
  private final Types types;

  SocketApiAnnotationResolver(final Elements elements, final Types types) {
    this.elements = elements;
    this.types = types;
  }

  public abstract CodeBlock resolve(VariableElement parameter);

  abstract Class<A> getAnnotationType();

  public Elements getElements() {
    return elements;
  }

  public Types getTypes() {
    return types;
  }

  /**
   * Validates that the annotation value is not empty.
   *
   * @param value the annotation value to validate
   * @param annotationName the name of the annotation (e.g., "@RequestParam", "@RequestHeader")
   * @param parameterType the type of the parameter
   * @param parameterName the name of the parameter
   * @throws WebSocketResolverException if the value is empty
   * @since 1.0.0
   */
  protected void validateAnnotationValue(
      final String value,
      final String annotationName,
      final TypeMirror parameterType,
      final String parameterName) {
    if (value.isEmpty()) {
      throw new WebSocketResolverException(
          "Value cannot be empty at %s %s %s", annotationName, parameterType, parameterName);
    }
  }
}
