package io.github.elpis.reactive.websockets.handler.flowcontrol;

import io.github.elpis.reactive.websockets.flowcontrol.AfterPolicy;
import io.github.elpis.reactive.websockets.flowcontrol.BeforePolicy;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.Assert;

/**
 * Represents a chain of reactive flow control policies for WebSocket message processing.
 *
 * <p>This class maintains separate lists of input and output flow control policies. It provides
 * functionality to build a chain of policies with specified ordering constraints, either through
 * builder methods or annotations on the policy classes.
 *
 * <p>The policies can be applied in sequence to incoming and outgoing WebSocket message streams to
 * enforce various flow control strategies.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveFlowControlChain {
  private final List<FlowControlPolicy> inputPolicies;
  private final List<FlowControlPolicy> outputPolicies;

  private ReactiveFlowControlChain() {
    this.inputPolicies = List.of();
    this.outputPolicies = List.of();
  }

  private ReactiveFlowControlChain(
      final List<FlowControlPolicy> inputPolicies, final List<FlowControlPolicy> outputPolicies) {
    this.inputPolicies = inputPolicies;
    this.outputPolicies = outputPolicies;
  }

  /**
   * Gets the list of input flow control policies.
   *
   * @return the list of input flow control policies
   */
  public List<FlowControlPolicy> getInputPolicies() {
    return inputPolicies;
  }

  /**
   * Gets the list of output flow control policies.
   *
   * @return the list of output flow control policies
   */
  public List<FlowControlPolicy> getOutputPolicies() {
    return outputPolicies;
  }

  /**
   * Checks if there are any flow control policies in the chain.
   *
   * @return true if there are input or output policies, false otherwise
   */
  public boolean hasPolicies() {
    return !inputPolicies.isEmpty() || !outputPolicies.isEmpty();
  }

  /**
   * Creates an empty ReactiveFlowControlChain with no policies.
   *
   * @return an empty ReactiveFlowControlChain
   */
  public static ReactiveFlowControlChain empty() {
    return new ReactiveFlowControlChain();
  }

  /**
   * Creates a builder for constructing a ReactiveFlowControlChain.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder initialized with the given collection of flow control policies.
   *
   * @param initialPolicies the initial collection of flow control policies
   * @return a Builder instance with the initial policies added
   */
  public static Builder builder(final Collection<FlowControlPolicy> initialPolicies) {
    return builder().addPolicies(initialPolicies);
  }

  /**
   * Builder class for constructing a ReactiveFlowControlChain with specified policies and ordering
   * constraints.
   *
   * @author Phillip J. Fry
   * @since 1.0.0
   */
  public static class Builder {
    private final List<PolicyEntry> policies = new ArrayList<>();

    /**
     * Adds a flow control policy to the chain.
     *
     * @param policy the flow control policy to add
     * @return the Builder instance for chaining
     */
    public Builder addPolicy(final FlowControlPolicy policy) {
      policies.add(new PolicyEntry(policy, null, null));
      return this;
    }

    /**
     * Adds a flow control policy to be placed before another specified policy class.
     *
     * @param policy the flow control policy to add
     * @param beforeClass the class of the policy before which to place the new policy
     * @return the Builder instance for chaining
     */
    public Builder addPolicyBefore(
        final FlowControlPolicy policy, Class<? extends FlowControlPolicy> beforeClass) {
      policies.add(new PolicyEntry(policy, beforeClass, null));
      return this;
    }

    /**
     * Adds a flow control policy to be placed after another specified policy class.
     *
     * @param policy the flow control policy to add
     * @param afterClass the class of the policy after which to place the new policy
     * @return the Builder instance for chaining
     */
    public Builder addPolicyAfter(
        final FlowControlPolicy policy, Class<? extends FlowControlPolicy> afterClass) {
      policies.add(new PolicyEntry(policy, null, afterClass));
      return this;
    }

    /**
     * Adds a collection of flow control policies to the chain.
     *
     * @param policies the collection of flow control policies to add
     * @return the Builder instance for chaining
     */
    public Builder addPolicies(final Collection<FlowControlPolicy> policies) {
      policies.forEach(this::addPolicy);
      return this;
    }

    /**
     * Builds the ReactiveFlowControlChain with the added policies and specified ordering.
     *
     * @return the constructed ReactiveFlowControlChain
     */
    public ReactiveFlowControlChain build() {
      final List<FlowControlPolicy> sorted = sortPolicies(policies);

      final List<FlowControlPolicy> inputPolicies =
          sorted.stream()
              .filter(p -> p.placement() == FlowControlPlacement.INPUT)
              .collect(Collectors.toList());

      final List<FlowControlPolicy> outputPolicies =
          sorted.stream()
              .filter(p -> p.placement() == FlowControlPlacement.OUTPUT)
              .collect(Collectors.toList());

      return new ReactiveFlowControlChain(inputPolicies, outputPolicies);
    }

    private Optional<Integer> compareBuilderPolicies(final PolicyEntry a, final PolicyEntry b) {
      // First check explicit before/after from builder
      if (a.afterClass != null && a.afterClass.equals(b.policy.getClass())) {
        return Optional.of(1); // a after b
      }
      if (a.beforeClass != null && a.beforeClass.equals(b.policy.getClass())) {
        return Optional.of(-1); // a before b
      }
      if (b.afterClass != null && b.afterClass.equals(a.policy.getClass())) {
        return Optional.of(-1); // b after a
      }
      if (b.beforeClass != null && b.beforeClass.equals(a.policy.getClass())) {
        return Optional.of(1); // b before a
      }

      return Optional.empty();
    }

    private Optional<Integer> compareAnnotationPolicies(final PolicyEntry a, final PolicyEntry b) {
      final BeforePolicy beforeA = a.policy.getClass().getAnnotation(BeforePolicy.class);
      final AfterPolicy afterA = a.policy.getClass().getAnnotation(AfterPolicy.class);

      final BeforePolicy beforeB = b.policy.getClass().getAnnotation(BeforePolicy.class);
      final AfterPolicy afterB = b.policy.getClass().getAnnotation(AfterPolicy.class);

      if (beforeA != null && beforeA.value().equals(b.policy.getClass())) {
        return Optional.of(-1);
      }
      if (afterA != null && afterA.value().equals(b.policy.getClass())) {
        return Optional.of(1);
      }
      if (beforeB != null && beforeB.value().equals(a.policy.getClass())) {
        return Optional.of(1);
      }
      if (afterB != null && afterB.value().equals(a.policy.getClass())) {
        return Optional.of(-1);
      }

      return Optional.empty();
    }

    /**
     * Sorts policies based on builder-defined and annotation-based ordering. Note: Ordering
     * constraints between policies with different placements (INPUT vs OUTPUT) are ignored since
     * they operate on separate streams.
     */
    private List<FlowControlPolicy> sortPolicies(final List<PolicyEntry> entries) {
      final Map<FlowControlPolicy, Set<FlowControlPolicy>> graph = new HashMap<>();
      final Map<FlowControlPolicy, Integer> inDegree = new HashMap<>();

      for (PolicyEntry entry : entries) {
        graph.put(entry.policy, new HashSet<>());
        inDegree.put(entry.policy, 0);
      }

      for (int i = 0; i < entries.size(); i++) {
        for (int j = 0; j < entries.size(); j++) {
          if (i == j) continue;

          final PolicyEntry a = entries.get(i);
          final PolicyEntry b = entries.get(j);

          final Optional<Integer> comparison =
              compareBuilderPolicies(a, b).or(() -> compareAnnotationPolicies(a, b));

          if (comparison.isPresent()) {
            int result = comparison.get();
            if (result < 0) {
              if (graph.get(a.policy).add(b.policy)) {
                inDegree.merge(b.policy, 1, Integer::sum);
              }
            }
          }
        }
      }

      final Queue<FlowControlPolicy> queue = new ArrayDeque<>();
      for (PolicyEntry entry : entries) {
        if (inDegree.get(entry.policy) == 0) {
          queue.offer(entry.policy);
        }
      }

      final List<FlowControlPolicy> result = new ArrayList<>();
      while (!queue.isEmpty()) {
        final FlowControlPolicy current = queue.poll();
        result.add(current);

        for (FlowControlPolicy neighbor : graph.get(current)) {
          int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
          if (newDegree == 0) {
            queue.offer(neighbor);
          }
        }
      }

      Assert.state(
          result.size() == entries.size(),
          "Circular dependency detected in policy ordering constraints");

      return result;
    }

    private record PolicyEntry(
        FlowControlPolicy policy,
        Class<? extends FlowControlPolicy> beforeClass,
        Class<? extends FlowControlPolicy> afterClass) {}
  }
}
