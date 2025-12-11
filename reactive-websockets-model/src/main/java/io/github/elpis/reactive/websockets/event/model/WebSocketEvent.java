package io.github.elpis.reactive.websockets.event.model;

/**
 * Contract for all system and custom websocket events. Could contain any event payload.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public interface WebSocketEvent<T> {

  /**
   * Abstract {@code get} method to return a payload from event.
   *
   * @since 1.0.0
   */
  T payload();
}
