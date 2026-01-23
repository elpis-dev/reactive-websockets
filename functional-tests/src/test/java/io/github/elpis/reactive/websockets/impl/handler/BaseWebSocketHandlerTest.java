package io.github.elpis.reactive.websockets.impl.handler;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.impl.TestWebSocketHandler;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, TestWebSocketHandler.class})
class BaseWebSocketHandlerTest extends BaseWebSocketTest {

  @Test
  void testProcessMessagesReceive() throws Exception {
    final String path = "/test/handler";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // test
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // verify - endpoint receives messages but doesn't respond
    StepVerifier.create(sink.asFlux().timeout(DEFAULT_FAST_TEST_FALLBACK))
        .expectNext("Alice: Hello, everyone!")
        .expectNext("Bob: Hi, Alice!")
        .expectNext("Charlie: Good morning!")
        .verifyError(TimeoutException.class);
  }
}
