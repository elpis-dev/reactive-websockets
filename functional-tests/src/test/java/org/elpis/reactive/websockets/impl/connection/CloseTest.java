package org.elpis.reactive.websockets.impl.connection;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.connection.CloseRoutingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, CloseRoutingConfiguration.class})
public class CloseTest extends BaseWebSocketTest {

    @Test
    void normalCloseFromServer() throws Exception {
        //given
        final String path = "/close";
        final Sinks.One<Integer> sink = Sinks.one();

        //test
        this.withClient(path, session -> session.closeStatus()
                        .doOnNext(closeStatus -> sink.tryEmitValue(closeStatus.getCode()))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(CloseStatus.NORMAL.getCode())
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }
}
