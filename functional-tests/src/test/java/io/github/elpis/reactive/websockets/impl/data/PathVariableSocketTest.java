package io.github.elpis.reactive.websockets.impl.data;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.data.PathVariableSocketResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, PathVariableSocketResource.class})
class PathVariableSocketTest extends BaseWebSocketTest {

    @Test
    void getWithStringPathTest() throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/path/single/get/string/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericBytePathTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/path/single/get/numeric/byte/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericPrimitiveBytePathTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/path/single/get/numeric/primitive/byte/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericShortPathTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/path/single/get/numeric/short/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericShortPrimitivePathTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/path/single/get/numeric/primitive/short/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericIntPathTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/path/single/get/numeric/int/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericIntPrimitivePathTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/path/single/get/numeric/primitive/int/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericLongPathTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/path/single/get/numeric/long/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithNumericLongPrimitivePathTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/path/single/get/numeric/primitive/long/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBooleanPath() throws Exception {
        //given
        final boolean data = true;

        final String path = "/path/single/get/boolean/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithBooleanPrimitivePath() throws Exception {
        //given
        final boolean data = false;

        final String path = "/path/single/get/boolean/primitive/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getEnumPathTest() throws Exception {
        //given
        final BootStarter.Example data = BootStarter.Example.VOID;

        final String path = "/path/single/get/enum/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void getWithStringPathNoRequiredTest() throws Exception {
        //given
        final String data = this.randomTextString(10);

        final String path = "/path/single/get/no/required/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"null\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll (" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }
}
