package io.github.elpis.reactive.websockets.processor.resolver;

import static io.github.elpis.reactive.websockets.processor.util.Constants.VARIABLE_SUFFIX;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketResolverException;
import io.github.elpis.reactive.websockets.processor.util.TypeCategoryResolver;
import io.github.elpis.reactive.websockets.processor.util.TypeCategoryResolver.TypeCategory;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestBodyResolver extends SocketApiAnnotationResolver<RequestBody> {

  /**
   * Enum to represent the publisher type.
   *
   * @since 1.0.0
   */
  private enum PublisherType {
    FLUX,
    MONO
  }

  RequestBodyResolver(Elements elements, Types types) {
    super(elements, types);
  }

  @Override
  public CodeBlock resolve(final VariableElement parameter) {
    final TypeMirror parameterType = parameter.asType();

    // Extract publisher type (Flux or Mono)
    final PublisherType publisherType = extractPublisherType(parameterType);

    // Extract generic type T from Flux<T> or Mono<T>
    final TypeMirror genericType = extractGenericType(parameterType);

    // Categorize the type
    final TypeCategory category =
        TypeCategoryResolver.categorize(genericType, getElements(), getTypes());

    // Generate appropriate code based on category
    return switch (category) {
      case WEBSOCKET_MESSAGE -> generateWebSocketMessageCode(parameter, publisherType);
      case SIMPLE_TYPE -> generateSimpleTypeCode(parameter, publisherType, genericType);
      case COMPLEX_TYPE -> generateComplexTypeCode(parameter, publisherType, genericType);
    };
  }

  /**
   * Extracts the publisher type (Flux or Mono) from the parameter type.
   *
   * @param parameterType the parameter type
   * @return the publisher type
   * @throws WebSocketResolverException if the parameter is not Flux or Mono
   * @since 1.0.0
   */
  private PublisherType extractPublisherType(final TypeMirror parameterType) {
    final Element fluxType = getElements().getTypeElement(Flux.class.getCanonicalName());
    final Element monoType = getElements().getTypeElement(Mono.class.getCanonicalName());

    final TypeMirror erasedType = getTypes().erasure(parameterType);

    if (getTypes().isSameType(erasedType, getTypes().erasure(fluxType.asType()))) {
      return PublisherType.FLUX;
    } else if (getTypes().isSameType(erasedType, getTypes().erasure(monoType.asType()))) {
      return PublisherType.MONO;
    }

    throw new WebSocketResolverException(
        "@RequestBody must be used with Flux or Mono type. Found: %s", parameterType);
  }

  /**
   * Extracts the generic type T from Flux&lt;T&gt; or Mono&lt;T&gt;.
   *
   * @param parameterType the parameter type
   * @return the generic type
   * @throws WebSocketResolverException if the generic type is missing
   * @since 1.0.0
   */
  private TypeMirror extractGenericType(final TypeMirror parameterType) {
    if (parameterType instanceof DeclaredType declaredType) {
      final List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

      if (typeArguments.isEmpty()) {
        throw new WebSocketResolverException(
            "@RequestBody Flux/Mono must have a generic type parameter. Found raw type: %s",
            parameterType);
      }

      return typeArguments.get(0);
    }

    throw new WebSocketResolverException(
        "@RequestBody parameter has unexpected type: %s", parameterType);
  }

  /**
   * Generates code for WebSocketMessage (backward compatibility).
   *
   * @param parameter the parameter element
   * @param publisherType the publisher type
   * @return the generated code block
   * @since 1.0.0
   */
  private CodeBlock generateWebSocketMessageCode(
      final VariableElement parameter, final PublisherType publisherType) {
    // For backward compatibility with Flux<WebSocketMessage>, just pass through
    // For Mono<WebSocketMessage>, we need to convert from Flux to Mono
    final String varName = parameter.getSimpleName().toString() + VARIABLE_SUFFIX;
    if (publisherType == PublisherType.FLUX) {
      return CodeBlock.of("final $T $L = messages;\n", parameter.asType(), varName);
    } else {
      return CodeBlock.builder()
          .add("final $T $L = messages.next();\n", parameter.asType(), varName)
          .build();
    }
  }

  /**
   * Generates code for simple types using TypeUtils.
   *
   * @param parameter the parameter element
   * @param publisherType the publisher type
   * @param genericType the generic type
   * @return the generated code block
   * @since 1.0.0
   */
  private CodeBlock generateSimpleTypeCode(
      final VariableElement parameter,
      final PublisherType publisherType,
      final TypeMirror genericType) {
    return generateMappingCode(
        parameter,
        publisherType,
        genericType,
        "io.github.elpis.reactive.websockets.util.TypeUtils.convert(text, $T.class)");
  }

  /**
   * Generates code for complex types using JsonMapper.
   *
   * @param parameter the parameter element
   * @param publisherType the publisher type
   * @param genericType the generic type
   * @return the generated code block
   * @since 1.0.0
   */
  private CodeBlock generateComplexTypeCode(
      final VariableElement parameter,
      final PublisherType publisherType,
      final TypeMirror genericType) {
    return generateMappingCode(
        parameter,
        publisherType,
        genericType,
        "io.github.elpis.reactive.websockets.mapper.JsonMapper.deserialize(text, $T.class)");
  }

  /**
   * Generates mapping code for both Flux and Mono publishers.
   *
   * @param parameter the parameter element
   * @param publisherType the publisher type (Flux or Mono)
   * @param genericType the generic type
   * @param mapperExpression the mapper expression to use (e.g., TypeUtils.convert or
   *     JsonMapper.deserialize)
   * @return the generated code block
   * @since 1.0.0
   */
  private CodeBlock generateMappingCode(
      final VariableElement parameter,
      final PublisherType publisherType,
      final TypeMirror genericType,
      final String mapperExpression) {
    final TypeName genericTypeName = TypeName.get(genericType);
    final String paramName = parameter.getSimpleName().toString() + VARIABLE_SUFFIX;
    final String sourceExpression =
        publisherType == PublisherType.FLUX ? "messages" : "messages.next()";

    return CodeBlock.builder()
        .add("final $T $L = $L\n", parameter.asType(), paramName, sourceExpression)
        .indent()
        .add(".map(org.springframework.web.reactive.socket.WebSocketMessage::getPayloadAsText)\n")
        .add(".map(text -> $L);\n", CodeBlock.of(mapperExpression, genericTypeName))
        .unindent()
        .build();
  }

  @Override
  Class<RequestBody> getAnnotationType() {
    return RequestBody.class;
  }
}
