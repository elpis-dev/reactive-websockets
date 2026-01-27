package io.github.elpis.reactive.websockets.context;

import io.github.elpis.reactive.websockets.context.resolver.ReactiveWebSocketMethodParameterResolver;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Processes {@link MessageEndpoint} annotated beans and registers their {@link OnMessage} methods.
 * Similar to Spring's {@link org.springframework.context.event.EventListenerMethodProcessor}.
 *
 * <p>This processor:
 *
 * <ul>
 *   <li>Scans all beans for @MessageEndpoint annotations
 *   <li>Discovers @OnMessage annotated methods
 *   <li>Properly handles Spring AOP proxies
 *   <li>Combines base path from @MessageEndpoint with method path from @OnMessage
 *   <li>Collects all annotations from methods for flow control configuration
 * </ul>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveWebsocketMessageEndpointResolver
    extends AbstractReactiveWebSocketExceptionResolver {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveWebsocketMessageEndpointResolver.class);

  private final Map<String, MessageHandlerMethod> handlerMethods = new ConcurrentHashMap<>(256);
  private final List<ReactiveWebSocketMethodParameterResolver> methodParameterResolvers;

  public ReactiveWebsocketMessageEndpointResolver(final ApplicationContext applicationContext) {
    super(applicationContext);
    this.methodParameterResolvers =
        beanFactory.getBeanProvider(ReactiveWebSocketMethodParameterResolver.class).stream()
            .toList();
  }

  public void initialize() {
    this.scanHandlers(MessageEndpoint.class, this::processBean);

    if (log.isTraceEnabled()) {
      log.trace("Registered {} WebSocket handler methods", handlerMethods.size());
    }
  }

  /**
   * Get all registered handler methods.
   *
   * @return unmodifiable map of path to handler method info
   */
  public Map<String, MessageHandlerMethod> getHandlerMethods() {
    return Collections.unmodifiableMap(handlerMethods);
  }

  /**
   * Get handler method for a specific path.
   *
   * @param path the WebSocket path
   * @return the handler method info, or null if not found
   */
  @Nullable public MessageHandlerMethod getHandlerMethod(final String path) {
    return handlerMethods.get(path);
  }

  /**
   * Get all annotations for a specific path.
   *
   * @param path the WebSocket path
   * @return list of annotations, or empty list if path not found
   */
  public List<Annotation> getAnnotations(final String path) {
    MessageHandlerMethod handlerMethod = handlerMethods.get(path);
    return handlerMethod != null
        ? Arrays.asList(handlerMethod.annotations())
        : Collections.emptyList();
  }

  /**
   * Get all annotations of a specific type for a path.
   *
   * @param path the WebSocket path
   * @param annotationType the annotation type to filter
   * @return list of matching annotations
   */
  public <A extends Annotation> List<A> getAnnotations(
      final String path, final Class<A> annotationType) {
    return getAnnotations(path).stream()
        .filter(annotationType::isInstance)
        .map(annotationType::cast)
        .toList();
  }

  /**
   * Get path-to-annotations map for compatibility with existing code.
   *
   * @return map of path to list of annotations
   */
  public Map<String, List<Annotation>> getPathAnnotations() {
    final Map<String, List<Annotation>> result = new LinkedHashMap<>();
    handlerMethods.forEach(
        (path, handlerMethod) -> result.put(path, Arrays.asList(handlerMethod.annotations())));
    return Collections.unmodifiableMap(result);
  }

  private void processBean(final String beanName, final Class<?> targetType) {
    if (!this.nonAnnotatedClasses.contains(targetType)) {
      final Map<Method, OnMessage> annotatedMethods =
          this.resolveAnnotatedMethods(targetType, OnMessage.class, beanName);

      if (CollectionUtils.isEmpty(annotatedMethods)) {
        this.nonAnnotatedClasses.add(targetType);
        if (log.isTraceEnabled()) {
          log.trace("No @OnMessage annotations found on bean class: {}", targetType.getName());
        }
      } else {
        final MessageEndpoint typeAnnotation =
            AnnotatedElementUtils.findMergedAnnotation(targetType, MessageEndpoint.class);

        final String basePath = (typeAnnotation != null) ? typeAnnotation.value() : "";

        for (Map.Entry<Method, OnMessage> entry : annotatedMethods.entrySet()) {
          final Method method = entry.getKey();
          final OnMessage onMessage = entry.getValue();
          final Method methodToUse =
              AopUtils.selectInvocableMethod(method, applicationContext.getType(beanName));

          final String fullPath = this.concatenatePaths(basePath, onMessage.value());

          final Set<Annotation> annotations = new LinkedHashSet<>();
          if (typeAnnotation != null) {
            annotations.addAll(Arrays.asList(targetType.getAnnotations()));
          }
          annotations.addAll(Arrays.asList(methodToUse.getAnnotations()));

          final Map<MethodParameter, ReactiveWebSocketMethodParameterResolver> resolverCache =
              this.scanAndCacheResolvers(method);
          final MessageHandlerMethod handlerMethod =
              new MessageHandlerMethod(
                  beanName,
                  targetType,
                  methodToUse,
                  annotations.toArray(new Annotation[0]),
                  resolverCache);

          final MessageHandlerMethod existing = handlerMethods.putIfAbsent(fullPath, handlerMethod);
          Assert.state(
              existing == null,
              () ->
                  "Ambiguous mapping detected. Cannot map '%s' method %s to %s: There is already '%s' bean method %s mapped."
                      .formatted(
                          beanName, methodToUse, fullPath, existing.beanName(), existing.method()));
        }

        if (log.isTraceEnabled()) {
          log.trace(
              "{} @OnMessage methods processed on bean '{}': {}",
              annotatedMethods.size(),
              beanName,
              annotatedMethods);
        }
      }
    }
  }

  private Map<MethodParameter, ReactiveWebSocketMethodParameterResolver> scanAndCacheResolvers(
      final Method method) {

    final Map<MethodParameter, ReactiveWebSocketMethodParameterResolver> result =
        new LinkedHashMap<>();
    final int parameterCount = method.getParameterCount();
    for (int i = 0; i < parameterCount; i++) {
      final MethodParameter parameter = new MethodParameter(method, i);
      final ReactiveWebSocketMethodParameterResolver resolver =
          this.findUniqueSupportingResolver(parameter);

      if (resolver != null) {
        resolver.preRegister(parameter);
        result.put(parameter, resolver);
      }
    }

    return result;
  }

  private ReactiveWebSocketMethodParameterResolver findUniqueSupportingResolver(
      final MethodParameter parameter) {

    ReactiveWebSocketMethodParameterResolver found = null;
    for (ReactiveWebSocketMethodParameterResolver candidate : methodParameterResolvers) {
      if (!candidate.supports(parameter)) {
        continue;
      }

      if (found != null) {
        throw new IllegalStateException(
            "Multiple ReactiveWebSocketMethodParameterResolvers support parameter [%d] of type [%s]: %s and %s"
                .formatted(
                    parameter.getParameterIndex(),
                    parameter.getParameterType().getName(),
                    found.getClass().getName(),
                    candidate.getClass().getName()));
      }

      found = candidate;
    }

    return found;
  }

  private String concatenatePaths(String basePath, String methodPath) {
    if (basePath == null || basePath.isEmpty()) {
      return ensureStartsWithSlash(methodPath);
    }
    if (methodPath == null || methodPath.isEmpty()) {
      return ensureStartsWithSlash(basePath);
    }

    String normalizedBase =
        basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
    String normalizedMethod = methodPath.startsWith("/") ? methodPath : "/" + methodPath;

    return ensureStartsWithSlash(normalizedBase + normalizedMethod);
  }

  private String ensureStartsWithSlash(String path) {
    if (path == null || path.isEmpty()) {
      return "/";
    }
    return path.startsWith("/") ? path : "/" + path;
  }

  public record MessageHandlerMethod(
      String beanName,
      Class<?> beanType,
      Method method,
      Annotation[] annotations,
      Map<MethodParameter, ReactiveWebSocketMethodParameterResolver> parameterResolvers) {

    /**
     * Find annotation of specific type.
     *
     * @param annotationType the annotation class
     * @return the annotation if present, null otherwise
     */
    @Nullable public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
      return Arrays.stream(annotations)
          .filter(annotationType::isInstance)
          .map(annotationType::cast)
          .findFirst()
          .orElse(null);
    }

    /**
     * Check if annotation is present.
     *
     * @param annotationType the annotation class
     * @return true if present
     */
    public boolean hasAnnotation(final Class<? extends Annotation> annotationType) {
      return Arrays.stream(annotations).anyMatch(annotationType::isInstance);
    }

    /**
     * Get all annotations of specific type.
     *
     * @param annotationType the annotation class
     * @return list of matching annotations
     */
    public <A extends Annotation> List<A> getAnnotations(final Class<A> annotationType) {
      return Arrays.stream(annotations)
          .filter(annotationType::isInstance)
          .map(annotationType::cast)
          .toList();
    }
  }
}
