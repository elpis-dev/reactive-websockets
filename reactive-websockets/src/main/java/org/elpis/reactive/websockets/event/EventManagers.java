package org.elpis.reactive.websockets.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elpis.reactive.websockets.event.impl.ReplayManyEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventManagers {
    public static <T extends WebSocketEvent<?>> ReplayManyEventManager<T> replayMany() {
        return new ReplayManyEventManager<>() {
        };
    }
}
