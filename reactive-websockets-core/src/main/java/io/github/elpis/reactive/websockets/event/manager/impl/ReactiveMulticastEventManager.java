package io.github.elpis.reactive.websockets.event.manager.impl;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManager;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

/**
 * Abstract implementation of {@link ReactiveWebSocketEventManager} that uses {@code
 * Sinks.many().multicast().onBackpressureBuffer()} as event queue.
 *
 * @author Phillip J. Fry
 * @see Sinks#many()
 * @see ReactiveWebSocketEventManager
 * @since 1.0.0
 */
public abstract class ReactiveMulticastEventManager<T extends WebSocketEvent<?>>
    implements ReactiveWebSocketEventManager<T> {
  private final Sinks.Many<T> sink;

  /**
   * Sets sink with queue of size {@link Queues#SMALL_BUFFER_SIZE}.
   *
   * @since 1.0.0
   */
  protected ReactiveMulticastEventManager() {
    this(Queues.SMALL_BUFFER_SIZE);
  }

  /**
   * Sets sink with queue of custom size.
   *
   * @param eventQueueSize custom queue size
   * @since 1.0.0
   */
  protected ReactiveMulticastEventManager(final int eventQueueSize) {
    sink = Sinks.many().multicast().onBackpressureBuffer(eventQueueSize);
  }

  /**
   * See {@link ReactiveWebSocketEventManager#fire(WebSocketEvent)}
   *
   * @since 1.0.0
   */
  @Override
  public Sinks.EmitResult fire(final T t) {
    return this.sink.tryEmitNext(t);
  }

  /**
   * See {@link ReactiveWebSocketEventManager#listen()}
   *
   * @since 1.0.0
   */
  @Override
  public Publisher<T> listen() {
    return this.sink.asFlux().share();
  }
}
