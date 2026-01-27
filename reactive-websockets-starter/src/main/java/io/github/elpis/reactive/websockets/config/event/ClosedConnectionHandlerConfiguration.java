package io.github.elpis.reactive.websockets.config.event;

import io.github.elpis.reactive.websockets.config.WebSocketCloseStatus;
import io.github.elpis.reactive.websockets.context.ReactiveWebSocketClosedEventHandlersResolver;
import io.github.elpis.reactive.websockets.event.annotation.CloseStatusHandler;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.matcher.ReactiveWebSocketEventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.matcher.impl.ClosedSessionReactiveWebSocketEventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

/**
 * Configuration support {@link ClientSessionClosedEvent} handling for {@link
 * CloseStatusHandler @CloseStatusHandler} annotated beans.
 *
 * @see org.springframework.context.annotation.Configuration
 * @see CloseStatusHandler
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Configuration
public class ClosedConnectionHandlerConfiguration {
  private final ExecutorService executorService =
      Executors.newFixedThreadPool(Queues.XS_BUFFER_SIZE);

  /**
   * Creates a {@link ReactiveWebSocketEventSelectorMatcher} for {@link ClientSessionClosedEvent}
   * events.
   *
   * @return the ReactiveWebSocketEventSelectorMatcher bean
   */
  @Bean
  public ReactiveWebSocketEventSelectorMatcher<ClientSessionClosedEvent>
      closedEventSelectorMatcher() {
    return new ClosedSessionReactiveWebSocketEventSelectorMatcher();
  }

  /**
   * Creates an ApplicationListener that listens for ApplicationReadyEvent to handle closed
   * WebSocket sessions.
   *
   * @param resolver the ReactiveWebSocketClosedEventHandlersResolver
   * @param eventManagerFactory the ReactiveWebSocketEventManagerFactory
   * @return the ApplicationListener bean
   */
  @Bean
  public ApplicationListener<ApplicationReadyEvent> closedSessionListener(
      final ReactiveWebSocketClosedEventHandlersResolver resolver,
      final ReactiveWebSocketEventManagerFactory eventManagerFactory) {

    return event ->
        eventManagerFactory
            .getEventManager(ClientSessionClosedEvent.class)
            .asFlux()
            .parallel()
            .runOn(Schedulers.fromExecutorService(executorService))
            .subscribe(
                clientSessionClosedEvent -> {
                  Optional.ofNullable(
                          resolver.getEventHandlers(
                              clientSessionClosedEvent.payload().getCloseStatus().getCode()))
                      .ifPresent(
                          functions ->
                              functions.forEach(
                                  clientSessionClosedEventConsumer ->
                                      clientSessionClosedEventConsumer.accept(
                                          clientSessionClosedEvent)));

                  Optional.ofNullable(
                          resolver.getEventHandlers(WebSocketCloseStatus.ALL.getStatusCode()))
                      .ifPresent(
                          functions ->
                              functions.forEach(
                                  clientSessionClosedEventConsumer ->
                                      clientSessionClosedEventConsumer.accept(
                                          clientSessionClosedEvent)));
                });
  }
}
