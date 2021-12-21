package org.elpis.socket.web.context.resource.data;

import org.elpis.reactive.websockets.web.annotation.controller.Outbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotation.request.SocketQueryParam;
import org.elpis.socket.web.context.BootStarter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@SocketResource("/query")
public class QueryParamSocketResource {

    @Outbound("/single/get/string")
    public Publisher<?> getWithStringQuery(@SocketQueryParam("id") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/no/string")
    public Publisher<?> getWithNoStringQuery(@SocketQueryParam("ids") final String id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/no/required")
    public Publisher<?> getWithNoRequiredQuery(@SocketQueryParam(value = "no", required = false) final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @Outbound("/single/get/default")
    public Publisher<?> getDefaultQuery(@SocketQueryParam(value = "no", defaultValue = "default") final String id) {
        return Flux.just(Map.of("query", String.valueOf(id)));
    }

    @Outbound("/single/get/numeric/byte")
    public Publisher<?> getWithNumericByteQuery(@SocketQueryParam("id") final Byte id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/byte")
    public Publisher<?> getWithNumericPrimitiveByteQuery(@SocketQueryParam("id") final byte id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/short")
    public Publisher<?> getWithNumericShortQuery(@SocketQueryParam("id") final Short id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/short")
    public Publisher<?> getWithNumericShortPrimitiveQuery(@SocketQueryParam("id") final short id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/int")
    public Publisher<?> getWithNumericIntQuery(@SocketQueryParam("id") final Integer id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/int")
    public Publisher<?> getWithNumericIntPrimitiveQuery(@SocketQueryParam("id") final int id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/long")
    public Publisher<?> getWithNumericLongQuery(@SocketQueryParam("id") final Long id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/long")
    public Publisher<?> getWithNumericLongPrimitiveQuery(@SocketQueryParam("id") final long id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/float")
    public Publisher<?> getWithNumericFloatQuery(@SocketQueryParam("id") final Float id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/float")
    public Publisher<?> getWithNumericFloatPrimitiveQuery(@SocketQueryParam("id") final float id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/double")
    public Publisher<?> getWithNumericDoubleQuery(@SocketQueryParam("id") final Double id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/numeric/primitive/double")
    public Publisher<?> getWithNumericDoublePrimitiveQuery(@SocketQueryParam("id") final double id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/char")
    public Publisher<?> getWithCharacterWrapperQuery(@SocketQueryParam("id") final Character id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/primitive/char")
    public Publisher<?> getWithCharPrimitiveQuery(@SocketQueryParam("id") final char id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/bigint")
    public Publisher<?> getWithBigIntegerQuery(@SocketQueryParam("id") final BigInteger id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/bigdeci")
    public Publisher<?> getWithBigDecimalQuery(@SocketQueryParam("id") final BigDecimal id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/boolean")
    public Publisher<?> getWithBooleanQuery(@SocketQueryParam("id") final Boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/boolean/primitive")
    public Publisher<?> getWithBooleanPrimitiveQuery(@SocketQueryParam("id") final boolean id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/enum")
    public Publisher<?> getEnumQuery(@SocketQueryParam("id") final BootStarter.Example id) {
        return Flux.just(Map.of("query", id));
    }

    @Outbound("/single/get/list")
    public Publisher<?> getListQuery(@SocketQueryParam("ids") final List<String> ids) {
        return Flux.just(Map.of("query", ids.toString()));
    }

}
