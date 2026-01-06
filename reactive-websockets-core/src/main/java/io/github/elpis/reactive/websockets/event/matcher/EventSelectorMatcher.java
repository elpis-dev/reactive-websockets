package io.github.elpis.reactive.websockets.event.matcher;

import io.github.elpis.reactive.websockets.event.EventSelectorProcessor;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Extension of {@link EventSelectorProcessor} with {@link Boolean} result returned by default.
 *
 * @author Phillip J. Fry
 * @see EventSelectorProcessor
 * @see WebSocketEvent
 * @see EventSelector
 * @since 1.0.0
 */
public interface EventSelectorMatcher<E extends WebSocketEvent<?>>
    extends EventSelectorProcessor<E, Boolean> {

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
   * Default implementation of {@link EventSelectorProcessor#process(WebSocketEvent, EventSelector)}
   * returning result of {@link #matches(WebSocketEvent, EventSelector)}.
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
