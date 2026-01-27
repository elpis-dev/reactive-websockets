package io.github.elpis.reactive.websockets.functional.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.data.CookiesSocketResource;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, CookiesSocketResource.class})
class CookiesSocketTest extends BaseWebSocketTest {

  @Test
  void testGetWithStringCookie() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("culture", new HttpCookie("culture", data));

    final String path = "/cookies/single/get";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithStringCookieNoValue() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("culture", new HttpCookie("culture", data));

    final String path = "/cookies/single/get/no/string";
    final Sinks.One<CloseStatus> closeStatusSink = Sinks.one();

    // test - should fail due to missing cookie
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .then(session.closeStatus().doOnNext(closeStatusSink::tryEmitValue).then()))
        .subscribe();

    // verify - expect WebSocketProcessingException wrapped in handshake error
    StepVerifier.create(closeStatusSink.asMono())
        .assertNext(
            closeStatus -> {
              assertThat(closeStatus.getCode()).isEqualTo(CloseStatus.PROTOCOL_ERROR.getCode());
              assertThat(closeStatus.getReason())
                  .isEqualTo("Cookie `cultures` is marked as required but was not present.");
            })
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericByteCookie() throws Exception {
    // given
    final byte data = this.getRandomByte();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/byte";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericPrimitiveByteCookie() throws Exception {
    // given
    final byte data = this.getRandomByte();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/byte";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericShortCookie() throws Exception {
    // given
    final short data = this.getRandomShort();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/short";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericShortPrimitiveCookie() throws Exception {
    // given
    final short data = this.getRandomShort();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/short";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericIntCookie() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/int";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericIntPrimitiveCookie() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/int";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericLongCookie() throws Exception {
    // given
    final long data = this.getRandomLong();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/long";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericLongPrimitiveCookie() throws Exception {
    // given
    final long data = this.getRandomLong();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/long";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithBooleanCookie() throws Exception {
    // given
    final boolean data = true;
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/boolean";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithBooleanPrimitiveCookie() throws Exception {
    // given
    final boolean data = false;
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/boolean/primitive";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericFloatCookie() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/float";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericFloatPrimitiveCookie() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/float";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericDoubleCookie() throws Exception {
    // given
    final double data = this.getRandomDouble();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/double";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithNumericDoublePrimitiveCookie() throws Exception {
    // given
    final double data = getRandomDouble();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/numeric/primitive/double";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithCharacterWrapperCookie() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/char";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data.charAt(0) + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithCharPrimitiveCookie() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/primitive/char";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data.charAt(0) + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithBigIntegerCookie() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/bigint";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithBigDecimalCookie() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/bigdeci";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":" + data + "}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetEnumCookie() throws Exception {
    // given
    final BootStarter.Example data = BootStarter.Example.VOID;
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", data.name()));

    final String path = "/cookies/single/get/enum";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithStringCookieNoRequired() throws Exception {
    // given
    final String data = this.randomTextString(10);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", String.valueOf(data)));

    final String path = "/cookies/single/get/no/required";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"null\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetListCookie() throws Exception {
    // given
    final List<String> data =
        List.of(this.randomTextString(3), this.randomTextString(3), this.randomTextString(3));
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    // Add multiple cookies with the same name
    data.forEach(value -> cookies.add("ids", new HttpCookie("ids", value)));

    final String path = "/cookies/single/get/list";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data.toString().replaceAll(" ", "") + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetDefaultCookie() throws Exception {
    // given
    final String data = this.randomTextString(10);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", data));

    final String path = "/cookies/single/get/default";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"default\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetHttpCookieList() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", data));

    final String path = "/cookies/list/httpcookie";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testGetWithMultipleStringCookie() throws Exception {
    // given
    final String id = this.randomTextString(5);
    final String version = this.randomTextString(5);
    final MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    cookies.add("id", new HttpCookie("id", id));
    cookies.add("version", new HttpCookie("version", version));

    final String path = "/cookies/multiple/get/cookie";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"cookie\":\"" + id + "_" + version + "\"}";

    // test
    this.withClient(
            path,
            cookies,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
