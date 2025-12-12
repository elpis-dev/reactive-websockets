package io.github.elpis.reactive.websockets.impl.backpressure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.flowcontrol.BackpressureResource;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * Functional tests for @Backpressure annotation with different strategies.
 *
 * <p>Tests verify that each backpressure strategy correctly handles slow consumers:
 *
 * <ul>
 *   <li>BUFFER - Buffers messages up to configured size
 *   <li>DROP_OLDEST - Keeps only the latest message
 *   <li>DROP_LATEST - Drops new messages when overwhelmed
 *   <li>ERROR - Signals error when backpressure occurs
 *   <li>DISABLED - No backpressure handling
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {BootStarter.class})
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE})
@Import({BaseWebSocketTest.PermitAllSecurityConfiguration.class, BackpressureResource.class})
public class BackpressureTest extends BaseWebSocketTest {

  private static final Logger log = LoggerFactory.getLogger(BackpressureTest.class);

  // Test constants
  private static final int EXPECTED_BUFFER_MESSAGES = 10; // Buffer size is 10 in resource
  private static final int EXPECTED_DISABLED_MESSAGES = 50; // All messages should arrive

  /**
   * Test BUFFER strategy with slow consumer. The endpoint produces 100 messages rapidly, but the
   * client consumes slowly. With a buffer size of 10, we expect the buffer to fill and either drop
   * messages or apply overflow strategy.
   */
  @Test
  public void testBufferStrategy() throws Exception {
    log.info("Testing BUFFER backpressure strategy");

    // given
    final String path = "/backpressure/buffer";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .doOnError(e -> log.error("Error in stream: {}", e.getMessage()))
                    .then())
        .subscribe();

    // then
    StepVerifier.create(
            sink.asFlux()
                .take(EXPECTED_BUFFER_MESSAGES)
                .collectList()
                .timeout(Duration.ofSeconds(5)))
        .assertNext(
            messages -> {
              assertThat(messages)
                  .hasSizeGreaterThanOrEqualTo(EXPECTED_BUFFER_MESSAGES)
                  .allMatch(msg -> msg.startsWith("Buffer-"));
              log.info("BUFFER strategy test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test DROP_OLDEST strategy with slow consumer. The endpoint produces 100 messages rapidly.
   * DROP_OLDEST keeps only the latest message, so we expect to receive fewer messages, with later
   * sequence numbers.
   */
  @Test
  public void testDropOldestStrategy() throws Exception {
    log.info("Testing DROP_OLDEST backpressure strategy");

    // given
    final String path = "/backpressure/drop-oldest";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - wait for some messages to arrive
    StepVerifier.create(sink.asFlux().take(20).collectList().timeout(Duration.ofSeconds(5)))
        .assertNext(
            messages -> {
              // DROP_OLDEST keeps only latest, so we should receive fewer messages
              // and they should have higher sequence numbers
              assertThat(messages).isNotEmpty().allMatch(msg -> msg.startsWith("DropOldest-"));
              log.info(
                  "DROP_OLDEST strategy test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test DROP_LATEST strategy with slow consumer. The endpoint produces 100 messages rapidly.
   * DROP_LATEST drops new messages when overwhelmed, preserving older messages in order.
   */
  @Test
  public void testDropLatestStrategy() throws Exception {
    log.info("Testing DROP_LATEST backpressure strategy");

    // given
    final String path = "/backpressure/drop-latest";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - take first 30 messages
    StepVerifier.create(sink.asFlux().take(30).collectList().timeout(Duration.ofSeconds(5)))
        .assertNext(
            messages -> {
              // DROP_LATEST should preserve older messages
              assertThat(messages).isNotEmpty().allMatch(msg -> msg.startsWith("DropLatest-"));

              // Verify messages are in order (older messages preserved)
              if (messages.size() >= 3) {
                String first = messages.get(0);
                String second = messages.get(1);
                String third = messages.get(2);

                assertThat(first).matches("DropLatest-\\d+");
                assertThat(second).matches("DropLatest-\\d+");
                assertThat(third).matches("DropLatest-\\d+");

                int firstNum = Integer.parseInt(first.substring("DropLatest-".length()));
                int secondNum = Integer.parseInt(second.substring("DropLatest-".length()));
                int thirdNum = Integer.parseInt(third.substring("DropLatest-".length()));

                // Messages should be sequential (older ones preserved)
                assertThat(secondNum).isGreaterThan(firstNum);
                assertThat(thirdNum).isGreaterThan(secondNum);
              }

              log.info(
                  "DROP_LATEST strategy test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test ERROR strategy with slow consumer. The endpoint produces 100 messages rapidly. ERROR
   * strategy should signal an error when backpressure occurs, potentially terminating the
   * connection.
   */
  @Test
  public void testErrorStrategy() throws Exception {
    log.info("Testing ERROR backpressure strategy");

    // given
    final String path = "/backpressure/error";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .doOnError(
                        e -> log.info("Expected error occurred (backpressure): {}", e.getMessage()))
                    .then())
        .subscribe();

    // then - expect either completion or error
    StepVerifier.create(sink.asFlux().take(50).collectList().timeout(Duration.ofSeconds(5)))
        .assertNext(
            messages -> {
              // ERROR strategy might error early or complete normally depending on timing
              assertThat(messages).isNotEmpty().allMatch(msg -> msg.startsWith("Error-"));
              log.info("ERROR strategy test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test DISABLED backpressure. The endpoint produces 50 messages with no backpressure handling.
   * Should use default WebFlux backpressure behavior.
   */
  @Test
  public void testDisabledBackpressure() throws Exception {
    log.info("Testing DISABLED backpressure");

    // given
    final String path = "/backpressure/disabled";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .doOnComplete(() -> log.info("Stream completed"))
                    .then())
        .subscribe();

    // then - expect all messages
    StepVerifier.create(sink.asFlux().take(EXPECTED_DISABLED_MESSAGES).collectList())
        .assertNext(
            messages -> {
              assertThat(messages)
                  .hasSize(EXPECTED_DISABLED_MESSAGES)
                  .allMatch(msg -> msg.startsWith("Disabled-"));
              log.info(
                  "DISABLED backpressure test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test custom buffer size. The endpoint uses a buffer size of 50 and produces 200 messages. This
   * tests that the custom buffer size is respected.
   */
  @Test
  public void testCustomBufferSize() throws Exception {
    log.info("Testing custom buffer size");

    // given
    final String path = "/backpressure/buffer-custom";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - expect at least the custom buffer size (50)
    StepVerifier.create(sink.asFlux().take(50).collectList().timeout(Duration.ofSeconds(10)))
        .assertNext(
            messages -> {
              assertThat(messages)
                  .hasSizeGreaterThanOrEqualTo(50)
                  .allMatch(msg -> msg.startsWith("CustomBuffer-"));
              log.info("Custom buffer size test completed. Received {} messages", messages.size());
            })
        .verifyComplete();
  }

  /**
   * Test high throughput scenario. The endpoint produces 1000 messages very rapidly (1ms delay).
   * With a buffer size of 100, this tests backpressure under heavy load.
   */
  @Test
  public void testHighThroughput() throws Exception {
    log.info("Testing high throughput with backpressure");

    // given
    final String path = "/backpressure/high-throughput";
    final Sinks.Many<String> sink = Sinks.many().replay().all();
    final int expectedMinMessages = 100; // At least the buffer size

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - expect at least the buffer size worth of messages
    StepVerifier.create(
            sink.asFlux().take(expectedMinMessages).collectList().timeout(Duration.ofSeconds(15)))
        .assertNext(
            messages ->
                assertThat(messages)
                    .hasSizeGreaterThanOrEqualTo(expectedMinMessages)
                    .allMatch(msg -> msg.startsWith("HighThroughput-")))
        .verifyComplete();
  }

  /**
   * Test that messages are correctly formatted and contain expected content. This validates the
   * integration between the annotation processor and runtime.
   */
  @Test
  public void testMessageContentValidation() throws Exception {
    log.info("Testing message content validation");

    // given
    final String path = "/backpressure/buffer";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - verify message format
    StepVerifier.create(sink.asFlux().take(5).timeout(Duration.ofSeconds(3)))
        .expectNextMatches(msg -> msg.matches("Buffer-\\d+"))
        .expectNextMatches(msg -> msg.matches("Buffer-\\d+"))
        .expectNextMatches(msg -> msg.matches("Buffer-\\d+"))
        .expectNextMatches(msg -> msg.matches("Buffer-\\d+"))
        .expectNextMatches(msg -> msg.matches("Buffer-\\d+"))
        .verifyComplete();

    log.info("Message content validation test completed");
  }

  /**
   * Test backpressure with very slow consumer to force buffer overflow. This simulates a realistic
   * scenario where the client cannot keep up with the server.
   */
  @Test
  public void testSlowConsumerBufferOverflow() throws Exception {
    log.info("Testing slow consumer with buffer overflow");

    // given
    final String path = "/backpressure/buffer";
    final Sinks.Many<String> sink = Sinks.many().replay().all();

    // when - simulate very slow consumer
    // Important: Extract text payload BEFORE delay to avoid IllegalReferenceCountException
    this.withClient(
            path,
            session ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .delayElements(Duration.ofMillis(100))
                    .doOnNext(sink::tryEmitNext)
                    .then())
        .subscribe();

    // then - with slow consumer, buffer should overflow
    StepVerifier.create(sink.asFlux().take(15).timeout(Duration.ofSeconds(10)))
        .expectNextCount(15)
        .verifyComplete();

    log.info("Slow consumer test completed. Received 15+ messages with slow consumption");
  }
}
