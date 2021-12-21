package org.elpis.reactive.websockets.event.model;

/**
 * Contract for all system and custom websocket events. Could contain any event payload.
 *
 * @author Alex Zharkov
 * @since 0.1.0
 */
public interface WebSocketEvent<T> {

    /**
     * Abstract {@code get} method to return a payload from event.
     *
     * @since 0.1.0
     */
    T payload();
}
