package io.github.elpis.reactive.websockets.event.matcher;

import io.github.elpis.reactive.websockets.event.ReactiveWebSocketEventSelectorProcessor;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Extension of {@link ReactiveWebSocketEventSelectorProcessor} with {@link Boolean} result returned
 * by default.
 *
 * @author Phillip J. Fry
 * @see ReactiveWebSocketEventSelectorProcessor
 * @see WebSocketEvent
 * @see EventSelector
 * @since 1.0.0
 */
public interface ReactiveWebSocketEventSelectorMatcher<E extends WebSocketEvent<?>>
    extends ReactiveWebSocketEventSelectorProcessor<E, Boolean> {

  /**
   * Takes an event and {@link EventSelector @EventSelector} and matches them to boolean result.
   *
   * @param event any WebSocketEvent implementation
   * @param eventSelector EventSelector annotation from annotated method
   * @return boolean signaling about match success/failure
   * @since 1.0.0
   */
  boolean matches(E event, EventSelector eventSelector);

  /**
   * Default implementation of {@link
   * ReactiveWebSocketEventSelectorProcessor#process(WebSocketEvent, EventSelector)} returning
   * result of {@link #matches(WebSocketEvent, EventSelector)}.
   *
   * @param event any WebSocketEvent implementation
   * @param eventSelector EventSelector annotation from annotated method
   * @return boolean signaling about match success/failure
   * @since 1.0.0
   */
  default Boolean process(E event, EventSelector eventSelector) {
    return matches(event, eventSelector);
  }
}
