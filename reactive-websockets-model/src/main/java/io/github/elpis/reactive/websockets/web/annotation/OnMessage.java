package io.github.elpis.reactive.websockets.web.annotation;

import io.github.elpis.reactive.websockets.config.Mode;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

    String value();

    /**
     * The messaging mode for this WebSocket connection.
     * Defaults to {@link Mode#BROADCAST}.
     *
     * @since 1.0.0
     */
    Mode mode() default Mode.BROADCAST;

    /**
     * Heartbeat configuration for this specific WebSocket connection.
     * Takes precedence over {@link MessageEndpoint#heartbeat()} if specified.
     *
     * @since 1.0.0
     */
    Heartbeat heartbeat() default @Heartbeat(enabled = false);
}
