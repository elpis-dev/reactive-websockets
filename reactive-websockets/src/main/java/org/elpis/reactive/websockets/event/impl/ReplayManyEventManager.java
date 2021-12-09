package org.elpis.reactive.websockets.event.impl;

import org.elpis.reactive.websockets.event.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

public abstract class ReplayManyEventManager<T extends WebSocketEvent<?>> implements WebSocketEventManager<T> {
    private final Sinks.Many<T> sink = Sinks.many().replay().all();

    @Override
    public Sinks.EmitResult fire(final T t) {
        return this.sink.tryEmitNext(t);
    }

    @Override
    public Publisher<T> listen() {
        return this.sink.asFlux();
    }
}
