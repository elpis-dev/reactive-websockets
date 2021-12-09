package org.elpis.reactive.websockets.config.event;

import org.elpis.reactive.websockets.event.EventManagers;
import org.elpis.reactive.websockets.event.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventManagerConfiguration {

    @Bean
    public WebSocketEventManager<SessionConnectedEvent> connectedEventWebSocketEventManager() {
        return EventManagers.multicast();
    }

    @Bean
    public WebSocketEventManager<ClientSessionClosedEvent> clientClosedEventWebSocketEventManager() {
        return EventManagers.multicast(WebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
    }

}