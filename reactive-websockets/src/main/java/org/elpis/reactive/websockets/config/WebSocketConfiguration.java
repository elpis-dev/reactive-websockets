package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.config.annotations.SocketAnnotationEvaluatorFactory;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.mappers.JsonMapper;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.utils.TerminateBean;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Inbound;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.web.model.WebSocketSessionInfo;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
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

@Configuration
@Import(SocketAnnotationEvaluatorFactory.class)
public class WebSocketConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfiguration.class);

    private final JsonMapper jsonMapper = new JsonMapper();

    private final Map<String, WebHandlerResourceDescriptor> descriptorRegistry = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;
    private final SocketAnnotationEvaluatorFactory socketAnnotationEvaluatorFactory;

    public WebSocketConfiguration(final ApplicationContext applicationContext,
                                  final SocketAnnotationEvaluatorFactory socketAnnotationEvaluatorFactory) {

        this.applicationContext = applicationContext;
        this.socketAnnotationEvaluatorFactory = socketAnnotationEvaluatorFactory;
    }

    @Bean
    public Map<String, WebSocketSessionInfo> sessionRegistry() {
        return new ConcurrentHashMap<>();
    }

    @Bean("webSocketsTerminateBean")
    public TerminateBean terminateBean() {
        return TerminateBean.with(() -> {
            LOG.info("Gracefully terminating all socket sessions...");
            sessionRegistry().forEach((sessionId, sessionInfo) -> {
                LOG.debug("Terminating session: {}", sessionId);

                sessionInfo.getClose().run();
            });
        });
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


        return new SimpleUrlHandlerMapping() {
            {
                setUrlMap(webSocketHandlers);
                setOrder(10);
            }
        };
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

        final WebHandlerResourceDescriptor<?> webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(inboundPathTemplate))
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

        final WebHandlerResourceDescriptor<?> webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(outboundPathTemplate))
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
        return session -> {
            final HandshakeInfo handshakeInfo = session.getHandshakeInfo();
            final BasicWebSocketResource resource = this.applicationContext.getBean(configEntity.getClazz());

            LOG.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", session.getId(), handshakeInfo.getUri(),
                    handshakeInfo.getRemoteAddress());

            sessionRegistry().put(session.getId(), WebSocketSessionInfo.builder()
                    .isOpen(session::isOpen)
                    .protocol(handshakeInfo.getSubProtocol())
                    .id(session.getId())
                    .uri(handshakeInfo.getUri())
                    .remoteAddress(handshakeInfo.getRemoteAddress())
                    .close(session::close)
                    .build());

            return session.getHandshakeInfo().getPrincipal()
                    .switchIfEmpty(Mono.just(new Anonymous()))
                    .flatMap(principal -> {
                        final WebSocketSessionContext webSocketSessionContext =
                                this.getWebSocketSessionContext(pathTemplate, session, handshakeInfo, principal);

                        return this.processConnection(resource, session, configEntity, webSocketSessionContext);
                    })
                    .doOnError(throwable -> LOG.error(throwable.getMessage()));
        };
    }

    private WebSocketSessionContext getWebSocketSessionContext(final String pathTemplate, final WebSocketSession session,
                                                               final HandshakeInfo handshakeInfo, final Principal principal) {

        final String uriPath = handshakeInfo.getUri().getPath();

        final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

        final Map<String, String> pathParameters = uriTemplate.match(uriPath);
        final MultiValueMap<String, String> queryParameters = UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build()
                .getQueryParams();
        final HttpHeaders headers = handshakeInfo.getHeaders();

        return WebSocketSessionContext.builder()
                .authentication(principal)
                .pathParameters(pathParameters)
                .queryParameters(queryParameters)
                .headers(headers)
                .messageStream(() -> session.receive()
                        .doOnError(throwable -> LOG.error("WebSocketSession error occurred: {}", throwable.toString()))
                        .doFinally(signalType -> {
                            LOG.info("Closing WebSocketSession {} on signal {}", session.getId(), signalType);
                            sessionRegistry().remove(session.getId());
                            session.close();
                        }))
                .build();
    }

    private Mono<Void> processConnection(final BasicWebSocketResource resource, final WebSocketSession session,
                                         final WebHandlerResourceDescriptor<?> configEntity,
                                         final WebSocketSessionContext webSocketSessionContext) {

        final Publisher<?> socketMessageFlux = this.processMethod(configEntity, resource, webSocketSessionContext);

        final Flux<WebSocketMessage> webSocketMessageFlux = Flux.from(socketMessageFlux)
                .flatMap(value -> String.class.isAssignableFrom(value.getClass())
                        ? Mono.just(TypeUtils.cast(value, String.class))
                        : this.jsonMapper.applyWithFlux(value))
                .map(session::textMessage);

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
                throw new RuntimeException("Unable to invoke method `@Outbound " + method.getName() + "()` with request parameters" + e.getMessage());
            }
        }).orElseGet(Flux::never);

        if (!fullMethod) {
            inbound.ifPresent(method -> {
                final Object[] parameters = this.processMethodParameters(method, webSocketSessionContext);

                try {
                    method.invoke(resource, parameters);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to invoke method `@Inbound " + method.getName() + "()` with request parameters " +
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

        return Stream.of(method.getParameters()).map(parameter -> {
            final Class<?> parameterType = parameter.getType();

            return this.socketAnnotationEvaluatorFactory.getEvaluator(parameter.getAnnotations())
                    .map(evaluator -> evaluator.evaluate(webSocketSessionContext, parameterType, methodName, parameter.getAnnotation(evaluator.getAnnotationType())))
                    .orElse(null);
        }).toArray();
    }

    private static class WebHandlerResourceDescriptor<T extends BasicWebSocketResource> {
        private Method outboundMethod;
        private Method inboundMethod;

        private final Class<T> clazz;

        public WebHandlerResourceDescriptor(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Method getOutboundMethod() {
            return outboundMethod;
        }

        public void setOutboundMethod(Method outboundMethod) {
            this.outboundMethod = outboundMethod;
        }

        public Method getInboundMethod() {
            return inboundMethod;
        }

        public void setInboundMethod(Method inboundMethod) {
            this.inboundMethod = inboundMethod;
        }

        public Class<T> getClazz() {
            return clazz;
        }
    }

}
