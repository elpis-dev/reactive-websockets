package org.elpis.reactive.websockets.event;

import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public interface WebSocketEventManager<T extends WebSocketEvent<?>> {
    int MEDIUM_EVENT_QUEUE_SIZE = 256;

    Sinks.EmitResult fire(final T t);

    Publisher<T> listen();

    default Mono<T> asMono() {
        return Mono.from(this.listen());
    }

    default Flux<T> asFlux() {
        return Flux.from(this.listen());
    }
}
