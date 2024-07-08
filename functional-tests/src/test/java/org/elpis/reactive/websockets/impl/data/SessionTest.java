package org.elpis.reactive.websockets.impl.data;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.data.SessionResource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, SessionResource.class})
public class SessionTest extends BaseWebSocketTest {

    @Test
    void nonRequired() throws Exception {
        //given
        final String path = "/session/nonRequired";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, session -> session.receive()
                        .doOnNext(webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNextMatches(sessionId -> !sessionId.isEmpty())
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void required() throws Exception {
        //given
        final String path = "/session/required";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, session -> session.receive()
                        .doOnNext(webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNextMatches(sessionId -> !sessionId.isEmpty())
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void optional() throws Exception {
        //given
        final String path = "/session/optional";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, session -> session.receive()
                        .doOnNext(webSocketMessage -> sink.tryEmitValue(webSocketMessage.getPayloadAsText()))
                        .then())
                .subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNextMatches(sessionId -> !sessionId.isEmpty())
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }
}
