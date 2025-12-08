package io.github.elpis.reactive.websockets.config.event;

import io.github.elpis.reactive.websockets.config.WebSocketCloseStatus;
import io.github.elpis.reactive.websockets.event.matcher.EventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.annotation.CloseStatusHandler;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.annotation.SessionCloseStatus;
import io.github.elpis.reactive.websockets.event.matcher.impl.ClosedSessionEventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Configuration support {@link ClientSessionClosedEvent} handling for {@link CloseStatusHandler @CloseStatusHandler} annotated beans.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @see CloseStatusHandler
 * @since 0.1.0
 */
@Configuration
public class ClosedConnectionHandlerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ClosedConnectionHandlerConfiguration.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(Queues.XS_BUFFER_SIZE);

    @Bean
    public EventSelectorMatcher<ClientSessionClosedEvent> closedEventSelectorMatcher() {
        return new ClosedSessionEventSelectorMatcher();
    }

    @Bean
    public ClosedEventHandlers closedEventHandlers(final ApplicationContext context) {
        final ClosedEventHandlers handlers = new ClosedEventHandlers();

        context.getBeansWithAnnotation(CloseStatusHandler.class)
                .values()
                .forEach(closeStatusHandler -> Stream.of(closeStatusHandler.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(SessionCloseStatus.class))
                        .forEach(method -> {
                            if (method.getParameterCount() > 1) {
                                throw new WebSocketConfigurationException("Found two or more parameters on " +
                                        "@SessionCloseStatus `%s.%s(...)` - one or none are only supported",
                                        closeStatusHandler.getClass().getSimpleName(), method.getName());
                            }

                            final SessionCloseStatus sessionCloseStatus = method.getAnnotation(SessionCloseStatus.class);
                            final int[] closeCodes = this.getWebSocketCloseCodes(sessionCloseStatus.value(), sessionCloseStatus.code());

                            IntStream.of(closeCodes)
                                    .forEach(closeCode -> handlers.add(closeCode, this.getClientSessionClosedEventFunction(closeStatusHandler, method)));
                        }));

        return handlers;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> closedSessionListener(final ClosedEventHandlers closedEventHandlers,
                                                                            final WebSocketEventManagerFactory eventManagerFactory) {

        return event -> eventManagerFactory.getEventManager(ClientSessionClosedEvent.class)
                .asFlux()
                .parallel()
                .runOn(Schedulers.fromExecutorService(executorService))
                .subscribe(clientSessionClosedEvent -> {
                    Optional.ofNullable(closedEventHandlers.get(clientSessionClosedEvent.payload().getCloseStatus().getCode()))
                        .ifPresent(functions -> functions.forEach(clientSessionClosedEventConsumer -> clientSessionClosedEventConsumer
                            .accept(clientSessionClosedEvent)));

                    Optional.ofNullable(closedEventHandlers.get(WebSocketCloseStatus.ALL.getStatusCode()))
                            .ifPresent(functions -> functions.forEach(clientSessionClosedEventConsumer -> clientSessionClosedEventConsumer
                                    .accept(clientSessionClosedEvent)));
                });
    }

    private Consumer<ClientSessionClosedEvent> getClientSessionClosedEventFunction(final Object closeStatusHandler, final Method method) {
        return event -> {
            try {
                final boolean isValid = !method.isAnnotationPresent(EventSelector.class) ||
                        this.closedEventSelectorMatcher().process(event, method.getAnnotation(EventSelector.class));

                if (isValid) {
                    if (method.getParameterCount() == 0) {
                        method.invoke(closeStatusHandler);
                    } else {
                        method.invoke(closeStatusHandler, event);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException exception) {
                log.error(String.format("Cannot call `@SessionCloseStatus %s.%s()` due occurred exception",
                        closeStatusHandler.getClass().getSimpleName(), method.getName()), exception);
            }
        };
    }

    private int[] getWebSocketCloseCodes(final WebSocketCloseStatus[] webSocketCloseStatuses, final int[] manualCodes) {
        if (manualCodes.length > 0) {
            IntStream.of(manualCodes)
                    .forEach(code -> {
                        if (!WebSocketCloseStatus.isValidCode(code)) {
                            throw new WebSocketConfigurationException("Cannot process `@SessionCloseStatus({%s})` " +
                                    "- code %s is not valid. Valid error code range is from 1000 to 4999", Arrays.toString(manualCodes), code);
                        }
                    });

            return manualCodes;
        } else if (webSocketCloseStatuses.length > 0) {
            return Stream.of(webSocketCloseStatuses)
                    .mapToInt(WebSocketCloseStatus::getStatusCode)
                    .toArray();
        }

        return new int[]{WebSocketCloseStatus.ALL.getStatusCode()};
    }

    public static class ClosedEventHandlers extends LinkedMultiValueMap<Integer, Consumer<ClientSessionClosedEvent>> {
        //Shortener
    }
}
