package org.elpis.reactive.websockets.event.annotation;

import org.elpis.reactive.websockets.config.model.WebSocketCloseStatus;

import java.lang.annotation.*;

/**
 * Indicates which close code should be handled by annotated method.
 *
 * <p>Annotation works in a similar way as {@link org.springframework.web.bind.annotation.ExceptionHandler @ExceptionHandler}.
 * Could be combined with {@link org.elpis.reactive.websockets.event.annotation.EventSelector @EventSelector} annotation to add additional filters on close event hit.
 *
 * @author Alex Zharkov
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 * @see org.elpis.reactive.websockets.event.annotation.EventSelector
 * @see org.springframework.web.reactive.socket.CloseStatus
 * @see WebSocketCloseStatus
 * @since 0.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionCloseStatus {

    /**
     * Standard status codes covered by the annotated method. If empty, will default to {@link WebSocketCloseStatus#ALL WebSocketCloseStatus.ALL} that would omit all occurred close events.
     *
     * @since 0.1.0
     */
    WebSocketCloseStatus[] value() default {};

    /**
     * Standard and custom status codes covered by the annotated method. If empty, will default to {@link WebSocketCloseStatus#ALL WebSocketCloseStatus.ALL} that would omit all occurred close events.
     * <p>Setting this value will discard {@link #value() @SessionCloseStatus.value}.
     *
     * <p><strong>NOTE: </strong> Allowed values for this value are in range from 1000 to 4999. Other values will trigger throwing a validation exception.
     *
     * @since 0.1.0
     */
    int[] code() default {};
}
