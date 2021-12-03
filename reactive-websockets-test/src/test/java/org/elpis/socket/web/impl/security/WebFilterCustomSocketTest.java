package org.elpis.socket.web.impl.security;

import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.elpis.socket.web.context.security.model.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.GENERIC, SecurityProfiles.WEB_FILTER_ATTRIBUTE})
public class WebFilterCustomSocketTest extends BaseWebSocketTest {

    @Test
    public void withWebSocketPrincipalTest() throws Exception {
        //given
        final String path = "/auth/filter/withWebSocketPrincipal";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestConstants.TEST_VALUE;

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
    public void withExtractedAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/filter/withExtractedAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestConstants.TEST_VALUE;

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

}
