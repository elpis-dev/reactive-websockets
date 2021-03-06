package org.elpis.socket.web.context.resource.data;

import org.elpis.reactive.websockets.web.annotation.controller.Outbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotation.request.SocketHeader;
import org.elpis.socket.web.context.BootStarter;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SocketResource("/header")
public class HeaderSocketResource {

    @Outbound("/single/get/header")
    public Publisher<?> getWithStringHeader(@SocketHeader("id") final String id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/no/string")
    public Publisher<?> getWithNoStringHeader(@SocketHeader("ids") final String id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/no/required")
    public Publisher<?> getWithNoRequiredHeader(@SocketHeader(value = "no", required = false) final String id) {
        return Flux.just(Map.of("header", String.valueOf(id)));
    }

    @Outbound("/single/get/default")
    public Publisher<?> getDefaultHeader(@SocketHeader(value = "no", defaultValue = "default") final String id) {
        return Flux.just(Map.of("header", String.valueOf(id)));
    }

    @Outbound("/single/get/numeric/byte")
    public Publisher<?> getWithNumericByteHeader(@SocketHeader("id") final Byte id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/byte")
    public Publisher<?> getWithNumericPrimitiveByteHeader(@SocketHeader("id") final byte id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/short")
    public Publisher<?> getWithNumericShortHeader(@SocketHeader("id") final Short id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/short")
    public Publisher<?> getWithNumericShortPrimitiveHeader(@SocketHeader("id") final short id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/int")
    public Publisher<?> getWithNumericIntHeader(@SocketHeader("id") final Integer id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/int")
    public Publisher<?> getWithNumericIntPrimitiveHeader(@SocketHeader("id") final int id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/long")
    public Publisher<?> getWithNumericLongHeader(@SocketHeader("id") final Long id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/long")
    public Publisher<?> getWithNumericLongPrimitiveHeader(@SocketHeader("id") final long id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/float")
    public Publisher<?> getWithNumericFloatHeader(@SocketHeader("id") final Float id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/float")
    public Publisher<?> getWithNumericFloatPrimitiveHeader(@SocketHeader("id") final float id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/double")
    public Publisher<?> getWithNumericDoubleHeader(@SocketHeader("id") final Double id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/numeric/primitive/double")
    public Publisher<?> getWithNumericDoublePrimitiveHeader(@SocketHeader("id") final double id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/char")
    public Publisher<?> getWithCharacterWrapperHeader(@SocketHeader("id") final Character id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/primitive/char")
    public Publisher<?> getWithCharPrimitiveHeader(@SocketHeader("id") final char id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/bigint")
    public Publisher<?> getWithBigIntegerHeader(@SocketHeader("id") final BigInteger id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/bigdeci")
    public Publisher<?> getWithBigDecimalHeader(@SocketHeader("id") final BigDecimal id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/boolean")
    public Publisher<?> getWithBooleanHeader(@SocketHeader("id") final Boolean id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/boolean/primitive")
    public Publisher<?> getWithBooleanPrimitiveHeader(@SocketHeader("id") final boolean id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/enum")
    public Publisher<?> getEnumHeader(@SocketHeader("id") final BootStarter.Example id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/list")
    public Publisher<?> getListHeader(@SocketHeader("ids") final List<String> ids) {
        return Flux.just(Map.of("header", ids.toString()));
    }

    @Outbound("/multiple/get/header")
    public Publisher<?> getWithMultipleStringHeader(@SocketHeader("id") final String id,
                                                    @SocketHeader("version") final String version) {

        return Flux.just(Map.of("header", id + "_" + version));
    }

    @Outbound("/http")
    public Publisher<?> getHttpHeaders(@SocketHeader final HttpHeaders headers) {
        return Mono.justOrEmpty(Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
    }

    @Outbound("/multimap")
    public Publisher<?> getMultiValueMap(@SocketHeader final MultiValueMap<String, String> headers) {
        return Mono.justOrEmpty(Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
    }

    @Outbound("/multimap/bad")
    public Publisher<?> getBadMultiValueMap(@SocketHeader final MultiValueMap<String, Long> headers) {
        return Mono.justOrEmpty(Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
    }

}
