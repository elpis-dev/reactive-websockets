package io.github.elpis.reactive.websockets.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for resolving WebSocket-related annotations and handlers within a Spring
 * context. This class provides common functionality for scanning beans, resolving annotated
 * methods, and filtering out Spring container classes. It's not intended to be instantiated and
 * configured directly but serves as a foundation for specific resolvers.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 * @see ReactiveWebSocketClosedEventHandlersResolver
 * @see ReactiveWebSocketExceptionResolver
 * @see ReactiveWebsocketMessageEndpointResolver
 */
public abstract sealed class AbstractReactiveWebSocketExceptionResolver
    permits ReactiveWebSocketClosedEventHandlersResolver,
        ReactiveWebSocketExceptionResolver,
        ReactiveWebsocketMessageEndpointResolver {
  private static final Logger log =
      LoggerFactory.getLogger(AbstractReactiveWebSocketExceptionResolver.class);

  final Set<Class<?>> nonAnnotatedClasses = ConcurrentHashMap.newKeySet(64);

  final ConfigurableApplicationContext applicationContext;
  final ConfigurableListableBeanFactory beanFactory;

  public AbstractReactiveWebSocketExceptionResolver(final ApplicationContext applicationContext) {
    Assert.isTrue(
        applicationContext instanceof ConfigurableApplicationContext,
        "ApplicationContext does not implement ConfigurableApplicationContext");
    this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    this.beanFactory = this.applicationContext.getBeanFactory();
  }

  /**
   * Initializes the resolver. Subclasses may override this method to perform custom initialization
   * logic. By default, this method is a no-op. This method is typically called after the resolver
   * has been constructed and the application context is set.
   */
  void initialize() {
    // No-op by default
  }

  boolean isSpringContainerClass(final Class<?> clazz) {
    return (clazz.getName().startsWith("org.springframework.")
        && !AnnotatedElementUtils.isAnnotated(
            ClassUtils.getUserClass(clazz), org.springframework.stereotype.Component.class));
  }

  <A extends Annotation> void scanHandlers(
      final Class<A> annotationType, final BiConsumer<String, Class<?>> processor) {
    final String[] beanNames = beanFactory.getBeanNamesForAnnotation(annotationType);
    for (String beanName : beanNames) {
      Class<?> type = null;
      try {
        type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
      } catch (Throwable e) {
        if (log.isDebugEnabled()) {
          log.debug("Could not resolve target class for bean with name '{}'", beanName, e);
        }
      }

      if (type != null && !isSpringContainerClass(type)) {
        try {
          processor.accept(beanName, type);
        } catch (Throwable ex) {
          throw new BeanInitializationException(
              "Failed to process @"
                  + annotationType.getSimpleName()
                  + " annotation on bean with name '"
                  + beanName
                  + "': "
                  + ex.getMessage(),
              ex);
        }
      }
    }
  }

  <A extends Annotation> Map<Method, A> resolveAnnotatedMethods(
      final Class<?> targetType, final Class<A> annotationType, final String beanName) {
    try {
      return MethodIntrospector.selectMethods(
          targetType,
          (MethodIntrospector.MetadataLookup<A>)
              method -> AnnotatedElementUtils.findMergedAnnotation(method, annotationType));
    } catch (Throwable e) {
      if (log.isDebugEnabled()) {
        log.debug(
            "Could not resolve @ExceptionHandler methods for bean with name '{}'", beanName, e);
      }
    }

    return Map.of();
  }
}
