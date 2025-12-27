package io.github.elpis.reactive.websockets.impl.connection;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.connection.CloseResource;
import io.github.elpis.reactive.websockets.context.resource.connection.CloseRoutingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({
  BaseWebSocketTest.PermitAllSecurityConfiguration.class,
  CloseRoutingConfiguration.class,
  CloseResource.class
})
public class CloseTest extends BaseWebSocketTest {

  @Test
  void normalCloseFromServer() throws Exception {
    // given
    final String path = "/close/normal";
    final Sinks.One<Integer> sink = Sinks.one();

    // test - server will close after 5 seconds
    // We need to subscribe to closeStatus first, then receive to keep connection alive
    this.withClient(
            path,
            session ->
                session
                    .closeStatus()
                    .doOnNext(closeStatus -> sink.tryEmitValue(closeStatus.getCode()))
                    .then(session.receive().then()))
        .subscribe();

    // verify - wait for server to close (5 seconds + buffer)
    StepVerifier.create(sink.asMono())
        .expectNext(CloseStatus.NORMAL.getCode())
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void goingAwayCloseFromServer() throws Exception {
    // given
    final String path = "/close/goingAway";
    final Sinks.One<Integer> sink = Sinks.one();

    // test - server will close after 5 seconds
    // We need to subscribe to closeStatus first, then receive to keep connection alive
    this.withClient(
            path,
            session ->
                session
                    .closeStatus()
                    .doOnNext(closeStatus -> sink.tryEmitValue(closeStatus.getCode()))
                    .then(session.receive().then()))
        .subscribe();

    // verify - wait for server to close (5 seconds + buffer)
    StepVerifier.create(sink.asMono())
        .expectNext(CloseStatus.GOING_AWAY.getCode())
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }
}
