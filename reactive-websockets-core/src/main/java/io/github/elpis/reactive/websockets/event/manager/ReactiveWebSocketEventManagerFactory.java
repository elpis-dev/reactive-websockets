package io.github.elpis.reactive.websockets.event.manager;

import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and retrieving {@link ReactiveWebSocketEventManager} instances.
 *
 * <p>Use the builder to register event managers for specific event types:
 *
 * <pre>{@code
 * WebSocketEventManagerFactory factory = WebSocketEventManagerFactory.builder()
 *     .register(SessionConnectedEvent.class, connectedEventManager)
 *     .register(SessionDisconnectedEvent.class, disconnectedEventManager)
 *     .build();
 * }</pre>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveWebSocketEventManagerFactory {
  private final Map<Class<? extends WebSocketEvent<?>>, ReactiveWebSocketEventManager<?>>
      socketEventManagers = new ConcurrentHashMap<>();

  private ReactiveWebSocketEventManagerFactory() {}

  public <T extends WebSocketEvent<?>> ReactiveWebSocketEventManager<T> getEventManager(
      final Class<T> eventType) {
    return (ReactiveWebSocketEventManager<T>) this.socketEventManagers.get(eventType);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ReactiveWebSocketEventManagerFactory factory =
        new ReactiveWebSocketEventManagerFactory();

    public <T extends WebSocketEvent<?>> Builder register(
        final Class<T> eventType, final ReactiveWebSocketEventManager<T> eventManager) {

      this.factory.socketEventManagers.put(eventType, eventManager);
      return this;
    }

    public ReactiveWebSocketEventManagerFactory build() {
      return this.factory;
    }
  }
}
