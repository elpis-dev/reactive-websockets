package io.github.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketResolverException;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.bind.annotation.SessionAttribute;

public class SessionResolver extends SocketApiAnnotationResolver<SessionAttribute> {
  private static final String GET_SESSION_OPTIONAL =
      """
        final java.util.Optional<io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession> $L = this.getSessionRegistry().get(context.getSessionId());
        """;

  private static final String GET_SESSION_REQUIRED =
      """
        final io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession $L = this.getSessionRegistry().get(context.getSessionId())
        .orElseThrow(() -> new io.github.elpis.reactive.websockets.exception.WebSocketProcessingException("Cannot find session with id %s", context.getSessionId()));
        """;

  private static final String GET_SESSION_NOT_REQUIRED =
      """
        final io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession $L = this.getSessionRegistry().get(context.getSessionId())
        .orElse(null);
        """;

  SessionResolver(Elements elements, Types types) {
    super(elements, types);
  }

  @Override
  public CodeBlock resolve(final VariableElement parameter) {
    final TypeMirror parameterType = parameter.asType();
    final SessionAttribute sessionAttribute = parameter.getAnnotation(SessionAttribute.class);

    final Element optionalType =
        this.getElements().getTypeElement(Optional.class.getCanonicalName());
    final TypeMirror sessionType =
        this.getElements()
            .getTypeElement(ReactiveWebSocketSession.class.getCanonicalName())
            .asType();

    if (this.getTypes()
        .isSameType(
            this.getTypes().erasure(parameterType),
            this.getTypes().erasure(optionalType.asType()))) {

      if (!this.isSessionParamValid(parameterType)) {
        throw new WebSocketResolverException(
            "Cannot process @SessionAttribute parameter: bad return type: %s", parameterType);
      }

      return CodeBlock.of(GET_SESSION_OPTIONAL, parameter.getSimpleName());
    } else if (this.getTypes().isSameType(parameterType, sessionType)) {
      return sessionAttribute.required()
          ? CodeBlock.of(GET_SESSION_REQUIRED, parameter.getSimpleName())
          : CodeBlock.of(GET_SESSION_NOT_REQUIRED, parameter.getSimpleName());
    } else {
      throw new WebSocketResolverException(
          "Only 'ReactiveWebSocketSession' type is supported for "
              + "@SessionAttribute. Found '%s'",
          parameterType.toString());
    }
  }

  private boolean isSessionParamValid(final TypeMirror type) {
    if (type instanceof DeclaredType declaredReturnType) {
      final TypeMirror declaredType = declaredReturnType.getTypeArguments().get(0);

      final TypeMirror sessionType =
          this.getElements()
              .getTypeElement(ReactiveWebSocketSession.class.getCanonicalName())
              .asType();

      return this.getTypes().isSameType(declaredType, sessionType);
    } else {
      throw new WebSocketResolverException(
          "Cannot process @SessionAttribute parameter: bad return type: %s", type.toString());
    }
  }

  @Override
  Class<SessionAttribute> getAnnotationType() {
    return SessionAttribute.class;
  }
}
