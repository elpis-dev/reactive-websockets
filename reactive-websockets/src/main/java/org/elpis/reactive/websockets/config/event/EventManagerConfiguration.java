package org.elpis.reactive.websockets.config.event;

import org.elpis.reactive.websockets.event.manager.EventManagers;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of all system-supported {@link WebSocketEventManager}.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @see WebSocketEventManager
 * @since 0.1.0
 */
@Configuration
public class EventManagerConfiguration {

    /**
     * {@link WebSocketEventManager} to observe {@link SessionConnectedEvent}.
     *
     * @author Alex Zharkov
     */
    @Bean
    public WebSocketEventManager<SessionConnectedEvent> connectedEventWebSocketEventManager() {
        return EventManagers.multicast();
    }

    /**
     * {@link WebSocketEventManager} to observe {@link ClientSessionClosedEvent}.
     *
     * @author Alex Zharkov
     */
    @Bean
    public WebSocketEventManager<ClientSessionClosedEvent> clientClosedEventWebSocketEventManager() {
        return EventManagers.multicast(WebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
    }

}
