package org.elpis.reactive.websockets.config.event;

import org.elpis.reactive.websockets.config.model.WebSocketCloseStatus;
import org.elpis.reactive.websockets.event.EventSelectorMatcher;
import org.elpis.reactive.websockets.event.annotation.CloseStatusHandler;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.annotation.SessionCloseStatus;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
public final class ClosedConnectionHandlerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ClosedConnectionHandlerConfiguration.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(Queues.XS_BUFFER_SIZE);

    private final ApplicationContext applicationContext;
    private final WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager;
    private final EventSelectorMatcher<ClientSessionClosedEvent> closedEventSelectorMatcher;

    private final Map<Integer, List<Consumer<ClientSessionClosedEvent>>> handlers = new ConcurrentHashMap<>();

    public ClosedConnectionHandlerConfiguration(final ApplicationContext applicationContext,
                                                final WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager,
                                                final EventSelectorMatcher<ClientSessionClosedEvent> closedEventSelectorMatcher) {

        this.applicationContext = applicationContext;
        this.closedEventWebSocketEventManager = closedEventWebSocketEventManager;
        this.closedEventSelectorMatcher = closedEventSelectorMatcher;

        this.init();
    }

    private void init() {
        applicationContext.getBeansWithAnnotation(CloseStatusHandler.class)
                .values()
                .forEach(closeStatusHandler -> Stream.of(closeStatusHandler.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(SessionCloseStatus.class))
                        .forEach(method -> {
                            if (method.getParameterCount() > 1) {
                                throw new WebSocketConfigurationException(String.format("Found two or more parameters on " +
                                                "@SessionCloseStatus `%s.%s(...)` - one or none are only supported",
                                        closeStatusHandler.getClass().getSimpleName(), method.getName()));
                            }

                            final SessionCloseStatus sessionCloseStatus = method.getAnnotation(SessionCloseStatus.class);
                            final int[] closeCodes = this.getWebSocketCloseCodes(sessionCloseStatus.value(), sessionCloseStatus.code());

                            IntStream.of(closeCodes).forEach(closeCode -> {
                                if (!handlers.containsKey(closeCode)) {
                                    this.handlers.put(closeCode, new ArrayList<>());
                                }

                                this.handlers.get(closeCode).add(this.getClientSessionClosedEventFunction(closeStatusHandler, method));
                            });
                        }));
    }

    private Consumer<ClientSessionClosedEvent> getClientSessionClosedEventFunction(final Object closeStatusHandler, final Method method) {
        return event -> {
            try {
                final boolean isValid = !method.isAnnotationPresent(EventSelector.class) ||
                        this.closedEventSelectorMatcher.select(event, method.getAnnotation(EventSelector.class));

                if (!isValid) {
                    return;
                }

                if (method.getParameterCount() == 0) {
                    method.invoke(closeStatusHandler);
                } else {
                    method.invoke(closeStatusHandler, event);
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
                            throw new WebSocketConfigurationException(String.format("Cannot process `@SessionCloseStatus({%s})` " +
                                    "- code %s is not valid. Valid error code range is from 1000 to 4999", Arrays.toString(manualCodes), code));
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

    @PostConstruct
    private void listen() {
        this.closedEventWebSocketEventManager
                .asFlux()
                .parallel()
                .runOn(Schedulers.fromExecutorService(executorService))
                .subscribe(clientSessionClosedEvent -> {
                    Optional.ofNullable(this.handlers.get(clientSessionClosedEvent.payload().getCloseStatus().getCode()))
                            .ifPresent(functions -> functions.forEach(clientSessionClosedEventConsumer -> clientSessionClosedEventConsumer
                                    .accept(clientSessionClosedEvent)));

                    Optional.ofNullable(this.handlers.get(WebSocketCloseStatus.ALL.getStatusCode()))
                            .ifPresent(functions -> functions.forEach(clientSessionClosedEventConsumer -> clientSessionClosedEventConsumer
                                    .accept(clientSessionClosedEvent)));
                });
    }
}
