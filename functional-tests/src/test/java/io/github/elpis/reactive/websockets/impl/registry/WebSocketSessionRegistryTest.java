package io.github.elpis.reactive.websockets.impl.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.controller.SessionRegistryController;
import io.github.elpis.reactive.websockets.context.resource.registry.WebSocketRegistryResource;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Functional tests for {@link WebSocketSessionRegistry}.
 *
 * <p>These tests verify the registry's ability to track sessions, count them, and perform cleanup
 * operations.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({
  BaseWebSocketTest.PermitAllSecurityConfiguration.class,
  SessionRegistryController.class,
  WebSocketRegistryResource.class
})
class WebSocketSessionRegistryTest extends BaseWebSocketTest {
  private static final int EXPECTED_SESSION_COUNT = 3;

  @Test
  void testGetTotalSessionCountPerPath() throws Exception {
    final String path = "/registry/count";

    final CountDownLatch latch = new CountDownLatch(EXPECTED_SESSION_COUNT);

    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    this.getWebClient()
        .post()
        .uri("/sessionRegistry/count")
        .bodyValue("/registry/count")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Long.class)
        .isEqualTo((long) EXPECTED_SESSION_COUNT);
  }

  @Test
  void testGetTotalSessionCount() throws Exception {
    final String pathOne = "/registry/totalCount/one";
    final String pathTwo = "/registry/totalCount/two";

    final CountDownLatch latch = new CountDownLatch(EXPECTED_SESSION_COUNT);

    withClient(pathOne, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(pathOne, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(pathTwo, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    this.getWebClient()
        .get()
        .uri("/sessionRegistry/totalCount")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Long.class)
        .isEqualTo((long) EXPECTED_SESSION_COUNT);
  }

  @Test
  void testGetActivePaths() throws Exception {
    final String pathOne = "/registry/active/one";
    final String pathTwo = "/registry/active/two";

    final CountDownLatch latch = new CountDownLatch(EXPECTED_SESSION_COUNT);

    withClient(pathOne, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(pathOne, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    withClient(pathTwo, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    Flux<String> result =
        this.getWebClient()
            .get()
            .uri("/sessionRegistry/paths")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseBody();

    StepVerifier.create(result)
        .expectNext("/registry/active/one")
        .expectNext("/registry/active/two")
        .verifyComplete();
  }

  @Test
  void testShutdown() throws Exception {
    final String path = "/registry/shutdown";

    final CountDownLatch latch = new CountDownLatch(EXPECTED_SESSION_COUNT);

    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();
    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();
    withClient(path, session -> session.receive().doOnSubscribe(__ -> latch.countDown()).then())
        .subscribe();

    assertThat(latch.await(DEFAULT_FAST_TEST_FALLBACK.getSeconds(), TimeUnit.SECONDS)).isTrue();

    this.getWebClient()
        .post()
        .uri("/sessionRegistry/count")
        .bodyValue("/registry/shutdown")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Long.class)
        .isEqualTo((long) EXPECTED_SESSION_COUNT);

    this.getWebClient().delete().uri("/sessionRegistry/shutdown").exchange().expectStatus().isOk();

    this.getWebClient()
        .post()
        .uri("/sessionRegistry/count")
        .bodyValue("/registry/shutdown")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Long.class)
        .isEqualTo(0L);
  }
}
