package org.elpis.reactive.websockets.impl.routing;

import nl.altindag.log.LogCaptor;
import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.RoutingConfiguration;
import org.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import org.elpis.reactive.websockets.context.security.model.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, RoutingConfiguration.class})
class RoutingTest extends BaseWebSocketTest {

    @Test
    void testPublishRouteWithHeader() throws Exception {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", TestConstants.TEST_VALUE);

        final String path = "/routing/publish";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(TestConstants.TEST_VALUE)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void testListenRouteWithHeader() throws Exception {
        //given
        final Flux<String> data = Flux.interval(Duration.ofMillis(100))
                .map(i -> "Entry " + i)
                .take(5);

        final String path = "/routing/listen";
        final Sinks.Many<String> sink = Sinks.many().replay().all();

        //expected
        final List<String> input = IntStream.range(0, 5)
                .boxed()
                .map(i -> "Received Entry " + i + " from '/routing/get'")
                .toList();

        final String[] expected = new String[input.size()];
        input.toArray(expected);

        final LogCaptor logCaptor = LogCaptor.forClass(RoutingConfiguration.class);

        //test
        this.withClient(path, session -> session.send(data.map(session::textMessage))
                        .thenMany(session.receive()
                                .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getInfoLogs())
                .containsSequence(expected);
    }

    @Test
    void testConnectRouteWithHeader() throws Exception {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add("id", TestConstants.TEST_VALUE);

        final String path = "/routing/connect";

        final LogCaptor logCaptor = LogCaptor.forClass(RoutingConfiguration.class);

        //test
        this.withClient(path, headers, (session) -> session.receive().then())
                .subscribe();

        //verify
        StepVerifier.create(Flux.just(path).delayElements(DEFAULT_FAST_TEST_FALLBACK).timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(logCaptor.getInfoLogs())
                .contains("Connected with header " + TestConstants.TEST_VALUE);
    }
}
