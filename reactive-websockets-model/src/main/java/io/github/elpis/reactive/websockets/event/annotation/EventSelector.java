package io.github.elpis.reactive.websockets.event.annotation;

import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

import java.lang.annotation.*;

/**
 * Applies additional filtering on {@link WebSocketEvent WebSocketEvent} selection.
 *
 * @author Phillip J. Fry
 * @see WebSocketEvent
 * @see io.github.elpis.reactive.websockets.event.EventSelectorProcessor
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSelector {

    /**
     * Declares a SpeL expression that can process any {@link WebSocketEvent WebSocketEvent} from context.
     * <p>
     * E.x. {@code &#064;EventSelector("sessionInfo.id eq '12345'")}
     * </p>
     *
     * @since 1.0.0
     */
    String value();
}
