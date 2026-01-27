package io.github.elpis.reactive.websockets.context;

import io.github.elpis.reactive.websockets.config.WebSocketCloseStatus;
import io.github.elpis.reactive.websockets.event.annotation.CloseStatusHandler;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.annotation.SessionCloseStatus;
import io.github.elpis.reactive.websockets.event.matcher.ReactiveWebSocketEventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Resolver for WebSocket closed event handlers annotated with {@link CloseStatusHandler} and {@link
 * SessionCloseStatus}. This class scans the application context for beans with methods annotated to
 * handle WebSocket session close events, and organizes them based on the specified close status
 * codes.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 * @see AbstractReactiveWebSocketExceptionResolver
 */
public final class ReactiveWebSocketClosedEventHandlersResolver
    extends AbstractReactiveWebSocketExceptionResolver {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveWebSocketClosedEventHandlersResolver.class);

  private final ReactiveWebSocketEventSelectorMatcher<ClientSessionClosedEvent>
      closedEventSelectorMatcher;
  private final MultiValueMap<Integer, Consumer<ClientSessionClosedEvent>> handlers =
      new LinkedMultiValueMap<>();

  @SuppressWarnings("unchecked")
  public ReactiveWebSocketClosedEventHandlersResolver(final ApplicationContext applicationContext) {
    super(applicationContext);

    final ResolvableType resolvableType =
        ResolvableType.forClassWithGenerics(
            ReactiveWebSocketEventSelectorMatcher.class, ClientSessionClosedEvent.class);
    this.closedEventSelectorMatcher =
        (ReactiveWebSocketEventSelectorMatcher<ClientSessionClosedEvent>)
            applicationContext.getBeanProvider(resolvableType).getIfAvailable();
  }

  public void initialize() {
    this.scanHandlers(CloseStatusHandler.class, this::processBean);
  }

  /**
   * Gets the map of close status codes to their corresponding event handlers.
   *
   * @return a MultiValueMap where the key is the close status code and the value is a list of event
   *     handlers
   */
  public MultiValueMap<Integer, Consumer<ClientSessionClosedEvent>> getHandlers() {
    return handlers;
  }

  /**
   * Gets the first event handler for the specified close status code.
   *
   * @param code the close status code
   * @return the first event handler for the specified code, or null if none exist
   */
  public Consumer<ClientSessionClosedEvent> getEventHandler(final Integer code) {
    return this.handlers.getFirst(code);
  }

  /**
   * Gets the list of event handlers for the specified close status code.
   *
   * @param code the close status code
   * @return a list of event handlers for the specified code, or null if none exist
   */
  public List<Consumer<ClientSessionClosedEvent>> getEventHandlers(final Integer code) {
    return this.handlers.get(code);
  }

  private void processBean(final String beanName, final Class<?> targetType) {
    if (!this.nonAnnotatedClasses.contains(targetType)) {
      final Map<Method, SessionCloseStatus> annotatedMethods =
          this.resolveAnnotatedMethods(targetType, SessionCloseStatus.class, beanName);

      if (CollectionUtils.isEmpty(annotatedMethods)) {
        this.nonAnnotatedClasses.add(targetType);
        if (log.isTraceEnabled()) {
          log.trace(
              "No @SessionCloseStatus annotations found on bean class: {}", targetType.getName());
        }
      } else {
        for (Map.Entry<Method, SessionCloseStatus> entry : annotatedMethods.entrySet()) {
          final Method method = entry.getKey();
          final SessionCloseStatus annotation = entry.getValue();

          if (method.getParameterCount() > 1) {
            throw new WebSocketConfigurationException(
                "Found two or more parameters on "
                    + "@SessionCloseStatus `%s.%s(...)` - one or none are only supported",
                targetType.getSimpleName(), method.getName());
          }

          final Method methodToUse =
              AopUtils.selectInvocableMethod(method, applicationContext.getType(beanName));
          final int[] closeCodes =
              this.getWebSocketCloseCodes(annotation.value(), annotation.code());

          IntStream.of(closeCodes)
              .forEach(
                  closeCode ->
                      handlers.add(
                          closeCode,
                          this.getClientSessionClosedEventFunction(
                              applicationContext.getBean(beanName, targetType), methodToUse)));
        }
      }
    }
  }

  private Consumer<ClientSessionClosedEvent> getClientSessionClosedEventFunction(
      final Object closeStatusHandler, final Method method) {
    return event -> {
      try {
        final boolean isValid =
            !method.isAnnotationPresent(EventSelector.class)
                || this.closedEventSelectorMatcher.process(
                    event, method.getAnnotation(EventSelector.class));

        if (isValid) {
          if (method.getParameterCount() == 0) {
            method.invoke(closeStatusHandler);
          } else {
            method.invoke(closeStatusHandler, event);
          }
        }
      } catch (IllegalAccessException | InvocationTargetException exception) {
        if (log.isErrorEnabled()) {
          log.error(
              "Cannot call `@SessionCloseStatus {}.{}()` due occurred exception",
              closeStatusHandler.getClass().getSimpleName(),
              method.getName(),
              exception);
        }
      }
    };
  }

  private int[] getWebSocketCloseCodes(
      final WebSocketCloseStatus[] webSocketCloseStatuses, final int[] manualCodes) {
    if (manualCodes.length > 0) {
      IntStream.of(manualCodes)
          .forEach(
              code -> {
                if (!WebSocketCloseStatus.isValidCode(code)) {
                  throw new WebSocketConfigurationException(
                      "Cannot process `@SessionCloseStatus({%s})` "
                          + "- code %s is not valid. Valid error code range is from 1000 to 4999",
                      Arrays.toString(manualCodes), code);
                }
              });

      return manualCodes;
    } else if (webSocketCloseStatuses.length > 0) {
      return Stream.of(webSocketCloseStatuses)
          .mapToInt(WebSocketCloseStatus::getStatusCode)
          .toArray();
    }

    return new int[] {WebSocketCloseStatus.ALL.getStatusCode()};
  }
}
