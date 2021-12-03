package org.elpis.socket.web.impl.data;

import nl.altindag.log.LogCaptor;
import org.elpis.reactive.websockets.config.WebSocketConfiguration;
import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.resource.data.MessageBodySocketResource;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.PERMIT_ALL})
public class BodySocketTest extends BaseWebSocketTest {

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

        assertThat(logCaptor.getDebugLogs())
                .anyMatch(error -> error.matches("Closing WebSocketSession .+ on signal cancel"));
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
                        "Flux<WebSocketMessage> instance, but `interface java.util.List` was found instead");
    }
}
