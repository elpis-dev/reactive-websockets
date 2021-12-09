package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.event.EventManagers;
import org.elpis.reactive.websockets.event.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EventManagerConfiguration {

    @Bean
    public WebSocketEventManager<SessionConnectedEvent> connectedEventWebSocketEventManager() {
        return EventManagers.replayMany();
    }

    @Bean
    public WebSocketEventManager<ClientSessionClosedEvent> clientClosedEventWebSocketEventManager() {
        return EventManagers.replayMany();
    }

}