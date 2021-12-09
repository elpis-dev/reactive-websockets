package org.elpis.reactive.websockets.config;

import io.micrometer.core.instrument.Tags;
import lombok.Getter;
import lombok.Setter;
import org.elpis.reactive.websockets.config.annotation.SocketAnnotationEvaluatorFactory;
import org.elpis.reactive.websockets.config.event.EventManagerConfiguration;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.elpis.reactive.websockets.mertics.WebSocketMetricsService;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.BasicWebSocketResource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.elpis.reactive.websockets.mertics.WebSocketMetricsService.MeterConstants.*;

@Configuration
@Import({SocketAnnotationEvaluatorFactory.class, WebSocketMetricsService.class, EventManagerConfiguration.class})
public class WebSocketConfiguration {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfiguration.class);

    private final JsonMapper jsonMapper;

    private final Map<String, WebHandlerResourceDescriptor> descriptorRegistry = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;
    private final SocketAnnotationEvaluatorFactory socketAnnotationEvaluatorFactory;
    private final WebSocketMetricsService webSocketMetricsService;
    private final WebSessionRegistry sessionRegistry;

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

    @Bean
    public HandlerMapping handlerMapping(final List<? extends BasicWebSocketResource> webSocketResources) {
        final Map<String, WebSocketHandler> webSocketHandlers = new HashMap<>();

        webSocketResources.forEach(resource -> {
            final Class<? extends BasicWebSocketResource> clazz = resource.getClass();

            Stream.of(clazz.getAnnotations())
                    .findFirst()
                    .map(annotation -> TypeUtils.cast(annotation, SocketResource.class))
                    .ifPresent(socketResource -> this.registerMappings(socketResource, webSocketHandlers, clazz));
        });


        return new SimpleUrlHandlerMapping(webSocketHandlers, 10);
    }

    private void registerMappings(final SocketResource socketResource,
                                  final Map<String, WebSocketHandler> webSocketHandlers,
                                  final Class<? extends BasicWebSocketResource> clazz) {

        Stream.of(clazz.getDeclaredMethods())
                .forEach(method -> {
                    if (method.isAnnotationPresent(Outbound.class)) {
                        this.configureEmitter(socketResource, method, clazz);
                    }

                    if (method.isAnnotationPresent(Inbound.class)) {
                        this.configureListener(socketResource, method, clazz);
                    }
                });

        this.descriptorRegistry.forEach((pathTemplate, configEntity) ->
                webSocketHandlers.put(pathTemplate, this.handle(pathTemplate, configEntity)));
    }

    private void configureListener(final SocketResource socketResource,
                                   final Method method,
                                   final Class<? extends BasicWebSocketResource> clazz) {

        final String inboundPathTemplate = socketResource.value() + method.getAnnotation(Inbound.class).value();

        final var webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(inboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor<>(clazz));

        if (nonNull(webHandlerResourceDescriptor.getInboundMethod())) {
            throw new ValidationException(String.format("Cannot register method `@Inbound %s()` on `%s` since " +
                            "`@Inbound %s()` was already registered on provided path", method.getName(), inboundPathTemplate,
                    webHandlerResourceDescriptor.getInboundMethod().getName()));
        }

        webHandlerResourceDescriptor.setInboundMethod(method);

        this.descriptorRegistry.put(inboundPathTemplate, webHandlerResourceDescriptor);
    }

    private void configureEmitter(final SocketResource socketResource,
                                  final Method method,
                                  final Class<? extends BasicWebSocketResource> clazz) {

        if (!Publisher.class.isAssignableFrom(method.getReturnType())) {
            throw new ValidationException(String.format("Cannot register method `@Outbound %s()`. " +
                    "Reason: method should return a Publisher instance", method.getName()));
        }

        final String outboundPathTemplate = socketResource.value() + method.getAnnotation(Outbound.class).value();

        final var webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(outboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor<>(clazz));

        if (nonNull(webHandlerResourceDescriptor.getOutboundMethod())) {
            throw new ValidationException(String.format("Cannot register method `@Outbound %s()` on `%s` since " +
                            "`@Outbound %s()` was already registered on provided path", method.getName(), outboundPathTemplate,
                    webHandlerResourceDescriptor.getOutboundMethod().getName()));
        }

        webHandlerResourceDescriptor.setOutboundMethod(method);

        this.descriptorRegistry.put(outboundPathTemplate, webHandlerResourceDescriptor);
    }

    private WebSocketHandler handle(final String pathTemplate, final WebHandlerResourceDescriptor<?> configEntity) {
        return session ->
                this.webSocketMetricsService.withTimer(stop -> {
                    final HandshakeInfo handshakeInfo = session.getHandshakeInfo();
                    final BasicWebSocketResource resource = this.applicationContext.getBean(configEntity.getClazz());

                    log.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", session.getId(), handshakeInfo.getUri(),
                            handshakeInfo.getRemoteAddress());

                    final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                            .isOpen(session::isOpen)
                            .protocol(handshakeInfo.getSubProtocol())
                            .id(session.getId())
                            .uri(handshakeInfo.getUri())
                            .remoteAddress(handshakeInfo.getRemoteAddress())
                            .closeStatus(session.closeStatus())
                            .build();

                    this.sessionRegistry.put(session.getId(), webSocketSessionInfo);

                    return session.getHandshakeInfo().getPrincipal()
                            .switchIfEmpty(Mono.just(new Anonymous()))
                            .flatMap(principal -> {
                                final WebSocketSessionContext webSocketSessionContext =
                                        this.getWebSocketSessionContext(pathTemplate, session, handshakeInfo, principal);

                                return stop.andThen(taskTime -> this.processConnection(resource, session, configEntity, webSocketSessionContext))
                                        .apply(SESSION_CONNECTION_TIME.getKey(), Tags.of(RESULT, SUCCESS));
                            })
                            .doOnError(throwable -> {
                                stop.apply(SESSION_CONNECTION_TIME.getKey(), Tags.of(RESULT, FAILURE));

                                log.error(throwable.getMessage());
                            });
                });
    }

    private WebSocketSessionContext getWebSocketSessionContext(final String pathTemplate, final WebSocketSession session,
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

    private Mono<Void> processConnection(final BasicWebSocketResource resource, final WebSocketSession session,
                                         final WebHandlerResourceDescriptor<?> configEntity,
                                         final WebSocketSessionContext webSocketSessionContext) {

        final var socketMessageFlux = this.processMethod(configEntity, resource, webSocketSessionContext);

        final Mono<WebSocketMessage> webSocketMessageFlux = Mono.from(socketMessageFlux)
                .flatMap(any -> CloseStatus.class.isAssignableFrom(any.getClass())
                        ? session.close(TypeUtils.cast(any, CloseStatus.class)).cast(WebSocketMessage.class)
                        : this.jsonMapper.applyWithMono(any).map(session::textMessage));

        return session.send(webSocketMessageFlux);
    }

    private Publisher<?> processMethod(final WebHandlerResourceDescriptor<?> configEntity,
                                       final BasicWebSocketResource resource,
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
                throw new WebSocketConfigurationException("Unable to invoke method `@Outbound " + method.getName() + "()` with request parameters" + e.getMessage());
            }
        }).orElseGet(Flux::never);

        if (!fullMethod) {
            inbound.ifPresent(method -> {
                final Object[] parameters = this.processMethodParameters(method, webSocketSessionContext);

                try {
                    method.invoke(resource, parameters);
                } catch (Exception e) {
                    throw new WebSocketConfigurationException("Unable to invoke method `@Inbound " + method.getName() + "()` with request parameters " +
                            "and message publisher instance" + e.getMessage());
                }
            });
        }

        webSocketSessionContext.setInbound(inbound.isPresent());
        webSocketSessionContext.setOutbound(outbound.isPresent());

        return publisher;
    }

    @SuppressWarnings("unchecked")
    private Object[] processMethodParameters(final Method method, final WebSocketSessionContext webSocketSessionContext) {
        final String methodName = method.getName();

        webSocketSessionContext.setInbound(method.isAnnotationPresent(Inbound.class));
        webSocketSessionContext.setOutbound(method.isAnnotationPresent(Outbound.class));

        return Stream.of(method.getParameters()).map(parameter -> this.socketAnnotationEvaluatorFactory.getEvaluator(parameter.getAnnotations())
                .map(evaluator -> evaluator.evaluate(webSocketSessionContext, parameter, methodName, parameter.getAnnotation(evaluator.getAnnotationType())))
                .orElse(null)).toArray();
    }

    private static class WebHandlerResourceDescriptor<T extends BasicWebSocketResource> {
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
