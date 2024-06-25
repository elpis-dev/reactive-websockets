package org.elpis.reactive.socket.web.context.resource.data;

import org.elpis.reactive.socket.web.context.BootStarter;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SocketController("/query")
public class QueryParamSocketResource {

    @SocketMapping(value = "/single/get/string", mode = Mode.SHARED)
    public Publisher<?> getWithStringQuery(@RequestParam("id") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/no/string", mode = Mode.SHARED)
    public Publisher<?> getWithNoStringQuery(@RequestParam("ids") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/no/required", mode = Mode.SHARED)
    public Publisher<?> getWithNoRequiredQuery(@RequestParam(value = "no", required = false) final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @SocketMapping(value = "/single/get/default", mode = Mode.SHARED)
    public Publisher<?> getDefaultQuery(@RequestParam(value = "no", defaultValue = "default") final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @SocketMapping(value = "/single/get/numeric/byte", mode = Mode.SHARED)
    public Publisher<?> getWithNumericByteQuery(@RequestParam("id") final Byte id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/byte", mode = Mode.SHARED)
    public Publisher<?> getWithNumericPrimitiveByteQuery(@RequestParam("id") final byte id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/short", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortQuery(@RequestParam("id") final Short id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/short", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortPrimitiveQuery(@RequestParam("id") final short id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/int", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntQuery(@RequestParam("id") final Integer id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/int", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntPrimitiveQuery(@RequestParam("id") final int id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/long", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongQuery(@RequestParam("id") final Long id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/long", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongPrimitiveQuery(@RequestParam("id") final long id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/float", mode = Mode.SHARED)
    public Publisher<?> getWithNumericFloatQuery(@RequestParam("id") final Float id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/float", mode = Mode.SHARED)
    public Publisher<?> getWithNumericFloatPrimitiveQuery(@RequestParam("id") final float id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/double", mode = Mode.SHARED)
    public Publisher<?> getWithNumericDoubleQuery(@RequestParam("id") final Double id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/numeric/primitive/double", mode = Mode.SHARED)
    public Publisher<?> getWithNumericDoublePrimitiveQuery(@RequestParam("id") final double id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/char", mode = Mode.SHARED)
    public Publisher<?> getWithCharacterWrapperQuery(@RequestParam("id") final Character id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/primitive/char", mode = Mode.SHARED)
    public Publisher<?> getWithCharPrimitiveQuery(@RequestParam("id") final char id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/bigint", mode = Mode.SHARED)
    public Publisher<?> getWithBigIntegerQuery(@RequestParam("id") final BigInteger id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/bigdeci", mode = Mode.SHARED)
    public Publisher<?> getWithBigDecimalQuery(@RequestParam("id") final BigDecimal id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/boolean", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanQuery(@RequestParam("id") final Boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/boolean/primitive", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanPrimitiveQuery(@RequestParam("id") final boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/enum", mode = Mode.SHARED)
    public Publisher<?> getEnumQuery(@RequestParam("id") final BootStarter.Example id) {
        return Flux.just(Map.of("query", id));
    }

    @SocketMapping(value = "/single/get/list", mode = Mode.SHARED)
    public Publisher<?> getListQuery(@RequestParam("ids") final List<String> ids) {
        return Flux.just(Map.of("query", ids.toString()));
    }

}
