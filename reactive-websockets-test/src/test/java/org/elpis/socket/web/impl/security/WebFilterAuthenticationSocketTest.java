package org.elpis.socket.web.impl.security;

import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.elpis.socket.web.context.security.model.TestPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.GENERIC, SecurityProfiles.WEB_FILTER_AUTHORIZATION})
public class WebFilterAuthenticationSocketTest extends BaseWebSocketTest {

    @Test
    public void withPrincipalTest() throws Exception {
        //given
        final String path = "/auth/filter/withPrincipal";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestPrincipal.class.getName();

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(sink::tryEmitValue)
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    public void withAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/filter/withAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"principal\":\"" + TestPrincipal.class.getName() + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

}
