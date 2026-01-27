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
import org.springframework.http.HttpCookie;
import org.springframework.web.bind.annotation.CookieValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MessageEndpoint("/cookies")
public class CookiesSocketResource {

  @OnMessage(value = "/single/get")
  public Publisher<?> getWithStringCookie(@CookieValue("culture") final String culture) {
    return Flux.just(Map.of("cookie", culture));
  }

  @OnMessage(value = "/single/get/no/string")
  public Publisher<?> getWithNoStringCookie(@CookieValue("cultures") final String cultures) {
    return Flux.just(Map.of("cookie", cultures));
  }

  @OnMessage(value = "/single/get/no/required")
  public Publisher<?> getWithNoRequiredCookie(
      @CookieValue(value = "no", required = false) final String culture) {
    return Flux.just(Map.of("cookie", String.valueOf(culture)));
  }

  @OnMessage(value = "/single/get/default")
  public Publisher<?> getDefaultCookie(
      @CookieValue(value = "no", defaultValue = "default", required = false) final String culture) {
    return Flux.just(Map.of("cookie", String.valueOf(culture)));
  }

  @OnMessage(value = "/single/get/numeric/byte")
  public Publisher<?> getWithNumericByteCookie(@CookieValue("id") final Byte id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/byte")
  public Publisher<?> getWithNumericPrimitiveByteCookie(@CookieValue("id") final byte id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/short")
  public Publisher<?> getWithNumericShortCookie(@CookieValue("id") final Short id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/short")
  public Publisher<?> getWithNumericShortPrimitiveCookie(@CookieValue("id") final short id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/int")
  public Publisher<?> getWithNumericIntCookie(@CookieValue("id") final Integer id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/int")
  public Publisher<?> getWithNumericIntPrimitiveCookie(@CookieValue("id") final int id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/long")
  public Publisher<?> getWithNumericLongCookie(@CookieValue("id") final Long id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/long")
  public Publisher<?> getWithNumericLongPrimitiveCookie(@CookieValue("id") final long id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/float")
  public Publisher<?> getWithNumericFloatCookie(@CookieValue("id") final Float id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/float")
  public Publisher<?> getWithNumericFloatPrimitiveCookie(@CookieValue("id") final float id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/double")
  public Publisher<?> getWithNumericDoubleCookie(@CookieValue("id") final Double id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/double")
  public Publisher<?> getWithNumericDoublePrimitiveCookie(@CookieValue("id") final double id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/char")
  public Publisher<?> getWithCharacterWrapperCookie(@CookieValue("id") final Character id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/primitive/char")
  public Publisher<?> getWithCharPrimitiveCookie(@CookieValue("id") final char id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/bigint")
  public Publisher<?> getWithBigIntegerCookie(@CookieValue("id") final BigInteger id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/bigdeci")
  public Publisher<?> getWithBigDecimalCookie(@CookieValue("id") final BigDecimal id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/boolean")
  public Publisher<?> getWithBooleanCookie(@CookieValue("id") final Boolean id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/boolean/primitive")
  public Publisher<?> getWithBooleanPrimitiveCookie(@CookieValue("id") final boolean id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/enum")
  public Publisher<?> getEnumCookie(@CookieValue("id") final BootStarter.Example id) {
    return Flux.just(Map.of("cookie", id));
  }

  @OnMessage(value = "/single/get/list")
  public Publisher<?> getListCookie(@CookieValue("ids") final List<HttpCookie> ids) {
    return Flux.just(Map.of("cookie", ids.stream().map(HttpCookie::getValue).toList().toString()));
  }

  @OnMessage(value = "/multiple/get/cookie")
  public Publisher<?> getWithMultipleStringCookie(
      @CookieValue("id") final String id, @CookieValue("version") final String version) {
    return Flux.just(Map.of("cookie", id + "_" + version));
  }

  @OnMessage(value = "/list/httpcookie")
  public Publisher<?> getHttpCookieList(@CookieValue("id") final List<HttpCookie> cookies) {
    return Mono.justOrEmpty(
        Optional.ofNullable(cookies)
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0))
            .map(HttpCookie::getValue)
            .map(value -> Map.of("cookie", value)));
  }
}
