package org.elpis.reactive.websockets.event.matcher.impl;

import org.elpis.reactive.websockets.config.SessionCloseInfo;
import org.elpis.reactive.websockets.event.matcher.EventSelectorMatcher;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ClosedSessionEventSelectorMatcher implements EventSelectorMatcher<ClientSessionClosedEvent> {
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Boolean process(final ClientSessionClosedEvent event, final EventSelector annotation) {
        final SessionCloseInfo sessionCloseInfo = event.payload();
        final Expression expression = expressionParser.parseExpression(annotation.value());
        final EvaluationContext context = new StandardEvaluationContext(SessionCloseInfo.builder()
                .session(sessionCloseInfo.getSession())
                .build());

        return expression.getValue(context, Boolean.class);
    }
}
