package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MessageEndpoint("/header")
public class HeaderSocketResource {

  @OnMessage(value = "/single/get/header")
  public Publisher<?> getWithStringHeader(@RequestHeader("id") final String id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/no/string")
  public Publisher<?> getWithNoStringHeader(@RequestHeader("ids") final String id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/no/required")
  public Publisher<?> getWithNoRequiredHeader(
      @RequestHeader(value = "no", required = false) final String id) {
    return Flux.just(Map.of("header", String.valueOf(id)));
  }

  @OnMessage(value = "/single/get/default")
  public Publisher<?> getDefaultHeader(
      @RequestHeader(value = "no", defaultValue = "default") final String id) {
    return Flux.just(Map.of("header", String.valueOf(id)));
  }

  @OnMessage(value = "/single/get/numeric/byte")
  public Publisher<?> getWithNumericByteHeader(@RequestHeader("id") final Byte id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/byte")
  public Publisher<?> getWithNumericPrimitiveByteHeader(@RequestHeader("id") final byte id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/short")
  public Publisher<?> getWithNumericShortHeader(@RequestHeader("id") final Short id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/short")
  public Publisher<?> getWithNumericShortPrimitiveHeader(@RequestHeader("id") final short id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/int")
  public Publisher<?> getWithNumericIntHeader(@RequestHeader("id") final Integer id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/int")
  public Publisher<?> getWithNumericIntPrimitiveHeader(@RequestHeader("id") final int id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/long")
  public Publisher<?> getWithNumericLongHeader(@RequestHeader("id") final Long id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/long")
  public Publisher<?> getWithNumericLongPrimitiveHeader(@RequestHeader("id") final long id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/float")
  public Publisher<?> getWithNumericFloatHeader(@RequestHeader("id") final Float id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/float")
  public Publisher<?> getWithNumericFloatPrimitiveHeader(@RequestHeader("id") final float id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/double")
  public Publisher<?> getWithNumericDoubleHeader(@RequestHeader("id") final Double id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/double")
  public Publisher<?> getWithNumericDoublePrimitiveHeader(@RequestHeader("id") final double id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/char")
  public Publisher<?> getWithCharacterWrapperHeader(@RequestHeader("id") final Character id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/primitive/char")
  public Publisher<?> getWithCharPrimitiveHeader(@RequestHeader("id") final char id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/bigint")
  public Publisher<?> getWithBigIntegerHeader(@RequestHeader("id") final BigInteger id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/bigdeci")
  public Publisher<?> getWithBigDecimalHeader(@RequestHeader("id") final BigDecimal id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/boolean")
  public Publisher<?> getWithBooleanHeader(@RequestHeader("id") final Boolean id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/boolean/primitive")
  public Publisher<?> getWithBooleanPrimitiveHeader(@RequestHeader("id") final boolean id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/enum")
  public Publisher<?> getEnumHeader(@RequestHeader("id") final BootStarter.Example id) {
    return Flux.just(Map.of("header", id));
  }

  @OnMessage(value = "/single/get/list")
  public Publisher<?> getListHeader(@RequestHeader("ids") final List<String> ids) {
    return Flux.just(Map.of("header", ids.toString()));
  }

  @OnMessage(value = "/multiple/get/header")
  public Publisher<?> getWithMultipleStringHeader(
      @RequestHeader("id") final String id, @RequestHeader("version") final String version) {

    return Flux.just(Map.of("header", id + "_" + version));
  }

  @OnMessage(value = "/http")
  public Publisher<?> getHttpHeaders(@RequestHeader final HttpHeaders headers) {
    return Mono.justOrEmpty(
        Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
  }

  @OnMessage(value = "/multimap")
  public Publisher<?> getMultiValueMap(@RequestHeader final MultiValueMap<String, String> headers) {
    return Mono.justOrEmpty(
        Optional.ofNullable(headers.getFirst("id")).map(header -> Map.of("header", header)));
  }
}
