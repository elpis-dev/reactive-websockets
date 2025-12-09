package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@MessageEndpoint("/query")
public class QueryParamSocketResource {

    @OnMessage(value = "/single/get/string", mode = Mode.BROADCAST)
    public Publisher<?> getWithStringQuery(@RequestParam("id") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/no/string", mode = Mode.BROADCAST)
    public Publisher<?> getWithNoStringQuery(@RequestParam("ids") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/no/required", mode = Mode.BROADCAST)
    public Publisher<?> getWithNoRequiredQuery(@RequestParam(value = "no", required = false) final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @OnMessage(value = "/single/get/default", mode = Mode.BROADCAST)
    public Publisher<?> getDefaultQuery(@RequestParam(value = "no", defaultValue = "default") final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @OnMessage(value = "/single/get/numeric/byte", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericByteQuery(@RequestParam("id") final Byte id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/byte", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericPrimitiveByteQuery(@RequestParam("id") final byte id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/short", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericShortQuery(@RequestParam("id") final Short id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/short", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericShortPrimitiveQuery(@RequestParam("id") final short id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/int", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericIntQuery(@RequestParam("id") final Integer id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/int", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericIntPrimitiveQuery(@RequestParam("id") final int id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/long", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericLongQuery(@RequestParam("id") final Long id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/long", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericLongPrimitiveQuery(@RequestParam("id") final long id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/float", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericFloatQuery(@RequestParam("id") final Float id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/float", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericFloatPrimitiveQuery(@RequestParam("id") final float id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/double", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericDoubleQuery(@RequestParam("id") final Double id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/double", mode = Mode.BROADCAST)
    public Publisher<?> getWithNumericDoublePrimitiveQuery(@RequestParam("id") final double id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/char", mode = Mode.BROADCAST)
    public Publisher<?> getWithCharacterWrapperQuery(@RequestParam("id") final Character id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/primitive/char", mode = Mode.BROADCAST)
    public Publisher<?> getWithCharPrimitiveQuery(@RequestParam("id") final char id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/bigint", mode = Mode.BROADCAST)
    public Publisher<?> getWithBigIntegerQuery(@RequestParam("id") final BigInteger id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/bigdeci", mode = Mode.BROADCAST)
    public Publisher<?> getWithBigDecimalQuery(@RequestParam("id") final BigDecimal id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/boolean", mode = Mode.BROADCAST)
    public Publisher<?> getWithBooleanQuery(@RequestParam("id") final Boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/boolean/primitive", mode = Mode.BROADCAST)
    public Publisher<?> getWithBooleanPrimitiveQuery(@RequestParam("id") final boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/enum", mode = Mode.BROADCAST)
    public Publisher<?> getEnumQuery(@RequestParam("id") final BootStarter.Example id) {
        return Flux.just(Map.of("query", id));
    }

    @OnMessage(value = "/single/get/list", mode = Mode.BROADCAST)
    public Publisher<?> getListQuery(@RequestParam("ids") final List<String> ids) {
        return Flux.just(Map.of("query", ids.toString()));
    }

}
