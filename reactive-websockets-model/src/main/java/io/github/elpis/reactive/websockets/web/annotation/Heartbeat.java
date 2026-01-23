package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures heartbeat (ping/pong) for WebSocket connections.
 *
 * <p>Heartbeats keep the connection alive and detect stale connections.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Class-level: applies to all handlers
 * @MessageEndpoint("/ws")
 * @Heartbeat(interval = 30, timeout = 60)
 * public class ChatEndpoint {
 *     @OnMessage("/chat")
 *     public Flux<String> chat(Flux<WebSocketMessage> messages) { ... }
 * }
 *
 * // Method-level: overrides class-level
 * @MessageEndpoint("/ws")
 * @Heartbeat(interval = 60)
 * public class MixedEndpoint {
 *     @OnMessage("/fast")
 *     @Heartbeat(interval = 10)  // More frequent for this handler
 *     public Flux<String> fastPing(Flux<WebSocketMessage> messages) { ... }
 *
 *     @OnMessage("/slow")
 *     @Heartbeat(enabled = false)  // Disable for this handler
 *     public Flux<String> noPing(Flux<WebSocketMessage> messages) { ... }
 * }
 * }</pre>
 *
 * <p>Precedence (highest to lowest):
 *
 * <ol>
 *   <li>@Heartbeat on method
 *   <li>@Heartbeat on class
 *   <li>Disabled (default)
 * </ol>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Heartbeat {

  /**
   * Interval between heartbeat pings in seconds.
   *
   * @return the interval in seconds
   */
  long interval() default 30;

  /**
   * Timeout for heartbeat response in seconds.
   *
   * @return the timeout in seconds
   */
  long timeout() default 60;

  /**
   * Whether heartbeat is enabled.
   *
   * @return true if enabled, false otherwise
   */
  boolean enabled() default true;
}
