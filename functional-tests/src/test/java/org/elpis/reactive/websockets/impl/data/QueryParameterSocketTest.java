package org.elpis.reactive.websockets.impl.data;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.data.QueryParamSocketResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, QueryParamSocketResource.class})
class QueryParameterSocketTest extends BaseWebSocketTest {

    @Test
    void getWithStringQueryTest() throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/query/single/get/string?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericByteQueryTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/query/single/get/numeric/byte?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericPrimitiveByteQueryTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/query/single/get/numeric/primitive/byte?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericShortQueryTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/query/single/get/numeric/short?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericShortPrimitiveQueryTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/query/single/get/numeric/primitive/short?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericIntQueryTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/query/single/get/numeric/int?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericIntPrimitiveQueryTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/query/single/get/numeric/primitive/int?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericLongQueryTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/query/single/get/numeric/long?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericLongPrimitiveQueryTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/query/single/get/numeric/primitive/long?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBooleanQueryTest() throws Exception {
        //given
        final boolean data = true;

        final String path = "/query/single/get/boolean?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBooleanPrimitiveQueryTest() throws Exception {
        //given
        final boolean data = false;

        final String path = "/query/single/get/boolean/primitive?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericFloatQueryTest() throws Exception {
        //given
        final float data = this.getRandomFloat();

        final String path = "/query/single/get/numeric/float?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericFloatPrimitiveQueryTest() throws Exception {
        //given
        final float data = this.getRandomFloat();

        final String path = "/query/single/get/numeric/primitive/float?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericDoubleQueryTest() throws Exception {
        //given
        final double data = this.getRandomDouble();

        final String path = "/query/single/get/numeric/double?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericDoublePrimitiveQueryTest() throws Exception {
        //given
        final float data = getRandomFloat();

        final String path = "/query/single/get/numeric/primitive/double?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithCharacterWrapperQueryTest() throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/query/single/get/char?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"" + data.charAt(0) + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithCharPrimitiveQueryTest() throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/query/single/get/primitive/char?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"" + data.charAt(0) + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBigIntegerQueryTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/query/single/get/bigint?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBigDecimalQueryTest() throws Exception {
        //given
        final float data = this.getRandomFloat();

        final String path = "/query/single/get/bigdeci?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getEnumQueryTest() throws Exception {
        //given
        final BootStarter.Example data = BootStarter.Example.VOID;

        final String path = "/query/single/get/enum?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithStringQueryNoRequiredTest() throws Exception {
        //given
        final String data = this.randomTextString(10);

        final String path = "/query/single/get/no/required?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"null\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getListQueryTest() throws Exception {
        //given
        final List<String> data = List.of(this.randomTextString(3), this.randomTextString(3), this.randomTextString(3));

        final String path = "/query/single/get/list?ids=" + String.join(",", data);
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"" + data.toString().replaceAll(" ", "") + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getDefaultQueryTest() throws Exception {
        //given
        final String data = this.randomTextString(10);

        final String path = "/query/single/get/default?id=" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"query\":\"default\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }
}
