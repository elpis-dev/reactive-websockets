package io.github.elpis.reactive.websockets.processor.resolver;

import static io.github.elpis.reactive.websockets.processor.util.Constants.VARIABLE_SUFFIX;

import com.squareup.javapoet.CodeBlock;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.bind.annotation.PathVariable;

public final class PathVariableResolver extends SocketApiAnnotationResolver<PathVariable> {
  private static final String CODE_FOR_GET_PATH_REQUIRED =
      """
            final $T $L = context.getPathVariable($S, $T.class)
            .orElseThrow(() -> new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException($S));
            """;

  private static final String CODE_FOR_GET_SINGLE_PATH =
      """
            final $T $L = context.getPathVariable($S, $T.class)
            .orElseGet(() -> io.github.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));
            """;

  PathVariableResolver(Elements elements, Types types) {
    super(elements, types);
  }

  @Override
  public CodeBlock resolve(final VariableElement parameter) {
    final TypeMirror parameterType = parameter.asType();
    final PathVariable annotation = parameter.getAnnotation(this.getAnnotationType());
    final String varName = parameter.getSimpleName().toString() + VARIABLE_SUFFIX;

    if (annotation.required()) {
      return CodeBlock.of(
          CODE_FOR_GET_PATH_REQUIRED,
          parameterType,
          varName,
          annotation.value(),
          parameterType,
          String.format(
              "@PathVariable %s %s is marked as required but was not present on request. Default value was not set.",
              parameter.asType().toString(), parameter.getSimpleName().toString()));
    } else {
      return CodeBlock.of(
          CODE_FOR_GET_SINGLE_PATH,
          parameterType,
          varName,
          annotation.value(),
          parameterType,
          parameterType);
    }
  }

  @Override
  Class<PathVariable> getAnnotationType() {
    return PathVariable.class;
  }
}
