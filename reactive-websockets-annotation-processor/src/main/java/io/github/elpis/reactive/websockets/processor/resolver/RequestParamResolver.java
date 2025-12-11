package io.github.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketResolverException;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

public final class RequestParamResolver extends SocketApiAnnotationResolver<RequestParam> {
  private static final String CODE_FOR_GET_LIST_QUERY_REQUIRED =
      """
            final $T $L = context.getQueryParams($S, $S, $T.class);
            if ($L.isEmpty())
             throw new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException($S);
            """;

  private static final String CODE_FOR_GET_LIST_QUERY =
      "final $T $L = context.getQueryParams($S, $S, $T.class);\n";

  private static final String CODE_FOR_GET_SINGLE_QUERY_REQUIRED =
      """
            final $T $L = context.getQueryParam($S, $S, $T.class)
            .orElseThrow(() -> new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException($S));
            """;

  private static final String CODE_FOR_GET_SINGLE_QUERY =
      """
            final $T $L = context.getQueryParam($S, $S, $T.class)
            .orElseGet(() -> io.github.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));
            """;

  RequestParamResolver(Elements elements, Types types) {
    super(elements, types);
  }

  @Override
  public CodeBlock resolve(final VariableElement parameter) {
    final TypeMirror parameterType = parameter.asType();
    final RequestParam annotation = parameter.getAnnotation(this.getAnnotationType());

    final Element listType = this.getElements().getTypeElement(List.class.getCanonicalName());

    final String defaultValue =
        Optional.of(annotation.defaultValue())
            .filter(value -> !value.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value))
            .orElse(null);

    if (this.getTypes().isAssignable(this.getTypes().erasure(parameterType), listType.asType())) {
      if (annotation.value().isEmpty()) {
        throw new WebSocketResolverException(
            "Value cannot be empty at @RequestParam %s %s",
            parameterType, parameter.getSimpleName().toString());
      }

      if (parameterType instanceof DeclaredType declaredReturnType) {
        final TypeMirror listDeclaredType = declaredReturnType.getTypeArguments().get(0);

        if (annotation.required()) {
          return CodeBlock.of(
              CODE_FOR_GET_LIST_QUERY_REQUIRED,
              parameterType,
              parameter.getSimpleName().toString(),
              annotation.value(),
              defaultValue,
              listDeclaredType,
              parameter.getSimpleName().toString(),
              String.format(
                  "@RequestParam List<%s> %s is marked as required but was not present on request. "
                      + "Default value was not set.",
                  listDeclaredType.toString(), parameter.getSimpleName().toString()));
        } else {
          return CodeBlock.of(
              CODE_FOR_GET_LIST_QUERY,
              parameterType,
              parameter.getSimpleName().toString(),
              annotation.value(),
              defaultValue,
              listDeclaredType);
        }
      } else {
        throw new WebSocketResolverException(
            "Cannot process @RequestParam annotated parameter '%s': bad return type: %s",
            parameter.getSimpleName().toString(), parameterType);
      }
    } else {
      if (annotation.value().isEmpty()) {
        throw new WebSocketResolverException(
            "Value cannot be empty at @RequestParam %s %s",
            parameterType, parameter.getSimpleName().toString());
      }

      if (annotation.required()) {
        return CodeBlock.of(
            CODE_FOR_GET_SINGLE_QUERY_REQUIRED,
            parameterType,
            parameter.getSimpleName().toString(),
            annotation.value(),
            defaultValue,
            parameterType,
            String.format(
                "@RequestParam %s %s is marked as required but was not present on request. "
                    + "Default value was not set.",
                parameter.asType().toString(), parameter.getSimpleName().toString()));
      } else {
        return CodeBlock.of(
            CODE_FOR_GET_SINGLE_QUERY,
            parameterType,
            parameter.getSimpleName().toString(),
            annotation.value(),
            defaultValue,
            parameterType,
            parameterType);
      }
    }
  }

  @Override
  Class<RequestParam> getAnnotationType() {
    return RequestParam.class;
  }
}
