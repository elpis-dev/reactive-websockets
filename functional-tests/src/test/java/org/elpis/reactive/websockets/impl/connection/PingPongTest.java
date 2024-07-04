package org.elpis.reactive.websockets.impl.connection;

import nl.altindag.log.LogCaptor;
import org.assertj.core.api.Condition;
import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.config.handler.BroadcastWebSocketResourceHandler;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.connection.PingPongResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, PingPongResource.class})
public class PingPongTest extends BaseWebSocketTest {

    @Test
    void pingPong() throws Exception {
        //given
        final String path = "/connection/ping";

        final AtomicInteger pongs = new AtomicInteger();

        //test
        final Mono<Void> chain = this.withClient(path, session -> {
                    final Flux<WebSocketMessage> clientPings = Flux.interval(Duration.ofMillis(1000L))
                            .map(aLong -> session.pingMessage(dataBufferFactory -> session.bufferFactory().allocateBuffer(256)));

                    final Flux<WebSocketMessage> receive = session.receive()
                            .doOnNext(webSocketMessage -> {
                                if (webSocketMessage.getType() == WebSocketMessage.Type.PONG) {
                                    pongs.incrementAndGet();
                                }
                            });

                    return Flux.merge(receive.then(), session.send(clientPings)).then();
                })
                .timeout(DEFAULT_GENERIC_TEST_FALLBACK);

        final LogCaptor logCaptor = LogCaptor.forRoot();

        //verify
        StepVerifier.create(chain)
                .verifyError(TimeoutException.class);

        assertThat(pongs)
                .hasValue(9);

        assertThat(logCaptor.getInfoLogs()
                .stream().filter(log -> log.contains("Got PONG response from client")).count())
                .isEqualTo(9);
    }
}
