package io.github.elpis.reactive.websockets.event;

import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Interface used by implementing class to process system or custom {@link WebSocketEvent} on method annotated with {@link EventSelector @EventSelector}.
 *
 * @author Alex Zharkov
 * @see WebSocketEvent
 * @see EventSelector
 * @since 0.1.0
 */
public interface EventSelectorProcessor<E extends WebSocketEvent<?>, R> {

    /**
     * Takes an event and {@link EventSelector @EventSelector} and processes them to any result.
     *
     * @param event         any WebSocketEvent implementation
     * @param eventSelector EventSelector annotation from annotated method
     * @return any custom result to return
     * @since 0.1.0
     */
    R process(E event, EventSelector eventSelector);
}
