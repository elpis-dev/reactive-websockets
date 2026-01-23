package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Marks a class as a WebSocket message endpoint controller.
 *
 * <p>This annotation works similarly to {@link Controller @Controller} but is designed for
 * WebSocket communication. Combine with {@link OnMessage @OnMessage} to define message handlers.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * @MessageEndpoint("/chat")
 * public class ChatEndpoint {
 *
 *     @OnMessage("/room/{roomId}")
 *     public Flux<String> handleMessage(@PathVariable String roomId,
 *                                        Flux<WebSocketMessage> messages) {
 *         return messages.map(msg -> "Echo: " + msg.getPayloadAsText());
 *     }
 *
 *     @ExceptionHandler(ValidationException.class)
 *     public Mono<ErrorResponse> handleValidation(ValidationException ex) {
 *         return Mono.just(new ErrorResponse(400, ex.getMessage()));
 *     }
 * }
 * }</pre>
 *
 * @author Phillip J. Fry
 * @see OnMessage
 * @see Heartbeat
 * @see RateLimit
 * @see Backpressure
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Controller
@Documented
@Inherited
public @interface MessageEndpoint {

  /**
   * The base websocket path mapping (e.g. {@code "/home"}) for all the included {@link
   * OnMessage @SocketMapping} annotated methods. for all the {@link OnMessage @SocketMapping}
   * annotated methods - the final path would be resulted to concatenation of {@link
   * MessageEndpoint @MessageEndpoint.value} and {@link OnMessage#value() @SocketMapping.value}:
   *
   * @since 1.0.0
   */
  @AliasFor(annotation = Component.class)
  String value() default "";
}
