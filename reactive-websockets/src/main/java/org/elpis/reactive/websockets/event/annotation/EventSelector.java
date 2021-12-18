package org.elpis.reactive.websockets.event.annotation;

import java.lang.annotation.*;

/**
 * Applies additional filtering on {@link org.elpis.reactive.websockets.event.model.WebSocketEvent WebSocketEvent} selection.

 * @author Alex Zharkov
 * @since 0.1.0
 * @see org.elpis.reactive.websockets.event.model.WebSocketEvent
 * @see org.elpis.reactive.websockets.event.EventSelectorProcessor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSelector {

    /**
     * Declares a SpeL expression that can process any {@link org.elpis.reactive.websockets.event.model.WebSocketEvent WebSocketEvent} from context.
     * <p>
     * E.x. {@code &#064;EventSelector("sessionInfo.id eq '12345'")}
     * </p>
     *
     * @since 0.1.0
     */
    String value();
}
