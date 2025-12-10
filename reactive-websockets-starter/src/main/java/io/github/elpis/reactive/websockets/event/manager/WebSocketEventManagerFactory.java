package io.github.elpis.reactive.websockets.event.manager;

import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketEventManagerFactory {
  private final Map<Class<? extends WebSocketEvent<?>>, WebSocketEventManager<?>>
      socketEventManagers = new ConcurrentHashMap<>();

  private WebSocketEventManagerFactory() {
    //
  }

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
