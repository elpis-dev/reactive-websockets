package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

@MessageEndpoint("/query")
public class QueryParamSocketResource {

  @OnMessage(value = "/single/get/string")
  public Publisher<?> getWithStringQuery(@RequestParam("id") final String id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/no/string")
  public Publisher<?> getWithNoStringQuery(@RequestParam("ids") final String id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/no/required")
  public Publisher<?> getWithNoRequiredQuery(
      @RequestParam(value = "no", required = false) final String id) {
    return Flux.just(Map.of("query", String.valueOf(id)));
  }

  @OnMessage(value = "/single/get/default")
  public Publisher<?> getDefaultQuery(
      @RequestParam(value = "no", defaultValue = "default") final String id) {
    return Flux.just(Map.of("query", String.valueOf(id)));
  }

  @OnMessage(value = "/single/get/numeric/byte")
  public Publisher<?> getWithNumericByteQuery(@RequestParam("id") final Byte id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/byte")
  public Publisher<?> getWithNumericPrimitiveByteQuery(@RequestParam("id") final byte id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/short")
  public Publisher<?> getWithNumericShortQuery(@RequestParam("id") final Short id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/short")
  public Publisher<?> getWithNumericShortPrimitiveQuery(@RequestParam("id") final short id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/int")
  public Publisher<?> getWithNumericIntQuery(@RequestParam("id") final Integer id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/int")
  public Publisher<?> getWithNumericIntPrimitiveQuery(@RequestParam("id") final int id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/long")
  public Publisher<?> getWithNumericLongQuery(@RequestParam("id") final Long id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/long")
  public Publisher<?> getWithNumericLongPrimitiveQuery(@RequestParam("id") final long id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/float")
  public Publisher<?> getWithNumericFloatQuery(@RequestParam("id") final Float id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/float")
  public Publisher<?> getWithNumericFloatPrimitiveQuery(@RequestParam("id") final float id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/double")
  public Publisher<?> getWithNumericDoubleQuery(@RequestParam("id") final Double id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/double")
  public Publisher<?> getWithNumericDoublePrimitiveQuery(@RequestParam("id") final double id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/char")
  public Publisher<?> getWithCharacterWrapperQuery(@RequestParam("id") final Character id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/primitive/char")
  public Publisher<?> getWithCharPrimitiveQuery(@RequestParam("id") final char id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/bigint")
  public Publisher<?> getWithBigIntegerQuery(@RequestParam("id") final BigInteger id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/bigdeci")
  public Publisher<?> getWithBigDecimalQuery(@RequestParam("id") final BigDecimal id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/boolean")
  public Publisher<?> getWithBooleanQuery(@RequestParam("id") final Boolean id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/boolean/primitive")
  public Publisher<?> getWithBooleanPrimitiveQuery(@RequestParam("id") final boolean id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/enum")
  public Publisher<?> getEnumQuery(@RequestParam("id") final BootStarter.Example id) {
    return Flux.just(Map.of("query", id));
  }

  @OnMessage(value = "/single/get/list")
  public Publisher<?> getListQuery(@RequestParam("ids") final List<String> ids) {
    return Flux.just(Map.of("query", ids.toString()));
  }
}
