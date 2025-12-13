package io.github.elpis.reactive.websockets.processor.resolver;

import static io.github.elpis.reactive.websockets.processor.util.Constants.VARIABLE_SUFFIX;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketResolverException;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ValueConstants;

public final class RequestHeaderResolver extends SocketApiAnnotationResolver<RequestHeader> {
  private static final String CODE_FOR_GET_HEADERS = "final $T $L = context.getHeaders();\n";

  private static final String CODE_FOR_GET_LIST_HEADER_REQUIRED =
      """
            final $T $L = context.getHeaders($S, $S, $T.class);
            if ($L.isEmpty()) {
             throw new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException($S);
            }
            """;

  private static final String CODE_FOR_GET_LIST_HEADER =
      "final $T $L = context.getHeaders($S, $S, $T.class);\n";

  private static final String CODE_FOR_GET_SINGLE_HEADER_REQUIRED =
      """
            final $T $L = context.getHeader($S, $S, $T.class)
            .orElseThrow(() -> new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException($S));
            """;

  private static final String CODE_FOR_GET_SINGLE_HEADER =
      """
            final $T $L = context.getHeader($S, $S, $T.class)
            .orElseGet(() -> io.github.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));
            """;

  RequestHeaderResolver(Elements elements, Types types) {
    super(elements, types);
  }

  @Override
  public CodeBlock resolve(final VariableElement parameter) {
    final TypeMirror parameterType = parameter.asType();
    final RequestHeader annotation = parameter.getAnnotation(this.getAnnotationType());
    final String varName = parameter.getSimpleName().toString() + VARIABLE_SUFFIX;

    final Element httpHeadersType =
        this.getElements().getTypeElement(HttpHeaders.class.getCanonicalName());
    final Element multiValueMapType =
        this.getElements().getTypeElement(MultiValueMap.class.getCanonicalName());
    final Element listType = this.getElements().getTypeElement(List.class.getCanonicalName());

    final String defaultValue =
        Optional.of(annotation.defaultValue())
            .filter(value -> !value.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value))
            .orElse(null);

    if (this.getTypes().isAssignable(parameterType, httpHeadersType.asType())) {
      return CodeBlock.of(CODE_FOR_GET_HEADERS, HttpHeaders.class, varName);
    } else if (this.getTypes()
        .isAssignable(this.getTypes().erasure(parameterType), multiValueMapType.asType())) {
      if (!this.isMultimapParamValid(parameterType)) {
        throw new WebSocketResolverException(
            "Request header `@RequestHeader %s %s` should accept "
                + "`org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>`, but got `%s`",
            parameterType, parameter.getSimpleName(), parameterType);
      }

      final TypeName multiValueMapTypeName =
          ParameterizedTypeName.get(
              ClassName.get(MultiValueMap.class),
              TypeName.get(String.class),
              TypeName.get(String.class));

      return CodeBlock.of(CODE_FOR_GET_HEADERS, multiValueMapTypeName, varName);
    } else if (this.getTypes()
        .isAssignable(this.getTypes().erasure(parameterType), listType.asType())) {
      if (annotation.value().isEmpty()) {
        throw new WebSocketResolverException(
            "Value cannot be empty at @RequestHeader %s %s",
            parameterType, parameter.getSimpleName().toString());
      }

      if (parameterType instanceof DeclaredType declaredReturnType) {
        final TypeMirror listDeclaredType = declaredReturnType.getTypeArguments().get(0);

        if (annotation.required()) {
          return CodeBlock.of(
              CODE_FOR_GET_LIST_HEADER_REQUIRED,
              parameterType,
              varName,
              annotation.value(),
              defaultValue,
              listDeclaredType,
              varName,
              String.format(
                  "@RequestHeader %s %s is marked as required but was not present on request. "
                      + "Default value was not set.",
                  parameter.asType().toString(), parameter.getSimpleName().toString()));
        } else {
          return CodeBlock.of(
              CODE_FOR_GET_LIST_HEADER,
              parameterType,
              varName,
              annotation.value(),
              defaultValue,
              listDeclaredType);
        }
      } else {
        throw new WebSocketResolverException(
            "Cannot process @RequestHeader %s %s parameter: bad return type: %s",
            parameter.asType().toString(), parameter.getSimpleName().toString(), parameterType);
      }
    } else {
      if (annotation.value().isEmpty()) {
        throw new WebSocketResolverException(
            "Value cannot be empty at @RequestHeader %s %s",
            parameterType, parameter.getSimpleName().toString());
      }

      if (annotation.required()) {
        return CodeBlock.of(
            CODE_FOR_GET_SINGLE_HEADER_REQUIRED,
            parameterType,
            varName,
            annotation.value(),
            defaultValue,
            parameterType,
            String.format(
                "@RequestHeader %s %s is marked as required but was not present on request. "
                    + "Default value was not set.",
                parameter.asType().toString(), parameter.getSimpleName().toString()));
      } else {
        return CodeBlock.of(
            CODE_FOR_GET_SINGLE_HEADER,
            parameterType,
            varName,
            annotation.value(),
            defaultValue,
            parameterType,
            parameterType);
      }
    }
  }

  private boolean isMultimapParamValid(final TypeMirror type) {
    if (type instanceof DeclaredType declaredReturnType) {
      final TypeMirror keyType = declaredReturnType.getTypeArguments().get(0);
      final TypeMirror valueType = declaredReturnType.getTypeArguments().get(1);

      final TypeMirror stringType =
          this.getElements().getTypeElement(String.class.getCanonicalName()).asType();

      return this.getTypes().isSameType(stringType, keyType)
          && this.getTypes().isSameType(valueType, stringType);
    } else {
      throw new WebSocketResolverException(
          "Cannot process @RequestHeader parameter: bad return type: %s", type.toString());
    }
  }

  @Override
  Class<RequestHeader> getAnnotationType() {
    return RequestHeader.class;
  }
}
