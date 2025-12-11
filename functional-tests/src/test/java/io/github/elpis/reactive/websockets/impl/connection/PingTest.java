package io.github.elpis.reactive.websockets.impl.connection;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.connection.PingResource;
import io.github.elpis.reactive.websockets.context.resource.connection.PingRoutingConfiguration;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({
  BaseWebSocketTest.PermitAllSecurityConfiguration.class,
  PingResource.class,
  PingRoutingConfiguration.class
})
public class PingTest extends BaseWebSocketTest {

  @Test
  void ping() throws Exception {
    // given
    final String path = "/connection/ping";

    final AtomicInteger pongs = new AtomicInteger();

    // test
    final Mono<Void> chain =
        this.withClient(
                path,
                session -> {
                  final Flux<WebSocketMessage> clientPings =
                      Flux.interval(Duration.ofMillis(1000L))
                          .map(
                              aLong ->
                                  session.pingMessage(
                                      dataBufferFactory ->
                                          session.bufferFactory().allocateBuffer(256)));

                  final Flux<WebSocketMessage> receive =
                      session
                          .receive()
                          .doOnNext(
                              webSocketMessage -> {
                                if (webSocketMessage.getType() == WebSocketMessage.Type.PONG) {
                                  pongs.incrementAndGet();
                                }
                              });

                  return Flux.merge(receive.then(), session.send(clientPings)).then();
                })
            .timeout(DEFAULT_GENERIC_TEST_FALLBACK);

    // verify
    StepVerifier.create(chain).verifyError(TimeoutException.class);

    assertThat(pongs).hasPositiveValue();
  }
}
