package org.elpis.reactive.websockets.event.impl;

import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.event.EventSelectorMatcher;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class ClosedSessionEventSelectorMatcher implements EventSelectorMatcher<ClientSessionClosedEvent> {
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Boolean select(final ClientSessionClosedEvent event, final EventSelector annotation) {
        final ClientSessionCloseInfo clientSessionCloseInfo = event.payload();
        final Expression expression = expressionParser.parseExpression(annotation.value());
        final EvaluationContext context = new StandardEvaluationContext(ClientSessionCloseInfo.builder()
                .sessionInfo(clientSessionCloseInfo.getSessionInfo())
                .build());

        return expression.getValue(context, Boolean.class);
    }
}
