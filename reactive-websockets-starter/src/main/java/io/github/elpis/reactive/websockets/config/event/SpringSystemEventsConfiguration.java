package io.github.elpis.reactive.websockets.config.event;

import io.github.elpis.reactive.websockets.flowcontrol.FlowControlPolicy;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.logging.ChainRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@ConditionalOnBean(ReactiveFlowControlChain.class)
public class SpringSystemEventsConfiguration {
  private static final Logger log = LoggerFactory.getLogger(SpringSystemEventsConfiguration.class);

  private final ReactiveFlowControlChain chain;

  public SpringSystemEventsConfiguration(final ReactiveFlowControlChain chain) {
    this.chain = chain;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void logFlowControlChain() {
    if (!log.isTraceEnabled() || !chain.hasPolicies()) {
      return;
    }

    ChainRenderer.<FlowControlPolicy>builder()
        .title("Flow Control Chain")
        .section("Input Policies", chain.getInputPolicies())
        .section("Output Policies", chain.getOutputPolicies())
        .render((rendered) -> log.trace("\n{}", rendered));
  }
}
