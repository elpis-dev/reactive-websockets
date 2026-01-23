package io.github.elpis.reactive.websockets;

import io.github.elpis.reactive.websockets.web.annotation.Backpressure;
import io.github.elpis.reactive.websockets.web.annotation.RateLimit;
import java.util.concurrent.TimeUnit;
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

  // Backpressure defaults
  public static final Backpressure.BackpressureStrategy DEFAULT_BACKPRESSURE_STRATEGY =
      Backpressure.BackpressureStrategy.BUFFER;
  public static final int DEFAULT_BACKPRESSURE_BUFFER_CAPACITY = 256;

  // Heartbeat defaults
  public static final long DEFAULT_HEARTBEAT_INTERVAL = 30L;
  public static final long DEFAULT_HEARTBEAT_TIMEOUT = 60L;

  // Rate limit defaults
  public static final int DEFAULT_RATE_LIMIT_FOR_PERIOD = 10;
  public static final long DEFAULT_RATE_LIMIT_REFRESH_PERIOD = 1L;
  public static final TimeUnit DEFAULT_RATE_LIMIT_TIME_UNIT = TimeUnit.SECONDS;
  public static final long DEFAULT_RATE_LIMIT_TIMEOUT = 25L;
  public static final RateLimit.RateLimitScope DEFAULT_RATE_LIMIT_SCOPE =
      RateLimit.RateLimitScope.SESSION;

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
