package org.elpis.socket.web.context.resource;

import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.SocketHeader;
import org.elpis.socket.web.context.BootStarter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SocketResource("/header")
public class HeaderSocketResource implements BasicWebSocketResource {

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
    public Publisher<?> getEnumHeader(@SocketHeader("id") final BootStarter.Test id) {
        return Flux.just(Map.of("header", id));
    }

    @Outbound("/single/get/list")
    public Publisher<?> getListHeader(@SocketHeader("ids") final List<String> ids) {
        return Flux.just(Map.of("header", ids.toString()));
    }

}
