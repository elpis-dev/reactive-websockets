package org.elpis.socket.web.impl.data;

import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
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

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import(BaseWebSocketTest.PermitAllSecurityConfiguration.class)
public class HeaderSocketTest extends BaseWebSocketTest {

    @Test
    public void getWithStringHeaderTest() throws Exception {
        //given
        final String data = this.randomTextString(5);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", data);

        final String path = "/header/single/get/header";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"" + data + "\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithStringHeaderNoValueTest(final CapturedOutput output) throws Exception {
        //given
        final String data = this.randomTextString(5);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", data);

        final String path = "/header/single/get/no/string";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(output)
                .contains("Request header `@SocketHeader ids` at method `getWithNoStringHeader()` was marked " +
                        "as `required` but was not found on request");
    }

    @Test
    public void getWithNumericByteHeaderTest() throws Exception {
        //given
        final byte data = this.getRandomByte();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/byte";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericPrimitiveByteHeaderTest() throws Exception {
        //given
        final byte data = this.getRandomByte();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/byte";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericShortHeaderTest() throws Exception {
        //given
        final short data = this.getRandomShort();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/short";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericShortPrimitiveHeaderTest() throws Exception {
        //given
        final short data = this.getRandomShort();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/short";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericIntHeaderTest() throws Exception {
        //given
        final int data = this.getRandomInteger();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/int";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericIntPrimitiveHeaderTest() throws Exception {
        //given
        final int data = this.getRandomInteger();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/int";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericLongHeaderTest() throws Exception {
        //given
        final long data = this.getRandomLong();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/long";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericLongPrimitiveHeaderTest() throws Exception {
        //given
        final long data = this.getRandomLong();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/long";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithBooleanHeaderTest() throws Exception {
        //given
        final boolean data = true;
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/boolean";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithBooleanPrimitiveHeaderTest() throws Exception {
        //given
        final boolean data = false;
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/boolean/primitive";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericFloatHeaderTest() throws Exception {
        //given
        final float data = this.getRandomFloat();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/float";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericFloatPrimitiveHeaderTest() throws Exception {
        //given
        final float data = this.getRandomFloat();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/float";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericDoubleHeaderTest() throws Exception {
        //given
        final double data = this.getRandomDouble();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/double";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithNumericDoublePrimitiveHeaderTest() throws Exception {
        //given
        final double data = getRandomDouble();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/numeric/primitive/double";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithCharacterWrapperHeaderTest() throws Exception {
        //given
        final String data = this.randomTextString(5);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/char";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"" + data.charAt(0) + "\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithCharPrimitiveHeaderTest() throws Exception {
        //given
        final String data = this.randomTextString(5);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/primitive/char";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"" + data.charAt(0) + "\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithBigIntegerHeaderTest() throws Exception {
        //given
        final int data = this.getRandomInteger();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/bigint";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithBigDecimalHeaderTest() throws Exception {
        //given
        final float data = this.getRandomFloat();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/bigdeci";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":" + data + "}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getEnumHeaderTest() throws Exception {
        //given
        final BootStarter.Example data = BootStarter.Example.VOID;
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", data.name());

        final String path = "/header/single/get/enum";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"" + data + "\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getWithStringHeaderNoRequiredTest() throws Exception {
        //given
        final String data = this.randomTextString(10);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", String.valueOf(data));

        final String path = "/header/single/get/no/required";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"null\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getListHeaderTest() throws Exception {
        //given
        final List<String> data = List.of(this.randomTextString(3), this.randomTextString(3), this.randomTextString(3));
        final HttpHeaders headers = new HttpHeaders();
        headers.add("ids", String.join(",", data));

        final String path = "/header/single/get/list";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"" + data.toString().replaceAll(" ", "") + "\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void getDefaultHeaderTest() throws Exception {
        //given
        final String data = this.randomTextString(10);
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", data);

        final String path = "/header/single/get/default";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"header\":\"default\"}";

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }
}
