package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.*;

/**
 * Heartbeat configuration for WebSocket connections.
 * Defines ping/pong interval and timeout settings.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Heartbeat {

    /**
     * Interval between heartbeat pings in seconds.
     * @return the interval in seconds
     */
    long interval() default 30;

    /**
     * Timeout for heartbeat response in seconds.
     * @return the timeout in seconds
     */
    long timeout() default 60;

    /**
     * Whether heartbeat is enabled.
     * @return true if enabled, false otherwise
     */
    boolean enabled() default true;
}

