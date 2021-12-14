package org.elpis.socket.web.impl.data;

import nl.altindag.log.LogCaptor;
import org.elpis.reactive.websockets.config.WebSocketConfiguration;
import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.resource.data.MessageBodySocketResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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
                        .doOnNext(v -> sink.tryEmitNext(v.getPayloadAsText())))
                .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_GENERIC_TEST_FALLBACK.plus(DEFAULT_FAST_TEST_FALLBACK)))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getInfoLogs())
                .containsSequence(expected);
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
                        .doOnNext(v -> sink.tryEmitNext(v.getPayloadAsText())))
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

    @Test
    public void nonValidFlux() throws Exception {
        //given
        final String path = "/body/post/false";
        final Sinks.Many<String> sink = Sinks.many().replay().all();

        final String data = this.randomTextString(10);

        //expected
        final LogCaptor logCaptor = LogCaptor.forClass(WebSocketConfiguration.class);

        //test
        this.withClient(path, session -> session.send(Mono.just(data).map(session::textMessage))
                .thenMany(session.receive()
                        .doOnNext(v -> sink.tryEmitNext(v.getPayloadAsText())))
                .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getErrorLogs())
                .contains("Unable register outbound method `@Inbound nonValidFlux()` since it should accept " +
                        "Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `Flux<String>` was found instead");
    }

    @Test
    public void notFluxTest() throws Exception {
        //given
        final String path = "/body/post/not/flux";
        final Sinks.Many<String> sink = Sinks.many().replay().all();

        final String data = this.randomTextString(10);

        //expected
        final LogCaptor logCaptor = LogCaptor.forClass(WebSocketConfiguration.class);

        //test
        this.withClient(path, session -> session.send(Mono.just(data).map(session::textMessage))
                .thenMany(session.receive()
                        .doOnNext(v -> sink.tryEmitNext(v.getPayloadAsText())))
                .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getErrorLogs())
                .contains("Unable register outbound method `@Inbound notFlux()` since it should accept " +
                        "Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `List` was found instead");
    }
}
