package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.context.ReactiveWebSocketExceptionResolver;
import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.context.resolver.ReactiveWebSocketMethodParameterResolver;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.exception.WebSocketProcessingException;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.util.TypeUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public final class ReflectiveReactiveWebSocketHandler extends AdaptiveReactiveWebSocketHandler {
  private static final Logger log =
      LoggerFactory.getLogger(ReflectiveReactiveWebSocketHandler.class);

  private final ReactiveWebSocketExceptionResolver exceptionResolver;
  private final ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod handlerMethod;
  private final Object handlerBean;

  private final ParameterNameDiscoverer parameterNameDiscoverer =
      new DefaultParameterNameDiscoverer();

  /**
   * Creates an ReflectiveReactiveWebSocketHandler with flow control configuration.
   *
   * <p>All flow control handlers, heartbeat and session handling are derived from
   * AdaptiveReactiveWebSocketHandler.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   * @param pathTemplate the WebSocket path template (e.g., "/chat/{room}")
   * @param reactiveHeartbeatFlowControlRegistry heartbeat flow control registry
   * @param reactiveFlowControlChain flow control chain
   */
  ReflectiveReactiveWebSocketHandler(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final String pathTemplate,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain,
      final ReactiveWebSocketExceptionResolver exceptionResolver,
      final ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod handlerMethod,
      final Object handlerBean) {
    super(
        eventManagerFactory,
        sessionRegistry,
        jsonMapper,
        pathTemplate,
        reactiveHeartbeatFlowControlRegistry,
        reactiveFlowControlChain);

    this.handlerMethod = handlerMethod;
    this.handlerBean = handlerBean;
    this.exceptionResolver = exceptionResolver;

    ReflectionUtils.makeAccessible(handlerMethod.method());
  }

  @Override
  protected Publisher<?> processMessages(
      final WebSocketSessionContext context,
      final Flux<WebSocketMessage> incomingStream,
      final Sinks.Many<Object> outboundSink) {

    return this.invokeHandler(context, outboundSink);
  }

  private Publisher<?> invokeHandler(
      final WebSocketSessionContext context, final Sinks.Many<Object> outboundSink) {

    try {
      final Object[] args = this.assignMethodArguments(context);
      final Object result = handlerMethod.method().invoke(handlerBean, args);
      return this.handleMethodResult(result, outboundSink, handlerBean);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Failed to invoke handler method: {}", handlerMethod.method(), e);
      }
      return Mono.error(new WebSocketProcessingException(e.getMessage(), e));
    }
  }

  private Object[] assignMethodArguments(final WebSocketSessionContext context) {
    final Method method = handlerMethod.method();
    final Parameter[] parameters = method.getParameters();
    final Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      final MethodParameter methodParameter = new MethodParameter(method, i);
      methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
      final ReactiveWebSocketMethodParameterResolver resolver =
          handlerMethod.parameterResolvers().get(methodParameter);

      args[i] =
          Optional.ofNullable(resolver)
              .map(r -> r.resolve(methodParameter, context))
              .orElseGet(
                  () -> TypeUtils.getDefaultValueForType(methodParameter.getParameterType()));
    }

    return args;
  }

  private Publisher<?> handleMethodResult(
      final Object result, final Sinks.Many<Object> outboundSink, final Object handlerBean) {

    if (result == null) {
      return Mono.empty();
    }

    if (result instanceof Mono<?> mono) {
      return mono.doOnNext(outboundSink::tryEmitNext)
          .onErrorResume(
              throwable ->
                  Flux.from(this.exceptionResolver.handleException(throwable, handlerBean))
                      .doOnNext(
                          handlerResult ->
                              outboundSink.tryEmitError(
                                  new ErrorResponseException(handlerResult, throwable)))
                      .then(Mono.never()));
    } else if (result instanceof Flux<?> flux) {
      return flux.doOnNext(outboundSink::tryEmitNext)
          .onErrorResume(
              throwable ->
                  Flux.from(this.exceptionResolver.handleException(throwable, handlerBean))
                      .doOnNext(
                          handlerResult ->
                              outboundSink.tryEmitError(
                                  new ErrorResponseException(handlerResult, throwable)))
                      .flatMap(ignored -> Flux.never()))
          .then();
    } else if (result instanceof Publisher<?> publisher) {
      return Flux.from(publisher)
          .doOnNext(outboundSink::tryEmitNext)
          .onErrorResume(
              throwable ->
                  Flux.from(this.exceptionResolver.handleException(throwable, handlerBean))
                      .doOnNext(
                          handlerResult ->
                              outboundSink.tryEmitError(
                                  new ErrorResponseException(handlerResult, throwable)))
                      .flatMap(ignored -> Flux.never()))
          .then();
    } else {
      outboundSink.tryEmitNext(result);
      return Mono.empty();
    }
  }
}
