package io.github.elpis.reactive.websockets.handler.flowcontrol;

import io.github.elpis.reactive.websockets.flowcontrol.AfterFlow;
import io.github.elpis.reactive.websockets.flowcontrol.BeforeFlow;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  public List<FlowControlPolicy> getInputPolicies() {
    return inputPolicies;
  }

  public List<FlowControlPolicy> getOutputPolicies() {
    return outputPolicies;
  }

  public boolean hasPolicies() {
    return !inputPolicies.isEmpty() || !outputPolicies.isEmpty();
  }

  public static ReactiveFlowControlChain empty() {
    return new ReactiveFlowControlChain();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(final Collection<FlowControlPolicy> initialPolicies) {
    Builder builder = new Builder();
    builder.addPolicies(initialPolicies);
    return builder;
  }

  public static class Builder {
    private final List<PolicyEntry> policies = new ArrayList<>();

    public Builder addPolicy(final FlowControlPolicy policy) {
      policies.add(new PolicyEntry(policy, null, null));
      return this;
    }

    public Builder addPolicyBefore(
        final FlowControlPolicy policy, Class<? extends FlowControlPolicy> beforeClass) {
      policies.add(new PolicyEntry(policy, beforeClass, null));
      return this;
    }

    public Builder addPolicyAfter(
        final FlowControlPolicy policy, Class<? extends FlowControlPolicy> afterClass) {
      policies.add(new PolicyEntry(policy, null, afterClass));
      return this;
    }

    public Builder addPolicies(final Collection<FlowControlPolicy> policies) {
      policies.forEach(this::addPolicy);
      return this;
    }

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
      final BeforeFlow beforeA = a.policy.getClass().getAnnotation(BeforeFlow.class);
      final AfterFlow afterA = a.policy.getClass().getAnnotation(AfterFlow.class);

      final BeforeFlow beforeB = b.policy.getClass().getAnnotation(BeforeFlow.class);
      final AfterFlow afterB = b.policy.getClass().getAnnotation(AfterFlow.class);

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
      return entries.stream()
          .sorted(
              (a, b) ->
                  this.compareBuilderPolicies(a, b)
                      .or(() -> this.compareAnnotationPolicies(a, b))
                      .orElse(0))
          .map(e -> e.policy)
          .toList();
    }

    private record PolicyEntry(
        FlowControlPolicy policy,
        Class<? extends FlowControlPolicy> beforeClass,
        Class<? extends FlowControlPolicy> afterClass) {}
  }
}
