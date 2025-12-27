package io.github.elpis.reactive.websockets.processor.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.github.elpis.reactive.websockets.processor.exception.ExceptionHandlerInfo;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Generates code for exception handling in WebSocket handlers.
 *
 * <p>This generator produces:
 *
 * <ul>
 *   <li>{@code .onErrorResume()} chains for Publisher-returning handlers
 *   <li>{@code .doOnError()} chains for void handlers
 *   <li>Direct method calls with zero reflection overhead
 * </ul>
 *
 * <p>Generated code flow for non-void handlers:
 *
 * <ol>
 *   <li>Catch exception in {@code .onErrorResume()}
 *   <li>Call handler method to get error response Publisher
 *   <li>Wrap response in {@code ErrorResponseException}
 *   <li>Emit via {@code outboundSink.tryEmitError()}
 *   <li>BaseWebSocketHandler catches, extracts payload, converts and sends
 * </ol>
 *
 * <p>Generated code example:
 *
 * <pre>{@code
 * .onErrorResume(ValidationException.class, ex -> {
 *     this.globalHandler.handleValidation(ex)
 *         .doOnNext(response -> streams.outboundSink().tryEmitError(
 *             new ErrorResponseException(response, ex)))
 *         .subscribe();
 *     return Flux.empty();
 * })
 * }</pre>
 *
 * @since 1.0.0
 */
public class ExceptionHandlerCodeGenerator {

  /**
   * Generates exception handler chains for a method. Combines local handlers
   * (from @MessageEndpoint) and global handlers (from @WebSocketAdvice).
   *
   * <p>Priority order:
   *
   * <ol>
   *   <li>Local handlers (higher priority)
   *   <li>Global handlers (lower priority)
   * </ol>
   *
   * <p>Returns the chain operators to append after the method call:
   *
   * <pre>{@code
   * .onErrorResume(Exception1.class, ex -> handler.method1(ex))
   * .onErrorResume(Exception2.class, ex -> handler.method2(ex))
   * }</pre>
   *
   * @param localHandlers handlers from the @MessageEndpoint class
   * @param globalHandlers map of @WebSocketAdvice classes to their handlers
   * @return code block with error handling chain operators (without leading method call)
   */
  public static CodeBlock generateErrorHandlerChain(
      List<ExceptionHandlerInfo> localHandlers,
      Map<TypeElement, List<ExceptionHandlerInfo>> globalHandlers) {

    if ((localHandlers == null || localHandlers.isEmpty())
        && (globalHandlers == null || globalHandlers.isEmpty())) {
      return CodeBlock.builder().build(); // Empty
    }

    CodeBlock.Builder chainBuilder = CodeBlock.builder();

    if (localHandlers != null) {
      for (ExceptionHandlerInfo handler : localHandlers) {
        for (TypeMirror exceptionType : handler.exceptionTypes()) {
          CodeBlock handlerCode = generateHandlerCode(handler, exceptionType);
          chainBuilder.add(handlerCode);
        }
      }
    }

    if (globalHandlers != null) {
      for (List<ExceptionHandlerInfo> handlers : globalHandlers.values()) {
        for (ExceptionHandlerInfo handler : handlers) {
          for (TypeMirror exceptionType : handler.exceptionTypes()) {
            CodeBlock handlerCode = generateHandlerCode(handler, exceptionType);
            chainBuilder.add(handlerCode);
          }
        }
      }
    }

    return chainBuilder.build();
  }

  /**
   * Generates error handler code for a single exception type.
   *
   * <p>This method generates different code based on whether the handler returns void:
   *
   * <ul>
   *   <li><b>Non-void handlers:</b> Use inline error handling that calls the handler, wraps
   *       response in ErrorResponseException, emits via tryEmitError, and returns Flux.never() to
   *       keep the stream alive.
   *   <li><b>Void handlers:</b> Use {@code .doOnError()} for side-effects only
   * </ul>
   *
   * @param handler the exception handler info
   * @param exceptionType the specific exception type to handle
   * @return code block with .onErrorResume() or .doOnError()
   */
  private static CodeBlock generateHandlerCode(
      ExceptionHandlerInfo handler, TypeMirror exceptionType) {
    String exceptionClassName = exceptionType.toString();
    String methodName = handler.getMethodName();
    String target = getHandlerTarget(handler);

    if (handler.returnsVoid()) {
      return CodeBlock.builder()
          .add(
              "    .doOnError($L.class,\n        ex -> $L.$L(ex))\n",
              exceptionClassName,
              target,
              methodName)
          .build();
    } else {
      // Inline error handling that keeps the stream alive
      ClassName flux = ClassName.get("reactor.core.publisher", "Flux");
      ClassName errorResponseException =
          ClassName.get(
              "io.github.elpis.reactive.websockets.handler.exception", "ErrorResponseException");

      return CodeBlock.builder()
          .add(
              """
                              .onErrorResume($L.class,
                                  ex -> $L.$L(ex)
                                      .doOnNext(response -> streams.outboundSink().tryEmitError(new $T(response, ex)))
                                      .flatMapMany(ignored -> $T.never()))
                          """,
              exceptionClassName,
              target,
              methodName,
              errorResponseException,
              flux)
          .build();
    }
  }

  /**
   * Gets the handler target (field name or "this.socketResource").
   *
   * @param handler the exception handler info
   * @return "this.globalHandler" for global handlers, "this.socketResource" for local
   */
  private static String getHandlerTarget(ExceptionHandlerInfo handler) {
    if (handler.isLocal()) {
      return "this.socketResource";
    } else {
      return "this." + handler.getBeanFieldName();
    }
  }
}
