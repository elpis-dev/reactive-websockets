package io.github.elpis.reactive.websockets.flowcontrol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that this flow control policy should be applied before another policy.
 *
 * <p>This annotation is used to define ordering constraints between {@link FlowControlPolicy}
 * implementations. When multiple policies are registered, this annotation ensures that the
 * annotated policy is executed before the specified policy in the processing chain.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * @BeforeFlow(LoggingPolicy.class)
 * public class AuthenticationPolicy implements FlowControlPolicy {
 *   // This policy will execute before LoggingPolicy
 *
 *   @Override
 *   public Flux<WebSocketMessage> apply(
 *       String path,
 *       WebSocketSessionContext context,
 *       Flux<WebSocketMessage> messages) {
 *     return messages.filter(msg -> isAuthenticated(context));
 *   }
 * }
 * }</pre>
 *
 * <p><b>Combining with {@link AfterPolicy}:</b>
 *
 * <pre>{@code
 * @AfterFlow(PolicyA.class)
 * @BeforeFlow(PolicyC.class)
 * public class PolicyB implements FlowControlPolicy {
 *   // Execution order: PolicyA -> PolicyB -> PolicyC
 * }
 * }</pre>
 *
 * <p><b>Complex ordering example:</b>
 *
 * <pre>{@code
 * // PolicyX executes after PolicyA
 * @AfterFlow(PolicyA.class)
 * public class PolicyX implements FlowControlPolicy { }
 *
 * // PolicyY executes after PolicyA but before PolicyX
 * @AfterFlow(PolicyA.class)
 * @BeforeFlow(PolicyX.class)
 * public class PolicyY implements FlowControlPolicy { }
 *
 * // Resulting order: PolicyA -> PolicyY -> PolicyX
 * }</pre>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 * @see AfterPolicy
 * @see FlowControlPolicy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforePolicy {
  /**
   * The flow control policy that must be executed after this policy.
   *
   * @return the policy class that this policy should execute before
   */
  Class<? extends FlowControlPolicy> value();
}
