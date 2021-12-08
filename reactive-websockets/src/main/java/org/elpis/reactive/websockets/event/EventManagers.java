package org.elpis.reactive.websockets.event;

import org.elpis.reactive.websockets.event.impl.ReplayManyEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

public class EventManagers {
    public static <T extends WebSocketEvent<?>> ReplayManyEventManager<T> replayMany() {
        return new ReplayManyEventManager<>() {
        };
    }
}
