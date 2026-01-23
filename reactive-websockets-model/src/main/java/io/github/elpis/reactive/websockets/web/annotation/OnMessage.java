package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a WebSocket message handler within a {@link MessageEndpoint}.
 *
 * <p>The annotated method receives incoming WebSocket messages and can return responses. The method
 * must return a {@code Publisher} (Flux/Mono) or void.
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>{@code
 * // Echo handler - returns Flux of responses
 * @OnMessage("/echo")
 * public Flux<String> echo(Flux<WebSocketMessage> messages) {
 *     return messages.map(msg -> "Echo: " + msg.getPayloadAsText());
 * }
 *
 * // Handler with path variables
 * @OnMessage("/room/{roomId}")
 * public Flux<ChatMessage> handleRoom(@PathVariable String roomId,
 *                                      Flux<WebSocketMessage> messages) {
 *     return messages.map(msg -> processMessage(roomId, msg));
 * }
 *
 * // Void handler - process without response
 * @OnMessage("/events")
 * public void logEvents(Flux<WebSocketMessage> messages) {
 *     messages.subscribe(msg -> log.info("Event: {}", msg.getPayloadAsText()));
 * }
 * }</pre>
 *
 * @see MessageEndpoint
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

  /**
   * The path pattern for this message handler. Supports path variables like {@code /room/{roomId}}.
   */
  String value();
}
