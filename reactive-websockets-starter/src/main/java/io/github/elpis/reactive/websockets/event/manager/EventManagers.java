package io.github.elpis.reactive.websockets.event.manager;

import io.github.elpis.reactive.websockets.event.manager.impl.MulticastEventManager;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Utils class with some pre-implemented {@link WebSocketEventManager}.
 *
 * @author Phillip J. Fry
 * @see WebSocketEvent
 * @see WebSocketEventManager
 * @since 1.0.0
 */
public class EventManagers {
    private EventManagers() {
    }

    /**
     * Creates default {@link MulticastEventManager} with default queue size of {@link reactor.util.concurrent.Queues#SMALL_BUFFER_SIZE}.
     *
     * @return {@link MulticastEventManager}
     * @since 1.0.0
     */
    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast() {
        return new MulticastEventManager<>() {
        };
    }

    /**
     * Creates default {@link MulticastEventManager} with custom queue size.
     *
     * @return {@link MulticastEventManager}
     * @since 1.0.0
     */
    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast(final int eventQueueSize) {
        return new MulticastEventManager<>(eventQueueSize) {
        };
    }
}
