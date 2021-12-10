package org.elpis.reactive.websockets.event.manager.impl;

import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public abstract class MulticastEventManager<T extends WebSocketEvent<?>> implements WebSocketEventManager<T> {
    private final Sinks.Many<T> sink;

    protected MulticastEventManager() {
        this(Queues.SMALL_BUFFER_SIZE);
    }

    protected MulticastEventManager(final int eventQueueSize) {
        sink = Sinks.many().multicast().onBackpressureBuffer(eventQueueSize);
    }

    @Override
    public Sinks.EmitResult fire(final T t) {
        return this.sink.tryEmitNext(t);
    }

    @Override
    public Publisher<T> listen() {
        return this.sink.asFlux();
    }
}
