package io.github.elpis.reactive.websockets.config.event;

import io.github.elpis.reactive.websockets.event.manager.ReactiveEventManagers;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManager;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.ServerSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of all system-supported {@link ReactiveWebSocketEventManager}.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @see ReactiveWebSocketEventManager
 * @since 1.0.0
 */
@Configuration
public class ReactiveEventManagerConfiguration {

  /** {@link ReactiveWebSocketEventManager} to observe {@link SessionConnectedEvent}. */
  @Bean
  public ReactiveWebSocketEventManager<SessionConnectedEvent>
      connectedEventWebSocketEventManager() {
    return ReactiveEventManagers.multicast();
  }

  /** {@link ReactiveWebSocketEventManager} to observe {@link ClientSessionClosedEvent}. */
  @Bean
  public ReactiveWebSocketEventManager<ClientSessionClosedEvent>
      clientClosedEventWebSocketEventManager() {
    return ReactiveEventManagers.multicast(ReactiveWebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
  }

  /** {@link ReactiveWebSocketEventManager} to observe {@link ServerSessionClosedEvent}. */
  @Bean
  public ReactiveWebSocketEventManager<ServerSessionClosedEvent>
      serverClosedEventWebSocketEventManager() {
    return ReactiveEventManagers.multicast(ReactiveWebSocketEventManager.MEDIUM_EVENT_QUEUE_SIZE);
  }

  @Bean
  public ReactiveWebSocketEventManagerFactory eventManagerFactory() {
    return ReactiveWebSocketEventManagerFactory.builder()
        .register(SessionConnectedEvent.class, connectedEventWebSocketEventManager())
        .register(ClientSessionClosedEvent.class, clientClosedEventWebSocketEventManager())
        .register(ServerSessionClosedEvent.class, serverClosedEventWebSocketEventManager())
        .build();
  }
}
