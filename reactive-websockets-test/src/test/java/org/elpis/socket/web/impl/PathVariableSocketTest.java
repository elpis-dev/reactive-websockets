package org.elpis.socket.web.impl;

import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
public class PathVariableSocketTest extends BaseWebSocketTest {

    @MockBean
    private WebSocketService webSocketService;

    @BeforeEach
    public void each() {
        doAnswer(answer -> {
            final ServerWebExchange serverWebExchange = answer.getArgument(0);
            final WebSocketHandler webSocketHandler = answer.getArgument(1);

            return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy())
                    .handleRequest(serverWebExchange, webSocketHandler);
        }).when(webSocketService).handleRequest(any(ServerWebExchange.class), any(WebSocketHandler.class));
    }

    @Test
    public void getWithStringPathTest() throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/path/single/get/string/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithStringPathNoValueTest(final CapturedOutput output) throws Exception {
        //given
        final String data = this.randomTextString(5);

        final String path = "/path/single/get/no/string/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(output)
                .contains("Path parameter `@SocketPathVariable ids` at method `getWithNoStringPath()` was marked as " +
                        "`required` but was not found on request");
    }

    @Test
    public void getWithNumericBytePathTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/path/single/get/numeric/byte/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericPrimitiveBytePathTest() throws Exception {
        //given
        final byte data = this.getRandomByte();

        final String path = "/path/single/get/numeric/primitive/byte/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericShortPathTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/path/single/get/numeric/short/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericShortPrimitivePathTest() throws Exception {
        //given
        final short data = this.getRandomShort();

        final String path = "/path/single/get/numeric/primitive/short/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericIntPathTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/path/single/get/numeric/int/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericIntPrimitivePathTest() throws Exception {
        //given
        final int data = this.getRandomInteger();

        final String path = "/path/single/get/numeric/primitive/int/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericLongPathTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/path/single/get/numeric/long/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithNumericLongPrimitivePathTest() throws Exception {
        //given
        final long data = this.getRandomLong();

        final String path = "/path/single/get/numeric/primitive/long/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithBooleanPath() throws Exception {
        //given
        final boolean data = true;

        final String path = "/path/single/get/boolean/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithBooleanPrimitivePath() throws Exception {
        //given
        final boolean data = false;

        final String path = "/path/single/get/boolean/primitive/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":" + data + "}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getEnumPathTest() throws Exception {
        //given
        final BootStarter.Test data = BootStarter.Test.VOID;

        final String path = "/path/single/get/enum/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"" + data + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
    public void getWithStringPathNoRequiredTest() throws Exception {
        //given
        final String data = this.randomTextString(10);

        final String path = "/path/single/get/no/required/" + data;
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"path\":\"null\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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
