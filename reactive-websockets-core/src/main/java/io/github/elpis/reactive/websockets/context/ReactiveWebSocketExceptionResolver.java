package io.github.elpis.reactive.websockets.context;

import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.WebSocketAdvice;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Runtime exception resolver for WebSocket handlers. Discovers and invokes @ExceptionHandler
 * methods dynamically - no code generation needed. Similar to Spring's
 * ExceptionHandlerExceptionResolver.
 */
public final class ReactiveWebSocketExceptionResolver
    extends AbstractReactiveWebSocketExceptionResolver {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveWebSocketExceptionResolver.class);

  private final Map<Object, Map<Class<? extends Throwable>, HandlerMethodInfo>>
      localHandlersByBean = new ConcurrentHashMap<>();
  private final Map<Class<? extends Throwable>, HandlerMethodInfo> globalHandlers =
      new LinkedHashMap<>();

  public ReactiveWebSocketExceptionResolver(final ApplicationContext applicationContext) {
    super(applicationContext);
  }

  public void initialize() {
    this.scanLocalHandlers();
    this.scanGlobalHandlers();

    if (log.isInfoEnabled()) {
      int localHandlerCount = localHandlersByBean.values().stream().mapToInt(Map::size).sum();
      log.info(
          "Registered {} local exception handlers across {} beans and {} global exception handlers",
          localHandlerCount,
          localHandlersByBean.size(),
          globalHandlers.size());
    }
  }

  /**
   * Handle an exception by finding and invoking the appropriate handler. This is the runtime method
   * - called for each exception.
   *
   * <p>CRITICAL: You must pass the source bean that threw the exception!
   *
   * @param exception The exception to handle
   * @param sourceBean The bean instance where the exception originated (can be null for
   *     global-only)
   * @return Mono<Object> - the response from the handler, or empty if void/not found
   */
  public Publisher<?> handleException(final Throwable exception, final Object sourceBean) {
    final HandlerMethodInfo bestMatchingHandler =
        this.resolveHandler(exception.getClass(), sourceBean);

    if (bestMatchingHandler == null) {
      if (log.isDebugEnabled()) {
        log.debug(
            "No exception handler found for: {} from bean: {}",
            exception.getClass().getName(),
            sourceBean != null ? sourceBean.getClass().getSimpleName() : "unknown");
      }
      return Mono.empty();
    }

    if (log.isTraceEnabled()) {
      log.trace(
          "Handling exception {} with handler method: {} from bean: {}",
          exception.getClass().getSimpleName(),
          bestMatchingHandler.method.getName(),
          bestMatchingHandler.bean.getClass().getSimpleName());
    }

    return this.invokeHandlerMethod(bestMatchingHandler, exception);
  }

  /** Check if this resolver has any handlers registered */
  public boolean hasHandlers() {
    return !localHandlersByBean.isEmpty() || !globalHandlers.isEmpty();
  }

  /** Get all registered exception types for debugging */
  public Set<Class<? extends Throwable>> getAllHandledExceptionTypes() {
    final Set<Class<? extends Throwable>> types = new LinkedHashSet<>();
    localHandlersByBean.values().forEach(handlers -> types.addAll(handlers.keySet()));
    types.addAll(globalHandlers.keySet());

    return Collections.unmodifiableSet(types);
  }

  /** Scan for @ExceptionHandler methods in all @MessageEndpoint beans */
  private void scanLocalHandlers() {
    this.scanHandlers(MessageEndpoint.class, this::processLocalHandlerBean);
  }

  /** Scan for @ExceptionHandler methods in all @WebSocketAdvice beans */
  private void scanGlobalHandlers() {
    this.scanHandlers(WebSocketAdvice.class, this::processGlobalHandlerBean);
  }

  /** Process a single bean to find @ExceptionHandler methods in @MessageEndpoint beans */
  private void processLocalHandlerBean(final String beanName, final Class<?> targetType) {
    if (!this.nonAnnotatedClasses.contains(targetType)) {
      final Map<Method, ExceptionHandler> annotatedMethods =
          this.resolveAnnotatedMethods(targetType, ExceptionHandler.class, beanName);

      if (CollectionUtils.isEmpty(annotatedMethods)) {
        this.nonAnnotatedClasses.add(targetType);
        if (log.isTraceEnabled()) {
          log.trace(
              "No @ExceptionHandler annotations found on bean class: {}", targetType.getName());
        }
      } else {
        final Map<Class<? extends Throwable>, HandlerMethodInfo> beanHandlers =
            new LinkedHashMap<>();
        final Object adviceBean = applicationContext.getBean(beanName);

        for (Map.Entry<Method, ExceptionHandler> entry : annotatedMethods.entrySet()) {
          final Method method = entry.getKey();
          final ExceptionHandler annotation = entry.getValue();
          final Method methodToUse =
              AopUtils.selectInvocableMethod(method, applicationContext.getType(beanName));

          if (annotation != null) {
            final Class<? extends Throwable>[] exceptionTypes =
                extractExceptionTypes(methodToUse, annotation);
            final HandlerMethodInfo handlerInfo = new HandlerMethodInfo(methodToUse, adviceBean);

            for (Class<? extends Throwable> exceptionType : exceptionTypes) {
              final HandlerMethodInfo existing = beanHandlers.put(exceptionType, handlerInfo);
              Assert.state(
                  existing == null || existing.method.equals(methodToUse),
                  () ->
                      "Ambiguous @ExceptionHandler method mapped for ["
                          + exceptionType
                          + "] in bean '"
                          + beanName
                          + "': {"
                          + existing.method
                          + ", "
                          + methodToUse
                          + "}");
            }

            if (log.isTraceEnabled()) {
              log.trace(
                  "Registered local @ExceptionHandler method '{}' for bean '{}' for exceptions: {}",
                  methodToUse.getName(),
                  beanName,
                  Arrays.toString(exceptionTypes));
            }
          }
        }

        localHandlersByBean.put(adviceBean, beanHandlers);
      }
    }
  }

  /** Process a single @WebSocketAdvice bean to find @ExceptionHandler methods */
  private void processGlobalHandlerBean(final String beanName, final Class<?> targetType) {
    final Map<Method, ExceptionHandler> annotatedMethods =
        this.resolveAnnotatedMethods(targetType, ExceptionHandler.class, beanName);

    if (!CollectionUtils.isEmpty(annotatedMethods)) {
      final Object adviceBean = applicationContext.getBean(beanName);

      for (Map.Entry<Method, ExceptionHandler> entry : annotatedMethods.entrySet()) {
        final Method method = entry.getKey();
        final ExceptionHandler annotation = entry.getValue();
        final Method methodToUse =
            AopUtils.selectInvocableMethod(method, applicationContext.getType(beanName));

        if (annotation != null) {
          final Class<? extends Throwable>[] exceptionTypes =
              this.extractExceptionTypes(methodToUse, annotation);
          final HandlerMethodInfo handlerInfo = new HandlerMethodInfo(methodToUse, adviceBean);

          for (Class<? extends Throwable> exceptionType : exceptionTypes) {
            globalHandlers.putIfAbsent(exceptionType, handlerInfo);
          }

          if (log.isDebugEnabled()) {
            log.debug(
                "Registered global @ExceptionHandler method '{}' from advice '{}' for exceptions: {}",
                methodToUse.getName(),
                beanName,
                Arrays.toString(exceptionTypes));
          }
        }
      }
    }
  }

  /**
   * Extract exception types from @ExceptionHandler annotation Same logic as Spring's
   * ExceptionHandlerMethodResolver
   */
  @SuppressWarnings("unchecked")
  private Class<? extends Throwable>[] extractExceptionTypes(
      final Method method, final ExceptionHandler annotation) {
    final Class<? extends Throwable>[] annotationValue = annotation.value();

    if (annotationValue.length > 0) {
      return annotationValue;
    }

    final List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>();
    for (Parameter param : method.getParameters()) {
      if (Throwable.class.isAssignableFrom(param.getType())) {
        exceptionTypes.add((Class<? extends Throwable>) param.getType());
      }
    }

    if (exceptionTypes.isEmpty()) {
      throw new IllegalStateException(
          "@ExceptionHandler method ["
              + method
              + "] must declare exception type "
              + "either in annotation value or as method parameter");
    }

    return exceptionTypes.toArray(new Class[0]);
  }

  /**
   * Resolve the best handler for an exception. Priority: 1. Local handlers from the source bean
   * (highest priority) 2. Global handlers (lower priority) Within each scope: Most specific
   * exception match wins
   *
   * @param exceptionType The exception class
   * @param sourceBean The bean where the exception originated (can be null)
   */
  private HandlerMethodInfo resolveHandler(
      final Class<? extends Throwable> exceptionType, final Object sourceBean) {
    if (sourceBean != null) {
      final Map<Class<? extends Throwable>, HandlerMethodInfo> beanHandlers =
          localHandlersByBean.get(sourceBean);

      if (beanHandlers != null) {
        HandlerMethodInfo handler = beanHandlers.get(exceptionType);
        if (handler != null) {
          return handler;
        }

        handler = this.findBestMatch(exceptionType, beanHandlers);
        if (handler != null) {
          return handler;
        }
      }
    }

    return Optional.ofNullable(globalHandlers.get(exceptionType))
        .orElseGet(() -> this.findBestMatch(exceptionType, globalHandlers));
  }

  /**
   * Find best matching handler based on exception hierarchy. Prefers the most specific (closest in
   * hierarchy) exception type. Same algorithm as Spring's ExceptionDepthComparator
   */
  private HandlerMethodInfo findBestMatch(
      final Class<? extends Throwable> exceptionType,
      final Map<Class<? extends Throwable>, HandlerMethodInfo> handlers) {

    HandlerMethodInfo bestMatch = null;
    int bestDepth = Integer.MAX_VALUE;

    for (Map.Entry<Class<? extends Throwable>, HandlerMethodInfo> entry : handlers.entrySet()) {
      final Class<? extends Throwable> mappedType = entry.getKey();

      if (mappedType.isAssignableFrom(exceptionType)) {
        final int depth = getDepth(exceptionType, mappedType);

        if (depth < bestDepth) {
          bestDepth = depth;
          bestMatch = entry.getValue();
        }
      }
    }

    return bestMatch;
  }

  /** Calculate inheritance distance between exception and handler type */
  private int getDepth(Class<?> exceptionType, Class<?> handlerType) {
    int depth = 0;
    Class<?> current = exceptionType;

    while (current != null && current != handlerType) {
      depth++;
      current = current.getSuperclass();
    }

    return depth;
  }

  /**
   * Invoke the handler method using reflection. Handles both reactive (Mono/Flux) and non-reactive
   * return types.
   */
  private Publisher<?> invokeHandlerMethod(HandlerMethodInfo handler, Throwable exception) {
    try {
      ReflectionUtils.makeAccessible(handler.method);

      final Object[] args = this.prepareArguments(handler.method, exception);
      final Object result = handler.method.invoke(handler.bean, args);

      if (handler.isVoid) {
        return Mono.empty();
      } else if (result instanceof Mono) {
        return (Mono<?>) result;
      } else if (result instanceof Flux) {
        return (Flux<?>) result;
      } else {
        return Mono.justOrEmpty(result);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("Failed to invoke exception handler method: {}", handler.method, e);
      }
      return Mono.error(
          new IllegalStateException(
              "Failed to invoke exception handler method: " + handler.method, e));
    }
  }

  /** Prepare method arguments for invocation. Supports: Throwable, specific exception types */
  private Object[] prepareArguments(final Method method, final Throwable exception) {
    final Parameter[] parameters = method.getParameters();
    final Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      final Class<?> paramType = parameters[i].getType();
      if (Throwable.class.isAssignableFrom(paramType)) {
        args[i] = exception;
      }
      // TODO: Extend this to support other parameter types like WebSocketSession, etc.
    }

    return args;
  }

  /** Holds information about an exception handler method */
  private static class HandlerMethodInfo {
    final Method method;
    final Object bean;
    final boolean isVoid;

    HandlerMethodInfo(final Method method, final Object bean) {
      this.method = method;
      this.bean = bean;
      this.isVoid = method.getReturnType().equals(Void.TYPE);
    }
  }
}
