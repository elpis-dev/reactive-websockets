package io.github.elpis.reactive.websockets.functional.routing;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.routing.RoutingConfiguration;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.context.security.model.TestConstants;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, RoutingConfiguration.class})
class RoutingTest extends BaseWebSocketTest {

  @Test
  void testPublishRouteWithHeader() throws Exception {
    // given
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", TestConstants.TEST_VALUE);

    final String path = "/routing/publish";
    final Sinks.One<String> sink = Sinks.one();

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(value -> sink.tryEmitValue(value.replaceAll(" ", "")))
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(TestConstants.TEST_VALUE)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void testListenRouteWithHeader() throws Exception {
    // given
    final Flux<String> data = Flux.interval(Duration.ofMillis(100)).map(i -> "Entry " + i).take(5);

    final String path = "/routing/listen";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test - POST-style endpoint receives messages but doesn't respond
    this.withClient(
            path,
            session ->
                session
                    .send(data.map(session::textMessage))
                    .thenMany(
                        session
                            .receive()
                            .doOnNext(value -> sink.tryEmitNext(value.getPayloadAsText())))
                    .then())
        .subscribe();

    // verify - timeout confirms messages were processed without responses
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }

  @Test
  void testConnectRouteWithHeader() throws Exception {
    // given
    final HttpHeaders headers = new HttpHeaders();
    headers.add("id", TestConstants.TEST_VALUE);

    final String path = "/routing/connect";
    final Sinks.One<String> sink = Sinks.one();

    // test - connect endpoint establishes connection
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitValue)
                    .then())
        .subscribe();

    // verify - timeout confirms connection was established without response messages
    StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .verifyError(TimeoutException.class);
  }
}
