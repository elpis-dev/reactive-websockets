package io.github.elpis.reactive.websockets.processor.exception;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Compile-time model for an exception handler method discovered in a {@code @MessageEndpoint} or
 * {@code @WebSocketAdvice} class.
 *
 * <p>This record contains all necessary metadata to generate exception handling code during
 * annotation processing. It includes the handler method element, exception types it handles, return
 * type information, and whether it's a local or global handler.
 *
 * @param method The exception handler method element
 * @param exceptionTypes List of exception types this handler can handle
 * @param returnsVoid Whether the method returns void (no response sent to client)
 * @param returnType The return type mirror for non-void handlers
 * @param isLocal true if handler is in @MessageEndpoint (local), false if in @WebSocketAdvice
 *     (global)
 * @param beanClass The @WebSocketAdvice class containing this handler (null for local handlers)
 * @since 1.0.0
 */
public record ExceptionHandlerInfo(
    ExecutableElement method,
    List<TypeMirror> exceptionTypes,
    boolean returnsVoid,
    TypeMirror returnType,
    boolean isLocal,
    TypeElement beanClass) {

  /**
   * Creates an ExceptionHandlerInfo with validation.
   *
   * @throws IllegalArgumentException if method is null or exceptionTypes is empty
   */
  public ExceptionHandlerInfo {
    if (method == null) {
      throw new IllegalArgumentException("Method cannot be null");
    }
    if (exceptionTypes == null || exceptionTypes.isEmpty()) {
      throw new IllegalArgumentException("Exception types cannot be null or empty");
    }

    exceptionTypes = List.copyOf(exceptionTypes);
  }

  /**
   * Gets the simple name of the handler method.
   *
   * @return the method name
   */
  public String getMethodName() {
    return method.getSimpleName().toString();
  }

  /**
   * Gets the field name for autowired global handler bean. Returns null for local handlers.
   *
   * @return field name like "globalErrorHandler" or null
   */
  public String getBeanFieldName() {
    if (isLocal || beanClass == null) {
      return null;
    }
    String className = beanClass.getSimpleName().toString();
    return Character.toLowerCase(className.charAt(0)) + className.substring(1);
  }
}
