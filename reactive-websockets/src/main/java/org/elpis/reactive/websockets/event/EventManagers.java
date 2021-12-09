package org.elpis.reactive.websockets.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elpis.reactive.websockets.event.impl.MulticastEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventManagers {
    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast() {
        return new MulticastEventManager<>() {
        };
    }

    public static <T extends WebSocketEvent<?>> MulticastEventManager<T> multicast(final int eventQueueSize) {
        return new MulticastEventManager<>(eventQueueSize) {
        };
    }
}
