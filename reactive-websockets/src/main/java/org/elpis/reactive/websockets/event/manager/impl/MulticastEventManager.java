package org.elpis.reactive.websockets.event.manager.impl;

import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

/**
 * Abstract implementation of {@link WebSocketEventManager} that uses {@code Sinks.many().multicast().onBackpressureBuffer()} as event queue.
 * @author Alex Zharkov
 * @see Sinks#many()
 * @see WebSocketEventManager
 * @since 0.1.0
 */
public abstract class MulticastEventManager<T extends WebSocketEvent<?>> implements WebSocketEventManager<T> {
    private final Sinks.Many<T> sink;

    /**
     * Sets sink with queue of size {@link Queues#SMALL_BUFFER_SIZE}.
     * @since 0.1.0
     */
    protected MulticastEventManager() {
        this(Queues.SMALL_BUFFER_SIZE);
    }

    /**
     * Sets sink with queue of custom size.
     * @param eventQueueSize custom queue size
     * @since 0.1.0
     */
    protected MulticastEventManager(final int eventQueueSize) {
        sink = Sinks.many().multicast().onBackpressureBuffer(eventQueueSize);
    }

    /**
     * See {@link WebSocketEventManager#fire(WebSocketEvent)}
     * @since 0.1.0
     */
    @Override
    public Sinks.EmitResult fire(final T t) {
        return this.sink.tryEmitNext(t);
    }

    /**
     * See {@link WebSocketEventManager#listen()}
     * @since 0.1.0
     */
    @Override
    public Publisher<T> listen() {
        return this.sink.asFlux();
    }
}
