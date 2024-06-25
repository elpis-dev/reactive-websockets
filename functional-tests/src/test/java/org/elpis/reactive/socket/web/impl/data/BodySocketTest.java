package org.elpis.reactive.socket.web.impl.data;

import nl.altindag.log.LogCaptor;
import org.elpis.reactive.socket.web.BaseWebSocketTest;
import org.elpis.reactive.socket.web.context.BootStarter;
import org.elpis.reactive.socket.web.context.resource.data.MessageBodySocketResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, MessageBodySocketResource.class})
public class BodySocketTest extends BaseWebSocketTest {

    @Test
    public void receiveDefaultMessageTestLong() throws Exception {
        //given
        final Flux<String> data = Flux.interval(Duration.ofMillis(100))
                .map(i -> "Entry " + i)
                .take(100);

        final String path = "/body/post";
        final Sinks.Many<String> sink = Sinks.many().replay().all();

        //expected
        final List<String> input = IntStream.range(0, 100)
                .boxed()
                .map(i -> "Received message: Entry " + i)
                .collect(Collectors.toList());

        final String[] expected = new String[input.size()];
        input.toArray(expected);


        final LogCaptor logCaptor = LogCaptor.forClass(MessageBodySocketResource.class);

        //test
        this.withClient(path, session -> session.send(data.map(session::textMessage))
                        .thenMany(session.receive()
                                .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK.plus(DEFAULT_FAST_TEST_FALLBACK)))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getInfoLogs())
                .containsSequence(expected);
    }

    @Test
    public void postBinaryMessage() throws Exception {
        //given
        final String path = "/body/post/binary";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, session -> session.receive()
                        .filter(webSocketMessage -> webSocketMessage.getType() == WebSocketMessage.Type.BINARY)
                        .doOnNext(webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext("Binary")
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void receiveErrorTest() throws Exception {
        //given
        final String path = "/body/post";
        final Sinks.Many<String> sink = Sinks.many().replay().all();
        final Sinks.One<String> errorSink = Sinks.one();

        final String data = this.randomTextString(10);

        final LogCaptor logCaptor = LogCaptor.forClass(MessageBodySocketResource.class);

        //test
        this.withClient(path, session -> session.send(Mono.error(new RuntimeException(data)))
                        .thenMany(session.receive()
                                .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                        .then())
                .doOnError(throwable -> errorSink.tryEmitValue(throwable.getMessage()))
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        StepVerifier.create(errorSink.asMono())
                .expectNext(data)
                .expectComplete()
                .verify();

        assertThat(logCaptor.getInfoLogs())
                .isEmpty();
    }
}
