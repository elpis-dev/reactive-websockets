package org.elpis.reactive.socket.web.context.resource.data;

import org.elpis.reactive.socket.web.context.BootStarter;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;

import java.util.Map;

@SocketController("/path")
public class PathVariableSocketResource {

    @SocketMapping(value = "/single/get/string/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithStringPath(@PathVariable("id") final String id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/no/string/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNoStringPath(@PathVariable("ids") final String id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/byte/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericBytePath(@PathVariable("id") final Byte id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/byte/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericPrimitiveBytePath(@PathVariable("id") final byte id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/short/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortPath(@PathVariable("id") final Short id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/short/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortPrimitivePath(@PathVariable("id") final short id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/int/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntPath(@PathVariable("id") final Integer id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/int/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntPrimitivePath(@PathVariable("id") final int id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/long/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongPath(@PathVariable("id") final Long id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/long/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongPrimitivePath(@PathVariable("id") final long id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/boolean/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanPath(@PathVariable("id") final Boolean id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/boolean/primitive/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanPrimitivePath(@PathVariable("id") final boolean id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/enum/{id}", mode = Mode.SHARED)
    public Publisher<?> getEnumPath(@PathVariable("id") final BootStarter.Example id) {
        return Flux.just(Map.of("path", id));
    }

    @SocketMapping(value = "/single/get/no/required/{id}", mode = Mode.SHARED)
    public Publisher<?> getWithNoRequiredPath(@PathVariable(value = "no", required = false) final String id) {
        return Flux.just(Map.of("path", String.valueOf(id)));
    }

}
