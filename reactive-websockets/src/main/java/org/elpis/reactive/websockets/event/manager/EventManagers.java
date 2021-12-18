package org.elpis.reactive.websockets.event.manager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elpis.reactive.websockets.event.manager.impl.MulticastEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Utils class with some pre-implemented {@link WebSocketEventManager}.
 * @author Alex Zharkov
 * @see WebSocketEvent
 * @see WebSocketEventManager
 * @since 0.1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventManagers {

    /**
     * Creates default {@link MulticastEventManager} with default queue size of {@link reactor.util.concurrent.Queues#SMALL_BUFFER_SIZE}.
     * @return {@link MulticastEventManager}
     * @since 0.1.0
     */
    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast() {
        return new MulticastEventManager<>() {
        };
    }

    /**
     * Creates default {@link MulticastEventManager} with custom queue size.
     * @return {@link MulticastEventManager}
     * @since 0.1.0
     */
    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast(final int eventQueueSize) {
        return new MulticastEventManager<>(eventQueueSize) {
        };
    }
}
