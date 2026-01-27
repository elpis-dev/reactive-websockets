package io.github.elpis.reactive.websockets.context.resolver;

import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;

/**
 * Interface for resolving method parameters in reactive WebSocket handlers.
 *
 * <p>Implementations of this interface are responsible for determining whether they can resolve a
 * specific method parameter and providing the actual resolution logic. This follows a similar
 * pattern to Spring's {@code HandlerMethodArgumentResolver} but adapted for reactive WebSocket
 * contexts.
 *
 * <p>The typical resolution flow is:
 *
 * <ol>
 *   <li>Check if the resolver supports the parameter via {@link #supports(MethodParameter)}
 *   <li>Optionally validate the parameter during registration via {@link
 *       #preRegister(MethodParameter)}
 *   <li>Resolve the actual parameter value via {@link #resolve(MethodParameter,
 *       WebSocketSessionContext)}
 * </ol>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 * @see MethodParameter
 * @see WebSocketSessionContext
 */
public interface ReactiveWebSocketMethodParameterResolver {

  /**
   * Determines whether this resolver supports the given method parameter.
   *
   * <p>This method is called to check if the resolver can handle the parameter, typically based on
   * the parameter's type, annotations, or other characteristics.
   *
   * @param parameter the method parameter to check
   * @return {@code true} if this resolver can resolve the parameter, {@code false} otherwise
   */
  boolean supports(final MethodParameter parameter);

  /**
   * Resolves the method parameter to a concrete value using the provided WebSocket session context.
   *
   * <p>This method is only called if {@link #supports(MethodParameter)} returns {@code true} for
   * the given parameter.
   *
   * @param parameter the method parameter to resolve
   * @param context the WebSocket session context containing request and session information
   * @return the resolved parameter value, or {@code null} if the value cannot be resolved
   * @throws IllegalStateException if the parameter cannot be resolved and null is not acceptable
   */
  Object resolve(final MethodParameter parameter, final WebSocketSessionContext context);

  /**
   * Pre-registration validation hook called when the handler method is registered.
   *
   * <p>This method allows resolvers to validate parameter configurations at application startup
   * rather than at runtime. For example, checking that required annotations are properly configured
   * or that parameter types are valid.
   *
   * <p>The default implementation is a no-op.
   *
   * @param parameter the method parameter to validate
   * @throws IllegalStateException if the parameter configuration is invalid
   */
  default void preRegister(final MethodParameter parameter) {
    // no-op
  }

  /**
   * Resolves a default value string, filtering out empty values and Spring's {@link
   * ValueConstants#DEFAULT_NONE} marker.
   *
   * <p>This utility method is commonly used by resolvers that support default values specified in
   * annotations.
   *
   * @param defaultValue the default value string to resolve
   * @return the resolved default value, or {@code null} if the value is empty or equal to {@link
   *     ValueConstants#DEFAULT_NONE}
   */
  default String resolveDefaultValue(final String defaultValue) {
    return Optional.ofNullable(defaultValue)
        .filter(v -> !v.isEmpty())
        .filter(v -> !ValueConstants.DEFAULT_NONE.equals(v))
        .orElse(null);
  }
}
