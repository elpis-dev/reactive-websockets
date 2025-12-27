package io.github.elpis.reactive.websockets.event.manager;

import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and retrieving {@link WebSocketEventManager} instances.
 *
 * <p>Use the builder to register event managers for specific event types:
 *
 * <pre>{@code
 * WebSocketEventManagerFactory factory = WebSocketEventManagerFactory.builder()
 *     .register(SessionConnectedEvent.class, connectedEventManager)
 *     .register(SessionDisconnectedEvent.class, disconnectedEventManager)
 *     .build();
 * }</pre>
 */
public class WebSocketEventManagerFactory {
  private final Map<Class<? extends WebSocketEvent<?>>, WebSocketEventManager<?>>
      socketEventManagers = new ConcurrentHashMap<>();

  private WebSocketEventManagerFactory() {}

  public <T extends WebSocketEvent<?>> WebSocketEventManager<T> getEventManager(
      final Class<T> eventType) {
    return (WebSocketEventManager<T>) this.socketEventManagers.get(eventType);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final WebSocketEventManagerFactory factory = new WebSocketEventManagerFactory();

    public <T extends WebSocketEvent<?>> Builder register(
        final Class<T> eventType, final WebSocketEventManager<T> eventManager) {

      this.factory.socketEventManagers.put(eventType, eventManager);
      return this;
    }

    public WebSocketEventManagerFactory build() {
      return this.factory;
    }
  }
}
