package io.github.elpis.reactive.websockets.web.annotation;

import io.github.elpis.reactive.websockets.config.Mode;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMessage {

    String value();

    /**
     * @deprecated Mode is deprecated and will default to SHARED.
     * It may be removed in future versions.
     */
    @Deprecated
    Mode mode() default Mode.BROADCAST;

    /**
     * Heartbeat configuration for this specific WebSocket connection.
     * Takes precedence over {@link MessageEndpoint#heartbeat()} if specified.
     *
     * @since 1.0.0
     */
    Heartbeat heartbeat() default @Heartbeat(enabled = false);
}
