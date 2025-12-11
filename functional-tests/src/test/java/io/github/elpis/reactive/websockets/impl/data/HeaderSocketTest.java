package io.github.elpis.reactive.websockets.impl.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.data.HeaderSocketResource;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, HeaderSocketResource.class})
class HeaderSocketTest extends BaseWebSocketTest {

  @Test
  void getWithStringHeaderTest() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data);

    final String path = "/header/single/get/header";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithStringHeaderNoValueTest(final CapturedOutput output) throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data);

    final String path = "/header/single/get/no/string";
    final Sinks.One<String> sink = Sinks.one();

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);

    assertThat(output)
        .contains(
            "@RequestHeader java.lang.String id is marked as required but was not present on request. Default value was not set.");
  }

  @Test
  void getWithNumericByteHeaderTest() throws Exception {
    // given
    final byte data = this.getRandomByte();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/byte";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericPrimitiveByteHeaderTest() throws Exception {
    // given
    final byte data = this.getRandomByte();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/byte";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericShortHeaderTest() throws Exception {
    // given
    final short data = this.getRandomShort();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/short";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericShortPrimitiveHeaderTest() throws Exception {
    // given
    final short data = this.getRandomShort();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/short";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericIntHeaderTest() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/int";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericIntPrimitiveHeaderTest() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/int";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericLongHeaderTest() throws Exception {
    // given
    final long data = this.getRandomLong();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/long";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericLongPrimitiveHeaderTest() throws Exception {
    // given
    final long data = this.getRandomLong();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/long";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithBooleanHeaderTest() throws Exception {
    // given
    final boolean data = true;
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/boolean";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithBooleanPrimitiveHeaderTest() throws Exception {
    // given
    final boolean data = false;
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/boolean/primitive";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericFloatHeaderTest() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/float";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericFloatPrimitiveHeaderTest() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/float";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericDoubleHeaderTest() throws Exception {
    // given
    final double data = this.getRandomDouble();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/double";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithNumericDoublePrimitiveHeaderTest() throws Exception {
    // given
    final double data = getRandomDouble();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/numeric/primitive/double";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithCharacterWrapperHeaderTest() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/char";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data.charAt(0) + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithCharPrimitiveHeaderTest() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/primitive/char";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data.charAt(0) + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithBigIntegerHeaderTest() throws Exception {
    // given
    final int data = this.getRandomInteger();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/bigint";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithBigDecimalHeaderTest() throws Exception {
    // given
    final float data = this.getRandomFloat();
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/bigdeci";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":" + data + "}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getEnumHeaderTest() throws Exception {
    // given
    final BootStarter.Example data = BootStarter.Example.VOID;
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data.name());

    final String path = "/header/single/get/enum";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithStringHeaderNoRequiredTest() throws Exception {
    // given
    final String data = this.randomTextString(10);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", String.valueOf(data));

    final String path = "/header/single/get/no/required";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"null\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getListHeaderTest() throws Exception {
    // given
    final List<String> data =
        List.of(this.randomTextString(3), this.randomTextString(3), this.randomTextString(3));
    final HttpHeaders headers = new HttpHeaders();
    headers.add("ids", String.join(",", data));

    final String path = "/header/single/get/list";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data.toString().replaceAll(" ", "") + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getDefaultHeaderTest() throws Exception {
    // given
    final String data = this.randomTextString(10);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data);

    final String path = "/header/single/get/default";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"default\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getHttpHeadersTest() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data);

    final String path = "/header/http";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getMultiMapTest() throws Exception {
    // given
    final String data = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", data);

    final String path = "/header/multimap";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + data + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void getWithMultipleStringHeaderTest() throws Exception {
    // given
    final String id = this.randomTextString(5);
    final String version = this.randomTextString(5);
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", id);
    headers.add("version", version);

    final String path = "/header/multiple/get/header";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = "{\"header\":\"" + id + "_" + version + "\"}";

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
