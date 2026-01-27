package io.github.elpis.reactive.websockets.unit.flowcontrol;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.elpis.reactive.websockets.flowcontrol.AfterFlow;
import io.github.elpis.reactive.websockets.flowcontrol.BeforeFlow;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPlacement;
import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

class ReactiveFlowControlChainTest {

  @Test
  void testFlowControlChainAnnotationBasedApproach() {
    // given
    final FlowControlPolicy policyA = new PolicyA();
    final FlowControlPolicy policyX = new AnnotatedPolicyX();
    final FlowControlPolicy policyY = new AnnotatedPolicyY();

    // test
    final ReactiveFlowControlChain chain =
        ReactiveFlowControlChain.builder(List.of(policyA, policyX, policyY)).build();

    // assert
    assertThat(chain.hasPolicies()).isTrue();
    assertThat(chain.getOutputPolicies()).hasSize(3).containsExactly(policyA, policyY, policyX);
  }

  @Test
  void testFlowControlChainMixedApproach() {
    // given
    final FlowControlPolicy policyA = new PolicyA();
    final FlowControlPolicy policyB = new PolicyB();
    final FlowControlPolicy policyC = new PolicyC();
    final FlowControlPolicy policyX = new AnnotatedPolicyX();
    final FlowControlPolicy policyY = new AnnotatedPolicyY();

    // test
    final ReactiveFlowControlChain chain =
        ReactiveFlowControlChain.builder()
            .addPolicy(policyA)
            .addPolicies(List.of(policyX, policyY))
            .addPolicyAfter(policyB, PolicyA.class)
            .addPolicyBefore(policyC, PolicyA.class)
            .build();

    // assert
    assertThat(chain.hasPolicies()).isTrue();
    assertThat(chain.getOutputPolicies())
        .hasSize(5)
        .containsExactly(policyC, policyA, policyB, policyY, policyX);
  }

  @Test
  void testFlowControlChainBuilderApproach() {
    // given
    final FlowControlPolicy policyA = new PolicyA();
    final FlowControlPolicy policyB = new PolicyB();
    final FlowControlPolicy policyC = new PolicyC();

    // test
    final ReactiveFlowControlChain chain =
        ReactiveFlowControlChain.builder()
            .addPolicy(policyA)
            .addPolicyAfter(policyB, PolicyA.class)
            .addPolicyBefore(policyC, PolicyB.class)
            .build();

    // assert
    assertThat(chain.hasPolicies()).isTrue();
    assertThat(chain.getOutputPolicies()).hasSize(3).containsExactly(policyA, policyC, policyB);
  }

  @Test
  void testFlowControlChainEmptyChain() {
    // test
    final ReactiveFlowControlChain chain = ReactiveFlowControlChain.empty();

    // assert
    assertThat(chain.hasPolicies()).isFalse();
  }

  private static class PolicyA implements FlowControlPolicy {
    @Override
    public FlowControlPlacement placement() {
      return FlowControlPlacement.OUTPUT;
    }

    @Override
    public Flux<WebSocketMessage> apply(
        final String path,
        final WebSocketSessionContext context,
        final Flux<WebSocketMessage> webSocketMessageFlux) {
      return Flux.never();
    }
  }

  @AfterFlow(PolicyA.class)
  private static class AnnotatedPolicyX implements FlowControlPolicy {

    @Override
    public FlowControlPlacement placement() {
      return FlowControlPlacement.OUTPUT;
    }

    @Override
    public Flux<WebSocketMessage> apply(
        final String path,
        final WebSocketSessionContext context,
        final Flux<WebSocketMessage> webSocketMessageFlux) {
      return Flux.never();
    }
  }

  @AfterFlow(PolicyA.class)
  @BeforeFlow(AnnotatedPolicyX.class)
  private static class AnnotatedPolicyY implements FlowControlPolicy {

    @Override
    public FlowControlPlacement placement() {
      return FlowControlPlacement.OUTPUT;
    }

    @Override
    public Flux<WebSocketMessage> apply(
        final String path,
        final WebSocketSessionContext context,
        final Flux<WebSocketMessage> webSocketMessageFlux) {
      return Flux.never();
    }
  }

  private static class PolicyB implements FlowControlPolicy {
    @Override
    public FlowControlPlacement placement() {
      return FlowControlPlacement.OUTPUT;
    }

    @Override
    public Flux<WebSocketMessage> apply(
        final String path,
        final WebSocketSessionContext context,
        final Flux<WebSocketMessage> webSocketMessageFlux) {
      return Flux.never();
    }
  }

  private static class PolicyC implements FlowControlPolicy {
    @Override
    public FlowControlPlacement placement() {
      return FlowControlPlacement.OUTPUT;
    }

    @Override
    public Flux<WebSocketMessage> apply(
        final String path,
        final WebSocketSessionContext context,
        final Flux<WebSocketMessage> webSocketMessageFlux) {
      return Flux.never();
    }
  }
}
