package org.elpis.socket.web.impl.security;

import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import(BaseWebSocketTest.PermitAllSecurityConfiguration.class)
public class AnonymousAuthenticationTest extends BaseWebSocketTest {

    @Test
    public void anonymousAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/security/anonymous";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"anonymous\":true}";

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
