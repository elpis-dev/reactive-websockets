package io.github.elpis.reactive.websockets.context.resource.flowcontrol;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.Backpressure.BackpressureStrategy;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@MessageEndpoint("/backpressure")
public class BackpressureResource {
  private static final Logger log = LoggerFactory.getLogger(BackpressureResource.class);

  /**
   * Endpoint with BUFFER strategy. Buffers up to 10 messages when client is slow. Produces 100
   * messages rapidly to test buffering behavior.
   */
  @OnMessage(value = "/buffer", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.BUFFER, bufferSize = 10)
  public Flux<String> bufferStrategy() {
    log.info("Buffer backpressure endpoint called");
    return Flux.range(1, 100)
        .delayElements(Duration.ofMillis(10))
        .map(i -> "Buffer-" + i)
        .doOnNext(msg -> log.trace("Emitting: {}", msg));
  }

  /**
   * Endpoint with DROP_OLDEST strategy. Keeps only the latest message when overwhelmed. Useful for
   * real-time dashboards where only the current state matters.
   */
  @OnMessage(value = "/drop-oldest", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.DROP_OLDEST)
  public Flux<String> dropOldestStrategy() {
    log.info("Drop oldest backpressure endpoint called");
    return Flux.range(1, 100)
        .delayElements(Duration.ofMillis(10))
        .map(i -> "DropOldest-" + i)
        .doOnNext(msg -> log.trace("Emitting: {}", msg));
  }

  /**
   * Endpoint with DROP_LATEST strategy. Drops new messages when client can't keep up. Preserves
   * older messages and maintains order.
   */
  @OnMessage(value = "/drop-latest", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.DROP_LATEST)
  public Flux<String> dropLatestStrategy() {
    log.info("Drop latest backpressure endpoint called");
    return Flux.range(1, 100)
        .delayElements(Duration.ofMillis(10))
        .map(i -> "DropLatest-" + i)
        .doOnNext(msg -> log.trace("Emitting: {}", msg));
  }

  /**
   * Endpoint with ERROR strategy. Signals an error when backpressure occurs. Client connection
   * should terminate when overwhelmed.
   */
  @OnMessage(value = "/error", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.ERROR)
  public Flux<String> errorStrategy() {
    log.info("Error backpressure endpoint called");
    return Flux.range(1, 100)
        .delayElements(Duration.ofMillis(10))
        .map(i -> "Error-" + i)
        .doOnNext(msg -> log.trace("Emitting: {}", msg));
  }

  /** Endpoint with backpressure disabled. Uses default WebFlux backpressure handling. */
  @OnMessage(value = "/disabled", mode = Mode.BROADCAST)
  @Backpressure(enabled = false)
  public Flux<String> disabledBackpressure() {
    log.info("Disabled backpressure endpoint called");
    return Flux.range(1, 50).delayElements(Duration.ofMillis(10)).map(i -> "Disabled-" + i);
  }

  /** Endpoint with custom buffer size. Tests configurable buffer capacity. */
  @OnMessage(value = "/buffer-custom", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.BUFFER, bufferSize = 50)
  public Flux<String> customBufferSize() {
    log.info("Custom buffer size backpressure endpoint called");
    return Flux.range(1, 200)
        .delayElements(Duration.ofMillis(5))
        .map(i -> "CustomBuffer-" + i)
        .doOnNext(msg -> log.trace("Emitting: {}", msg));
  }

  /**
   * High-throughput endpoint for stress testing. Emits 1000 messages very quickly to test
   * backpressure under load.
   */
  @OnMessage(value = "/high-throughput", mode = Mode.BROADCAST)
  @Backpressure(strategy = BackpressureStrategy.BUFFER, bufferSize = 100)
  public Flux<String> highThroughput() {
    log.info("High throughput backpressure endpoint called");
    return Flux.range(1, 1000).delayElements(Duration.ofMillis(1)).map(i -> "HighThroughput-" + i);
  }
}
