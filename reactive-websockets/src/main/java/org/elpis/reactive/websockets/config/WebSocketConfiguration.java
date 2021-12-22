package org.elpis.reactive.websockets.config;

import io.micrometer.core.instrument.Tags;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.elpis.reactive.websockets.config.annotation.SocketAnnotationEvaluatorFactory;
import org.elpis.reactive.websockets.config.event.ClosedConnectionHandlerConfiguration;
import org.elpis.reactive.websockets.config.event.EventManagerConfiguration;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.elpis.reactive.websockets.exception.WebSocketInboundException;
import org.elpis.reactive.websockets.exception.WebSocketOutboundException;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.elpis.reactive.websockets.mertics.WebSocketMetricsService;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.controller.Inbound;
import org.elpis.reactive.websockets.web.annotation.controller.Outbound;
import org.elpis.reactive.websockets.web.annotation.controller.SocketResource;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.elpis.reactive.websockets.mertics.WebSocketMetricsService.MeterConstants.*;

/**
 * Configuration class that setups all the websocket endpoints and processes annotated methods.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Configuration
@Import({
        SocketAnnotationEvaluatorFactory.class,
        WebSocketMetricsService.class,
        EventManagerConfiguration.class,
        ClosedConnectionHandlerConfiguration.class
})
public class WebSocketConfiguration {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfiguration.class);

    private static final int HANDLER_ORDER = 10;

    private final Map<String, WebHandlerResourceDescriptor> descriptorRegistry = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;
    private final SocketAnnotationEvaluatorFactory socketAnnotationEvaluatorFactory;
    private final WebSocketMetricsService webSocketMetricsService;
    private final WebSessionRegistry sessionRegistry;
    private final JsonMapper jsonMapper;

    public WebSocketConfiguration(final ApplicationContext applicationContext,
                                  final SocketAnnotationEvaluatorFactory socketAnnotationEvaluatorFactory,
                                  final WebSocketMetricsService webSocketMetricsService,
                                  final WebSessionRegistry sessionRegistry,
                                  final JsonMapper jsonMapper) {

        this.applicationContext = applicationContext;
        this.socketAnnotationEvaluatorFactory = socketAnnotationEvaluatorFactory;
        this.webSocketMetricsService = webSocketMetricsService;
        this.sessionRegistry = sessionRegistry;
        this.jsonMapper = jsonMapper;
    }

    /**
     * {@link HandlerMapping} bean with all {@link Inbound @Inbound} and {@link Outbound @Outbound} resources.
     *
     * @return {@link HandlerMapping}
     * @since 0.1.0
     */
    @Bean
    public HandlerMapping handlerMapping(@NonNull final ApplicationContext applicationContext) {
        applicationContext.getBeansWithAnnotation(SocketResource.class)
                .forEach((name, bean) -> {
                    final Class<?> clazz = bean.getClass();

                    Stream.of(clazz.getAnnotations())
                            .findFirst()
                            .map(annotation -> TypeUtils.cast(annotation, SocketResource.class))
                            .ifPresent(socketResource -> this.registerMappings(socketResource, clazz));
                });

        final Map<String, WebSocketHandler> webSocketHandlers = this.descriptorRegistry.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> this.handle(entry.getKey(), entry.getValue())));

        return new SimpleUrlHandlerMapping(webSocketHandlers, HANDLER_ORDER);
    }

    private void registerMappings(final SocketResource socketResource, final Class<?> clazz) {
        Stream.of(clazz.getDeclaredMethods())
                .forEach(method -> {
                    if (method.isAnnotationPresent(Outbound.class)) {
                        this.configurePublisher(socketResource, method, clazz);
                    }

                    if (method.isAnnotationPresent(Inbound.class)) {
                        this.configureListener(socketResource, method, clazz);
                    }
                });
    }

    private void configureListener(final SocketResource resource, final Method method, final Class<?> clazz) {
        final String inboundPathTemplate = resource.value() + method.getAnnotation(Inbound.class).value();

        final var resourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(inboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor<>(clazz));

        if (nonNull(resourceDescriptor.getInboundMethod())) {
            throw new WebSocketConfigurationException("Cannot register method `@Inbound %s()` on `%s` since `@Inbound %s()` " +
                    "was already registered on provided path", method.getName(), inboundPathTemplate, resourceDescriptor.getInboundMethod().getName());
        }

        resourceDescriptor.setInboundMethod(method);

        this.descriptorRegistry.put(inboundPathTemplate, resourceDescriptor);
    }

    private void configurePublisher(final SocketResource resource, final Method method, final Class<?> clazz) {
        if (!Publisher.class.isAssignableFrom(method.getReturnType())) {
            throw new WebSocketConfigurationException("Cannot register method `@Outbound %s()`. Reason: method should " +
                    "return a Publisher instance", method.getName());
        }

        final String outboundPathTemplate = resource.value() + method.getAnnotation(Outbound.class).value();

        final var resourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(outboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor<>(clazz));

        if (nonNull(resourceDescriptor.getOutboundMethod())) {
            throw new WebSocketConfigurationException("Cannot register method `@Outbound %s()` on `%s` since `@Outbound %s()` " +
                    "was already registered on provided path", method.getName(), outboundPathTemplate, resourceDescriptor.getOutboundMethod().getName());
        }

        resourceDescriptor.setOutboundMethod(method);

        this.descriptorRegistry.put(outboundPathTemplate, resourceDescriptor);
    }

    private WebSocketHandler handle(final String pathTemplate, final WebHandlerResourceDescriptor<?> configEntity) {
        return session ->
                this.webSocketMetricsService.withTimer(stop -> {
                    final HandshakeInfo handshakeInfo = session.getHandshakeInfo();
                    final Object resource = this.applicationContext.getBean(configEntity.getClazz());

                    log.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", session.getId(), handshakeInfo.getUri(),
                            handshakeInfo.getRemoteAddress());

                    final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                            .isOpen(session::isOpen)
                            .protocol(handshakeInfo.getSubProtocol())
                            .id(session.getId())
                            .host(handshakeInfo.getUri().getHost())
                            .port(handshakeInfo.getUri().getPort())
                            .path(pathTemplate)
                            .remoteAddress(handshakeInfo.getRemoteAddress())
                            .closeStatus(session.closeStatus())
                            .build();

                    this.sessionRegistry.put(session.getId(), webSocketSessionInfo);

                    return session.getHandshakeInfo().getPrincipal()
                            .switchIfEmpty(Mono.just(new Anonymous()))
                            .flatMap(principal -> {
                                final WebSocketSessionContext webSocketSessionContext =
                                        this.getSessionContext(pathTemplate, session, handshakeInfo, principal);

                                return stop.andThen(taskTime -> this.processConnection(resource, session, configEntity, webSocketSessionContext))
                                        .apply(SESSION_CONNECTION_TIME.getKey(), Tags.of(RESULT, SUCCESS));
                            })
                            .doOnError(throwable -> {
                                stop.apply(SESSION_CONNECTION_TIME.getKey(), Tags.of(RESULT, FAILURE));

                                log.error(throwable.getMessage());
                            });
                });
    }

    private WebSocketSessionContext getSessionContext(final String pathTemplate, final WebSocketSession session,
                                                      final HandshakeInfo handshakeInfo, final Principal principal) {

        final String uriPath = handshakeInfo.getUri().getPath();
        final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

        final var pathParameters = uriTemplate.match(uriPath);
        final var queryParameters = UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build()
                .getQueryParams();
        final var headers = handshakeInfo.getHeaders();

        return WebSocketSessionContext.builder()
                .authentication(principal)
                .pathParameters(pathParameters)
                .queryParameters(queryParameters)
                .headers(headers)
                .messageStream(session::receive)
                .build();
    }

    private Mono<Void> processConnection(final Object resource, final WebSocketSession session,
                                         final WebHandlerResourceDescriptor<?> configEntity,
                                         final WebSocketSessionContext webSocketSessionContext) {

        final var socketMessageFlux = this.processMethod(configEntity, resource, webSocketSessionContext);

        final Flux<WebSocketMessage> webSocketMessageFlux = Flux.from(socketMessageFlux)
                .flatMap(any -> CloseStatus.class.isAssignableFrom(any.getClass())
                        ? session.close(TypeUtils.cast(any, CloseStatus.class)).cast(WebSocketMessage.class)
                        : this.jsonMapper.applyWithFlux(any).map(session::textMessage));

        return session.send(webSocketMessageFlux);
    }

    private Publisher<?> processMethod(final WebHandlerResourceDescriptor<?> configEntity, final Object resource,
                                       final WebSocketSessionContext webSocketSessionContext) {

        final Optional<Method> outbound = Optional.ofNullable(configEntity.getOutboundMethod());
        final Optional<Method> inbound = Optional.ofNullable(configEntity.getInboundMethod());

        final boolean fullMethod = outbound
                .map(method -> method.isAnnotationPresent(Inbound.class))
                .orElse(false);

        final Publisher<?> publisher = outbound.map(method -> {
            final Object[] parameters = this.processMethodParameters(method, webSocketSessionContext);

            try {
                return TypeUtils.cast(method.invoke(resource, parameters), Publisher.class);
            } catch (Exception e) {
                throw new WebSocketOutboundException("Unable to invoke method `@Outbound %s()` with request parameters %s",
                        method.getName(), e.getMessage());
            }
        }).orElseGet(Flux::never);

        if (!fullMethod) {
            inbound.ifPresent(method -> {
                final Object[] parameters = this.processMethodParameters(method, webSocketSessionContext);

                try {
                    method.invoke(resource, parameters);
                } catch (Exception e) {
                    throw new WebSocketInboundException("Unable to invoke method `@Inbound %s()` with request parameters " +
                            "and message publisher instance %s", method.getName(), e.getMessage());
                }
            });
        }

        webSocketSessionContext.setInbound(inbound.isPresent());
        webSocketSessionContext.setOutbound(outbound.isPresent());

        return publisher;
    }

    @SuppressWarnings("unchecked")
    private Object[] processMethodParameters(final Method method, final WebSocketSessionContext webSocketSessionContext) {
        webSocketSessionContext.setInbound(method.isAnnotationPresent(Inbound.class));
        webSocketSessionContext.setOutbound(method.isAnnotationPresent(Outbound.class));

        return Stream.of(method.getParameters()).map(parameter -> this.socketAnnotationEvaluatorFactory.getEvaluator(parameter.getAnnotations())
                .map(evaluator -> evaluator.evaluate(webSocketSessionContext, parameter, parameter.getAnnotation(evaluator.getAnnotationType())))
                .orElse(null)).toArray();
    }

    private static final class WebHandlerResourceDescriptor<T> {
        @Getter
        @Setter
        private Method outboundMethod;

        @Getter
        @Setter
        private Method inboundMethod;

        @Getter
        private final Class<T> clazz;

        public WebHandlerResourceDescriptor(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

}
