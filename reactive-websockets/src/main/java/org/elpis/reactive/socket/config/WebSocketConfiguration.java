package org.elpis.reactive.socket.config;

import org.elpis.reactive.socket.exceptions.ValidationException;
import org.elpis.reactive.socket.mappers.JsonMapper;
import org.elpis.reactive.socket.security.principal.Anonymous;
import org.elpis.reactive.socket.security.principal.WebSocketPrincipal;
import org.elpis.reactive.socket.utils.TerminateBean;
import org.elpis.reactive.socket.utils.TypeUtils;
import org.elpis.reactive.socket.web.BasicWebSocketResource;
import org.elpis.reactive.socket.web.annotations.controller.Inbound;
import org.elpis.reactive.socket.web.annotations.controller.Outbound;
import org.elpis.reactive.socket.web.annotations.controller.SocketResource;
import org.elpis.reactive.socket.web.annotations.request.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ValueConstants;
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
import java.lang.reflect.Parameter;
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

    @Bean
    public HandlerMapping handlerMapping(final List<? extends BasicWebSocketResource> webSocketResources) {
        final Map<String, WebSocketHandler> webSocketHandlers = new HashMap<>();

        webSocketResources.forEach(resource -> {
            final Class<? extends BasicWebSocketResource> clazz = resource.getClass();

            Stream.of(clazz.getAnnotations())
                    .findFirst()
                    .map(annotation -> TypeUtils.cast(annotation, SocketResource.class))
                    .ifPresent(socketResource -> this.registerMappings(socketResource, webSocketHandlers, resource, clazz));
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
                                  final BasicWebSocketResource resource,
                                  final Class<? extends BasicWebSocketResource> clazz) {

        Stream.of(clazz.getDeclaredMethods())
                .forEach(method -> {
                    if (method.isAnnotationPresent(Outbound.class)) {
                        this.configureEmitters(socketResource, method);
                    }

                    if (method.isAnnotationPresent(Inbound.class)) {
                        this.configureListeners(socketResource, method);
                    }
                });

        this.descriptorRegistry.forEach((pathTemplate, configEntity) ->
                webSocketHandlers.put(pathTemplate, this.handle(pathTemplate, configEntity, resource)));
    }

    private void configureListeners(final SocketResource socketResource,
                                    final Method method) {

        final String inboundPathTemplate = socketResource.value() + method.getAnnotation(Inbound.class).value();

        final WebHandlerResourceDescriptor webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(inboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor());

        if (nonNull(webHandlerResourceDescriptor.getInboundMethod())) {
            throw new ValidationException(String.format("Cannot register method `@Inbound %s()` on `%s` since " +
                    "`@Inbound %s()` was already registered on provided path", method.getName(), inboundPathTemplate,
                    webHandlerResourceDescriptor.getInboundMethod().getName()));
        }

        webHandlerResourceDescriptor.setInboundMethod(method);

        this.descriptorRegistry.put(inboundPathTemplate, webHandlerResourceDescriptor);
    }

    private void configureEmitters(final SocketResource socketResource,
                                   final Method method) {

        if (!Publisher.class.isAssignableFrom(method.getReturnType())) {
            throw new ValidationException(String.format("Cannot register method `@Outbound %s()`. " +
                    "Reason: method should return a Publisher instance", method.getName()));
        }

        final String outboundPathTemplate = socketResource.value() + method.getAnnotation(Outbound.class).value();

        final WebHandlerResourceDescriptor webHandlerResourceDescriptor = Optional.ofNullable(this.descriptorRegistry.get(outboundPathTemplate))
                .orElse(new WebHandlerResourceDescriptor());

        if (nonNull(webHandlerResourceDescriptor.getOutboundMethod())) {
            throw new ValidationException(String.format("Cannot register method `@Outbound %s()` on `%s` since " +
                    "`@Outbound %s()` was already registered on provided path", method.getName(), outboundPathTemplate,
                    webHandlerResourceDescriptor.getOutboundMethod().getName()));
        }

        webHandlerResourceDescriptor.setOutboundMethod(method);

        this.descriptorRegistry.put(outboundPathTemplate, webHandlerResourceDescriptor);
    }

    private WebSocketHandler handle(final String pathTemplate, final WebHandlerResourceDescriptor configEntity,
                                    final BasicWebSocketResource resource) {

        return session -> {
            final HandshakeInfo handshakeInfo = session.getHandshakeInfo();

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
                                                               final T principal, final WebHandlerResourceDescriptor configEntity,
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
                                                             final WebHandlerResourceDescriptor configEntity,
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
                throw new RuntimeException("Unable to invoke method `@Outbound " + method.getName() + "()` with request parameters", e);
            }
        }).orElseGet(Flux::never);

        if (!fullMethod) {
            inbound.ifPresent(method -> {
                final Object[] parameters = processMethodParameters(session, pathParameters, queryParameters,
                        headers, principal, method);

                try {
                    method.invoke(resource, parameters);
                } catch (Exception e) {
                    throw new RuntimeException( "Unable to invoke method `@Inbound " + method.getName() + "()` with request parameters " +
                            "and message publisher instance", e);
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
            if (parameter.isAnnotationPresent(SocketPathVariable.class)) {
                return this.getPathVariable(pathParameters, parameter, methodName);
            } else if (parameter.isAnnotationPresent(SocketQueryParam.class)) {
                return this.getRequestParameter(queryParameters, parameter, methodName);
            } else if (parameter.isAnnotationPresent(SocketHeader.class)) {
                return this.getHeader(headers, parameter, methodName);
            } else if (parameter.isAnnotationPresent(SocketAuthentication.class)) {
                return this.getSocketAuthentication(parameter, methodName, principal);
            } else if (isInbound && parameter.isAnnotationPresent(SocketMessageBody.class)) {
                return this.getWebSocketMessageFlux(session, parameter, methodName);
            }

            return null;
        }).toArray();
    }

    private <T extends Principal> Object getSocketAuthentication(final Parameter parameter,
                                                                 final String methodName,
                                                                 final T principal) {

        final Class<?> parameterType = parameter.getType();
        final Class<?> principalType = principal.getClass();

        if (Anonymous.class.isAssignableFrom(principalType)) {
            return null;
        } else if (WebSocketPrincipal.class.isAssignableFrom(principalType)) {
            final WebSocketPrincipal<?> webSocketPrincipal = TypeUtils.cast(principal, WebSocketPrincipal.class);

            if (!parameterType.isAssignableFrom(webSocketPrincipal.getAuthentication().getClass())) {
                throw new ValidationException(String.format("Unable register method `%s()`. Requested @SocketAuthentication type: %s, found: %s",
                        methodName, parameterType.getName(), webSocketPrincipal.getAuthentication().getClass().getName()));
            }

            return webSocketPrincipal.getAuthentication();
        } else if (Authentication.class.isAssignableFrom(principalType)) {
            final Authentication authentication = TypeUtils.cast(principal, Authentication.class);

            if (Authentication.class.isAssignableFrom(parameterType)) {
                return authentication;
            } else {
                return authentication.getDetails();
            }
        } else {
            return principal;
        }

    }

    private Flux<WebSocketMessage> getWebSocketMessageFlux(final WebSocketSession session, final Parameter parameter, final String methodName) {
        if (!Flux.class.isAssignableFrom(parameter.getType())) {
            throw new ValidationException(String.format("Unable register outbound method `@Inbound  %s()` since " +
                    "it should accept Flux<WebSocketMessage> instance, but `%s` was found instead", methodName, parameter.getType()));
        }

        return session.receive()
                .doOnError(throwable -> LOG.error("WebSocketSession error occurred: {}", throwable.toString()))
                .doFinally(signalType -> {
                    LOG.info("Closing WebSocketSession {} on signal {}", session.getId(), signalType);
                    this.sessionPreDestroyRegistry.remove(session.getId());
                    session.close();
                });
    }

    private Object getHeader(final HttpHeaders headers, final Parameter parameter, final String methodName) {
        final SocketHeader requestHeader = parameter.getAnnotation(SocketHeader.class);

        final Class<?> parameterType = parameter.getType();

        final Optional<String> defaultValue = Optional.of(requestHeader.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && requestHeader.required();

        final Optional<List<String>> values = Optional.ofNullable(headers.get(requestHeader.value()))
                .filter(l -> !l.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ValidationException(String.format("Request header `@SocketHeader %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", requestHeader.value(), methodName));
        }

        return values.flatMap(s -> List.class.isAssignableFrom(parameterType) ? Optional.of(s) : s.stream().findFirst())
                .orElseGet(() -> defaultValue.map(v -> (Object) TypeUtils.convert(v, parameterType))
                        .orElse(TypeUtils.getDefaultValueForType(parameter.getType())));
    }

    private Object getRequestParameter(final MultiValueMap<String, String> queryParameters,
                                       final Parameter parameter,
                                       final String methodName) {

        final SocketQueryParam requestParam = parameter.getAnnotation(SocketQueryParam.class);

        final Class<?> parameterType = parameter.getType();

        final Optional<String> defaultValue = Optional.of(requestParam.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && requestParam.required();

        final Optional<List<String>> values = Optional.ofNullable(queryParameters.get(requestParam.value()))
                .filter(l -> !l.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ValidationException(String.format("Request parameter `@SocketQueryParam %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", requestParam.value(), methodName));
        }

        return values.flatMap(l -> List.class.isAssignableFrom(parameterType)
                ? Optional.of(l)
                : l.stream().findFirst().map(v -> (Object) TypeUtils.convert(v, parameterType)))
                .orElseGet(() -> defaultValue.map(v -> (Object) TypeUtils.convert(v, parameterType))
                        .orElse(TypeUtils.getDefaultValueForType(parameter.getType())));
    }

    private Object getPathVariable(final Map<String, String> pathParameters, final Parameter parameter, final String methodName) {
        final SocketPathVariable pathVariable = parameter.getAnnotation(SocketPathVariable.class);

        final Optional<String> value = Optional.ofNullable(pathParameters.get(pathVariable.value()))
                .filter(s -> !s.isEmpty());

        if (pathVariable.required() && value.isEmpty()) {
            throw new ValidationException(String.format("Path parameter `@SocketPathVariable %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", pathVariable.value(), methodName));
        }

        return value.map(v -> (Object) TypeUtils.convert(v, parameter.getType()))
                .orElse(TypeUtils.getDefaultValueForType(parameter.getType()));
    }

    private static class WebHandlerResourceDescriptor {
        private Method outboundMethod;
        private Method inboundMethod;

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
    }

}
