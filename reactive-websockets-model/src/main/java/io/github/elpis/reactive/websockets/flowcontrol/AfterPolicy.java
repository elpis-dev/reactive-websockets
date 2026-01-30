package io.github.elpis.reactive.websockets.flowcontrol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that this flow control policy should be applied after another policy.
 *
 * <p>This annotation is used to define ordering constraints between {@link FlowControlPolicy}
 * implementations. When multiple policies are registered, this annotation ensures that the
 * annotated policy is executed after the specified policy in the processing chain.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * @AfterPolicy(RateLimitPolicy.class)
 * public class LoggingPolicy implements FlowControlPolicy {
 *   // This policy will execute after RateLimitPolicy
 *
 *   @Override
 *   public Flux<WebSocketMessage> apply(
 *       String path,
 *       WebSocketSessionContext context,
 *       Flux<WebSocketMessage> messages) {
 *     return messages.doOnNext(msg -> log.info("Message: {}", msg));
 *   }
 * }
 * }</pre>
 *
 * <p><b>Combining with {@link BeforePolicy}:</b>
 *
 * <pre>{@code
 * @AfterPolicy(PolicyA.class)
 * @BeforePolicy(PolicyC.class)
 * public class PolicyB implements FlowControlPolicy {
 *   // Execution order: PolicyA -> PolicyB -> PolicyC
 * }
 * }</pre>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 * @see BeforePolicy
 * @see FlowControlPolicy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterPolicy {
  /**
   * The flow control policy that must be executed before this policy.
   *
   * @return the policy class that this policy should execute after
   */
  Class<? extends FlowControlPolicy> value();
}
