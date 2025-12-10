package io.github.elpis.reactive.websockets.config.event;

import io.github.elpis.reactive.websockets.event.manager.EventManagers;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.ServerSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of all system-supported {@link WebSocketEventManager}.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @see WebSocketEventManager
 * @since 1.0.0
 */
@Configuration
public class EventManagerConfiguration {

  /** {@link WebSocketEventManager} to observe {@link SessionConnectedEvent}. */
  @Bean
  public WebSocketEventManager<SessionConnectedEvent> connectedEventWebSocketEventManager() {
    return EventManagers.multicast();
  }

  /** {@link WebSocketEventManager} to observe {@link ClientSessionClosedEvent}. */
  @Bean
  public WebSocketEventManager<ClientSessionClosedEvent> clientClosedEventWebSocketEventManager() {
    return EventManagers.multicast(WebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
  }

  /** {@link WebSocketEventManager} to observe {@link ServerSessionClosedEvent}. */
  @Bean
  public WebSocketEventManager<ServerSessionClosedEvent> serverClosedEventWebSocketEventManager() {
    return EventManagers.multicast(WebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
  }

  @Bean
  public WebSocketEventManagerFactory eventManagerFactory() {
    return WebSocketEventManagerFactory.builder()
        .register(SessionConnectedEvent.class, connectedEventWebSocketEventManager())
        .register(ClientSessionClosedEvent.class, clientClosedEventWebSocketEventManager())
        .register(ServerSessionClosedEvent.class, serverClosedEventWebSocketEventManager())
        .build();
  }
}
