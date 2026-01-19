package io.github.elpis.reactive.websockets.context.resolver;

import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;

public interface ReactiveWebSocketMethodParameterResolver {
  boolean supports(final MethodParameter parameter);

  Object resolve(final MethodParameter parameter, final WebSocketSessionContext context);

  default void preRegister(final MethodParameter parameter) {
    // no-op
  }

  default String resolveDefaultValue(final String defaultValue) {
    return Optional.ofNullable(defaultValue)
        .filter(v -> !v.isEmpty())
        .filter(v -> !ValueConstants.DEFAULT_NONE.equals(v))
        .orElse(null);
  }
}
