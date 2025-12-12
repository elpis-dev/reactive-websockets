package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Heartbeat configuration for WebSocket connections. Defines ping/pong interval and timeout
 * settings.
 *
 * <p>Can be applied at:
 *
 * <ul>
 *   <li>Class level - applies to all methods in the @MessageEndpoint
 *   <li>Method level - applies to specific @OnMessage method, overrides class-level
 * </ul>
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
