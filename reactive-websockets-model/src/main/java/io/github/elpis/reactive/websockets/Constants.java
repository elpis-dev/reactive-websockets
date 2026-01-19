package io.github.elpis.reactive.websockets;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

public final class Constants {
  public static final int NO_SESSIONS = 0;
  public static final int HANDLER_ORDER = 10;

  public static final String DEFAULT_KEY = "__DEFAULT__";

  private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

  private Constants() {
    // private constructor
  }

  public static <T> T parseExpression(
      final String expressionValue,
      final Class<T> type,
      final UnaryOperator<SimpleEvaluationContext.Builder> builder,
      final Consumer<EvaluationContext> contextConsumer) {
    final Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(expressionValue);
    final SimpleEvaluationContext context =
        builder.apply(SimpleEvaluationContext.forReadWriteDataBinding()).build();
    contextConsumer.accept(context);
    return expression.getValue(context, type);
  }
}
