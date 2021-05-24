package org.elpis.reactive.websockets.config;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.mappers.JsonMapper;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.utils.TerminateBean;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.BasicWebSocketResource;
import org.elpis.reactive.websockets.web.annotations.controller.Inbound;
import org.elpis.reactive.websockets.web.annotations.controller.Outbound;
import org.elpis.reactive.websockets.web.annotations.controller.SocketResource;
import org.elpis.reactive.websockets.web.annotations.request.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class WebSocketConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfiguration.class);

    private final JsonMapper jsonMapper = new JsonMapper();

    private final Map<String, Runnable> sessionPreDestroyRegistry = new ConcurrentHashMap<>();
    private final Map<String, WebHandlerResourceDescriptor> descriptorRegistry = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;

    private final SocketApiAnnotationEvaluator<SocketAuthentication, Principal> authenticationAnnotationEvaluator;
    private final SocketApiAnnotationEvaluator<SocketHeader, HttpHeaders> headersAnnotationEvaluator;
    private final SocketApiAnnotationEvaluator<SocketMessageBody, WebSocketSession> messageBodyAnnotationEvaluator;
    private final SocketApiAnnotationEvaluator<SocketPathVariable, Map<String, String>> pathVariableAnnotationEvaluator;
    private final SocketApiAnnotationEvaluator<SocketQueryParam, MultiValueMap<String, String>> queryParamAnnotationEvaluator;

    public WebSocketConfiguration(final ApplicationContext applicationContext,
                                  final SocketApiAnnotationEvaluator<SocketAuthentication, Principal> authenticationAnnotationEvaluator,
                                  final SocketApiAnnotationEvaluator<SocketHeader, HttpHeaders> headersAnnotationEvaluator,
                                  final SocketApiAnnotationEvaluator<SocketMessageBody, WebSocketSession> messageBodyAnnotationEvaluator,
                                  final SocketApiAnnotationEvaluator<SocketPathVariable, Map<String, String>> pathVariableAnnotationEvaluator,
                                  final SocketApiAnnotationEvaluator<SocketQueryParam, MultiValueMap<String, String>> queryParamAnnotationEvaluator) {

        this.applicationContext = applicationContext;
        this.authenticationAnnotationEvaluator = authenticationAnnotationEvaluator;
        this.headersAnnotationEvaluator = headersAnnotationEvaluator;
        this.messageBodyAnnotationEvaluator = messageBodyAnnotationEvaluator;
        this.pathVariableAnnotationEvaluator = pathVariableAnnotationEvaluator;
        this.queryParamAnnotationEvaluator = queryParamAnnotationEvaluator;
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

    @Bean("webSocketsTerminateBean")
    public TerminateBean terminateBean() {
        return TerminateBean.with(() -> {
            LOG.info("Gracefully terminating all socket sessions...");

            this.sessionPreDestroyRegistry.forEach((sessionId, executor) -> {
                LOG.debug("Terminating session: {}", sessionId);

                executor.run();
            });
        });
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

            LOG.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", session.getId(), handshakeInfo.getUri().toString(),
                    handshakeInfo.getRemoteAddress());

            final String uriPath = handshakeInfo.getUri().getPath();

            final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

            final Map<String, String> pathParameters = uriTemplate.match(uriPath);
            final MultiValueMap<String, String> queryParameters = UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build()
                    .getQueryParams();
            final HttpHeaders headers = handshakeInfo.getHeaders();

            this.sessionPreDestroyRegistry.put(session.getId(), session::close);

            return session.getHandshakeInfo().getPrincipal()
                    .switchIfEmpty(Mono.just(new Anonymous()))
                    .flatMap(principal -> this.processConnection(resource, session, principal, configEntity,
                            pathParameters, queryParameters, headers))
                    .doOnError(throwable -> LOG.error(throwable.getMessage()));
        };
    }

    private <T extends Principal> Mono<Void> processConnection(final BasicWebSocketResource resource, final WebSocketSession session,
                                                               final T principal, final WebHandlerResourceDescriptor<?> configEntity,
                                                               final Map<String, String> pathParameters,
                                                               final MultiValueMap<String, String> queryParameters, final HttpHeaders headers) {

        final Publisher<?> socketMessageFlux = this.processMethod(session, configEntity, resource, pathParameters,
                queryParameters, headers, principal);

        final Flux<WebSocketMessage> webSocketMessageFlux = Flux.from(socketMessageFlux)
                .flatMap(value -> String.class.isAssignableFrom(value.getClass())
                        ? Mono.just(TypeUtils.cast(value, String.class))
                        : this.jsonMapper.applyWithFlux(value))
                .map(session::textMessage);

        return session.send(webSocketMessageFlux);
    }

    private <T extends Principal> Publisher<?> processMethod(final WebSocketSession session,
                                                             final WebHandlerResourceDescriptor<?> configEntity,
                                                             final BasicWebSocketResource resource,
                                                             final Map<String, String> pathParameters,
                                                             final MultiValueMap<String, String> queryParameters,
                                                             final HttpHeaders headers,
                                                             final T principal) {

        final Optional<Method> outbound = Optional.ofNullable(configEntity.getOutboundMethod());
        final Optional<Method> inbound = Optional.ofNullable(configEntity.getInboundMethod());

        final boolean fullMethod = outbound
                .map(method -> method.isAnnotationPresent(Inbound.class))
                .orElse(false);

        final Publisher<?> publisher = outbound.map(method -> {
            final Object[] parameters = processMethodParameters(session, pathParameters, queryParameters,
                    headers, principal, method);

            try {
                return TypeUtils.cast(method.invoke(resource, parameters), Publisher.class);
            } catch (Exception e) {
                throw new RuntimeException("Unable to invoke method `@Outbound " + method.getName() + "()` with request parameters" + e.getMessage());
            }
        }).orElseGet(Flux::never);

        if (!fullMethod) {
            inbound.ifPresent(method -> {
                final Object[] parameters = processMethodParameters(session, pathParameters, queryParameters,
                        headers, principal, method);

                try {
                    method.invoke(resource, parameters);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to invoke method `@Inbound " + method.getName() + "()` with request parameters " +
                            "and message publisher instance" + e.getMessage());
                }
            });
        }

        return publisher;
    }

    private <T extends Principal> Object[] processMethodParameters(final WebSocketSession session,
                                                                   final Map<String, String> pathParameters,
                                                                   final MultiValueMap<String, String> queryParameters,
                                                                   final HttpHeaders headers, final T principal,
                                                                   final Method method) {

        final String methodName = method.getName();
        final boolean isInbound = method.isAnnotationPresent(Inbound.class);

        return Stream.of(method.getParameters()).map(parameter -> {
            final Class<?> parameterType = parameter.getType();

            if (parameter.isAnnotationPresent(SocketPathVariable.class)) {
                return this.pathVariableAnnotationEvaluator.evaluate(pathParameters, parameterType, methodName, parameter.getAnnotation(SocketPathVariable.class));
            } else if (parameter.isAnnotationPresent(SocketQueryParam.class)) {
                return this.queryParamAnnotationEvaluator.evaluate(queryParameters, parameterType, methodName, parameter.getAnnotation(SocketQueryParam.class));
            } else if (parameter.isAnnotationPresent(SocketHeader.class)) {
                return this.headersAnnotationEvaluator.evaluate(headers, parameterType, methodName, parameter.getAnnotation(SocketHeader.class));
            } else if (parameter.isAnnotationPresent(SocketAuthentication.class)) {
                return this.authenticationAnnotationEvaluator.evaluate(principal, parameterType, methodName, parameter.getAnnotation(SocketAuthentication.class));
            } else if (isInbound && parameter.isAnnotationPresent(SocketMessageBody.class)) {
                return this.messageBodyAnnotationEvaluator.evaluate(session, parameterType, methodName, parameter.getAnnotation(SocketMessageBody.class));
            }

            return null;
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
