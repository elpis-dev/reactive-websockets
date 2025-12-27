package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Indicates that an annotated class is a WebSocket advice component containing
 * {@code @ExceptionHandler} methods for handling exceptions thrown during WebSocket message
 * processing.
 *
 * <p>This annotation is analogous to Spring's {@code @ControllerAdvice} but specifically designed
 * for reactive WebSocket handlers. Classes annotated with {@code @WebSocketAdvice} are
 * automatically discovered and registered during application startup.
 *
 * <p>Exception handlers defined in {@code @WebSocketAdvice} classes serve as global handlers and
 * have lower priority than local handlers defined within {@code @MessageEndpoint} classes.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * @WebSocketAdvice
 * public class GlobalErrorHandler {
 *
 *     @ExceptionHandler
 *     public Mono<ErrorResponse> handleValidation(ValidationException ex) {
 *         return Mono.just(new ErrorResponse("Validation failed", ex.getMessage()));
 *     }
 *
 *     @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
 *     public Mono<String> handleIllegalOperations(RuntimeException ex) {
 *         return Mono.just("Invalid operation: " + ex.getMessage());
 *     }
 * }
 * }</pre>
 *
 * <p>This annotation is meta-annotated with {@code @Component}, making it automatically eligible
 * for component scanning and dependency injection.
 *
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 * @see MessageEndpoint
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface WebSocketAdvice {

  /**
   * The value may indicate a suggestion for a logical component name, to be turned into a Spring
   * bean in case of an autodetected component.
   *
   * @return the suggested component name, if any (or empty String otherwise)
   */
  @AliasFor(annotation = Component.class)
  String value() default "";
}
