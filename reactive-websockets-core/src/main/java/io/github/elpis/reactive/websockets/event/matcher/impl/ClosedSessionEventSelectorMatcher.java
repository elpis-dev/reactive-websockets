package io.github.elpis.reactive.websockets.event.matcher.impl;

import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.matcher.EventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * Implementation of {@link EventSelectorMatcher} for {@link ClientSessionClosedEvent} events.
 *
 * @author Phillip J. Fry
 * @see EventSelectorMatcher
 * @see ClientSessionClosedEvent
 * @since 1.0.0
 */
public class ClosedSessionEventSelectorMatcher
    implements EventSelectorMatcher<ClientSessionClosedEvent> {
  private final ExpressionParser expressionParser = new SpelExpressionParser();

  /**
   * Matches {@link ClientSessionClosedEvent} against {@link EventSelector} using SpEL expression
   * defined in the annotation.
   *
   * @param event ClientSessionClosedEvent event
   * @param annotation EventSelector annotation from annotated method
   * @return boolean signaling about match success/failure
   * @since 1.0.0
   */
  @Override
  public boolean matches(final ClientSessionClosedEvent event, final EventSelector annotation) {
    final SessionCloseInfo sessionCloseInfo = event.payload();
    final Expression expression = expressionParser.parseExpression(annotation.value());
    final SimpleEvaluationContext context =
        SimpleEvaluationContext.forReadWriteDataBinding()
            .withAssignmentDisabled()
            .withRootObject(
                SessionCloseInfo.builder().session(sessionCloseInfo.getSession()).build())
            .build();

    return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
  }
}
