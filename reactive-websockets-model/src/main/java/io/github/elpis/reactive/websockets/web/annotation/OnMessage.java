package io.github.elpis.reactive.websockets.web.annotation;

import io.github.elpis.reactive.websockets.config.Mode;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

  String value();

  /**
   * The messaging mode for this WebSocket connection. Defaults to {@link Mode#BROADCAST}.
   *
   * @since 1.0.0
   */
  Mode mode() default Mode.BROADCAST;
}
