package io.github.elpis.reactive.websockets.event.manager;

import io.github.elpis.reactive.websockets.event.manager.impl.ReactiveMulticastEventManager;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

/**
 * Utils class with some pre-implemented {@link ReactiveWebSocketEventManager}.
 *
 * @author Phillip J. Fry
 * @see WebSocketEvent
 * @see ReactiveWebSocketEventManager
 * @since 1.0.0
 */
public final class ReactiveEventManagers {
  private ReactiveEventManagers() {}

  /**
   * Creates default {@link ReactiveMulticastEventManager} with default queue size of {@link
   * reactor.util.concurrent.Queues#SMALL_BUFFER_SIZE}.
   *
   * @return {@link ReactiveMulticastEventManager}
   * @since 1.0.0
   */
  public static <T extends WebSocketEvent<?>> ReactiveMulticastEventManager<T> multicast() {
    return new ReactiveMulticastEventManager<>() {};
  }

  /**
   * Creates default {@link ReactiveMulticastEventManager} with custom queue size.
   *
   * @return {@link ReactiveMulticastEventManager}
   * @since 1.0.0
   */
  public static <T extends WebSocketEvent<?>> ReactiveMulticastEventManager<T> multicast(
      final int eventQueueSize) {
    return new ReactiveMulticastEventManager<>(eventQueueSize) {};
  }
}
