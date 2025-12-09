package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@MessageEndpoint("/header")
public class HeaderSocketResource {

    @OnMessage(value = "/single/get/header", mode = Mode.SHARED)
    public Publisher<?> getWithStringHeader(@RequestHeader("id") final String id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/no/string", mode = Mode.SHARED)
    public Publisher<?> getWithNoStringHeader(@RequestHeader("ids") final String id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/no/required", mode = Mode.SHARED)
    public Publisher<?> getWithNoRequiredHeader(@RequestHeader(value = "no", required = false) final String id) {
        return Flux.just(Map.of("header", String.valueOf(id)));
    }

    @OnMessage(value = "/single/get/default", mode = Mode.SHARED)
    public Publisher<?> getDefaultHeader(@RequestHeader(value = "no", defaultValue = "default") final String id) {
        return Flux.just(Map.of("header", String.valueOf(id)));
    }

    @OnMessage(value = "/single/get/numeric/byte", mode = Mode.SHARED)
    public Publisher<?> getWithNumericByteHeader(@RequestHeader("id") final Byte id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/byte", mode = Mode.SHARED)
    public Publisher<?> getWithNumericPrimitiveByteHeader(@RequestHeader("id") final byte id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/short", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortHeader(@RequestHeader("id") final Short id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/short", mode = Mode.SHARED)
    public Publisher<?> getWithNumericShortPrimitiveHeader(@RequestHeader("id") final short id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/int", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntHeader(@RequestHeader("id") final Integer id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/int", mode = Mode.SHARED)
    public Publisher<?> getWithNumericIntPrimitiveHeader(@RequestHeader("id") final int id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/long", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongHeader(@RequestHeader("id") final Long id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/long", mode = Mode.SHARED)
    public Publisher<?> getWithNumericLongPrimitiveHeader(@RequestHeader("id") final long id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/float", mode = Mode.SHARED)
    public Publisher<?> getWithNumericFloatHeader(@RequestHeader("id") final Float id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/float", mode = Mode.SHARED)
    public Publisher<?> getWithNumericFloatPrimitiveHeader(@RequestHeader("id") final float id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/double", mode = Mode.SHARED)
    public Publisher<?> getWithNumericDoubleHeader(@RequestHeader("id") final Double id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/numeric/primitive/double", mode = Mode.SHARED)
    public Publisher<?> getWithNumericDoublePrimitiveHeader(@RequestHeader("id") final double id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/char", mode = Mode.SHARED)
    public Publisher<?> getWithCharacterWrapperHeader(@RequestHeader("id") final Character id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/primitive/char", mode = Mode.SHARED)
    public Publisher<?> getWithCharPrimitiveHeader(@RequestHeader("id") final char id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/bigint", mode = Mode.SHARED)
    public Publisher<?> getWithBigIntegerHeader(@RequestHeader("id") final BigInteger id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/bigdeci", mode = Mode.SHARED)
    public Publisher<?> getWithBigDecimalHeader(@RequestHeader("id") final BigDecimal id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/boolean", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanHeader(@RequestHeader("id") final Boolean id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/boolean/primitive", mode = Mode.SHARED)
    public Publisher<?> getWithBooleanPrimitiveHeader(@RequestHeader("id") final boolean id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/enum", mode = Mode.SHARED)
    public Publisher<?> getEnumHeader(@RequestHeader("id") final BootStarter.Example id) {
        return Flux.just(Map.of("header", id));
    }

    @OnMessage(value = "/single/get/list", mode = Mode.SHARED)
    public Publisher<?> getListHeader(@RequestHeader("ids") final List<String> ids) {
        return Flux.just(Map.of("header", ids.toString()));
    }

    @OnMessage(value = "/multiple/get/header", mode = Mode.SHARED)
    public Publisher<?> getWithMultipleStringHeader(@RequestHeader("id") final String id,
                                                    @RequestHeader("version") final String version) {

        return Flux.just(Map.of("header", id + "_" + version));
    }

    @OnMessage(value = "/http", mode = Mode.SHARED)
    public Publisher<?> getHttpHeaders(@RequestHeader final HttpHeaders headers) {
        return Mono.justOrEmpty(Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
    }

    @OnMessage(value = "/multimap", mode = Mode.SHARED)
    public Publisher<?> getMultiValueMap(@RequestHeader final MultiValueMap<String, String> headers) {
        return Mono.justOrEmpty(Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
    }

}
