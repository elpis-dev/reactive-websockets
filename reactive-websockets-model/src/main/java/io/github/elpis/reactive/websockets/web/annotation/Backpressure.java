package io.github.elpis.reactive.websockets.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Backpressure configuration for WebSocket endpoints. Controls behavior when clients are slow to
 * consume messages in high-throughput streams.
 *
 * <p>Provides four strategies for handling backpressure:
 *
 * <ul>
 *   <li>BUFFER - Buffer messages up to a configurable size
 *   <li>DROP_OLDEST - Keep only the latest message when overwhelmed
 *   <li>DROP_LATEST - Drop new messages when overwhelmed
 *   <li>ERROR - Propagate error when backpressure occurs
 * </ul>
 *
 * <p>Can be applied at:
 *
 * <ul>
 *   <li>Class level - applies to all methods in the @MessageEndpoint
 *   <li>Method level - applies to specific @OnMessage method, overrides class-level
 * </ul>
 *
 * <p>Precedence (highest to lowest):
 *
 * <ol>
 *   <li>@Backpressure on method
 *   <li>@Backpressure on class
 *   <li>Disabled (default)
 * </ol>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @MessageEndpoint("/stream")
 * public class StreamResource {
 *
 *   @OnMessage
 *   @Backpressure(strategy = BackpressureStrategy.BUFFER, bufferSize = 1000)
 *   public Flux<String> highThroughputStream() {
 *     return Flux.interval(Duration.ofMillis(1)).map(String::valueOf);
 *   }
 *
 *   @OnMessage
 *   @Backpressure(strategy = BackpressureStrategy.DROP_LATEST)
 *   public Flux<String> realTimeUpdates() {
 *     return eventStream.asFlux();
 *   }
 * }
 * }</pre>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Backpressure {

  /**
   * The backpressure strategy to apply when the client is slow to consume messages.
   *
   * @return the backpressure strategy
   */
  BackpressureStrategy strategy() default BackpressureStrategy.BUFFER;

  /**
   * The maximum number of messages to buffer when using BUFFER strategy. This setting is ignored
   * for other strategies.
   *
   * <p>Default is 256 messages, which provides a good balance between memory usage and tolerance
   * for temporary slowdowns. Consider increasing for bursty traffic or decreasing for
   * memory-constrained environments.
   *
   * @return the buffer size
   */
  int bufferSize() default 256;

  /**
   * Whether backpressure handling is enabled.
   *
   * @return true if enabled, false otherwise
   */
  boolean enabled() default true;

  /**
   * Enum defining backpressure strategies.
   *
   * <p>Each strategy uses a corresponding Project Reactor operator:
   *
   * <ul>
   *   <li>BUFFER: {@code onBackpressureBuffer(bufferSize)}
   *   <li>DROP_OLDEST: {@code onBackpressureLatest()} - keeps only latest
   *   <li>DROP_LATEST: {@code onBackpressureDrop()} - drops new messages
   *   <li>ERROR: {@code onBackpressureError()} - propagates exception
   * </ul>
   */
  enum BackpressureStrategy {
    /**
     * Buffer messages up to bufferSize. When buffer is full, behavior depends on overflow strategy
     * (typically drops oldest or errors).
     *
     * <p>Use when: Temporary slowdowns are expected and message delivery is important.
     *
     * <p>Memory impact: High (bufferSize * avg message size)
     */
    BUFFER,

    /**
     * Keep only the latest message, dropping older unprocessed messages. Client always receives the
     * most recent data.
     *
     * <p>Use when: Only the latest state matters (e.g., real-time dashboards, live prices).
     *
     * <p>Memory impact: Minimal (only 1 message buffered)
     */
    DROP_OLDEST,

    /**
     * Drop new messages when the client can't keep up. Older messages are preserved and delivered
     * in order.
     *
     * <p>Use when: Message ordering and completeness matter more than recency.
     *
     * <p>Memory impact: Minimal (no buffering)
     */
    DROP_LATEST,

    /**
     * Signal an error when backpressure occurs. The connection may be terminated depending on error
     * handling configuration.
     *
     * <p>Use when: Message loss is unacceptable and the client must keep up.
     *
     * <p>Memory impact: None
     */
    ERROR
  }
}
