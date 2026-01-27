package io.github.elpis.reactive.websockets.config.context;

import io.github.elpis.reactive.websockets.context.resolver.ReactiveWebSocketMethodParameterResolver;
import io.github.elpis.reactive.websockets.exception.WebSocketProcessingException;
import io.github.elpis.reactive.websockets.handler.validation.ReactiveJSR303ValidationHandler;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.util.TypeUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Resolver configurations for Spring's native annotations
 *
 * @author Phillip J. Fry
 * @see ReactiveWebSocketMethodParameterResolver
 * @since 1.0.0
 */
@Configuration
public class ReactiveWebSocketParameterResolverConfiguration {
  private static final Logger log =
      LoggerFactory.getLogger(ReactiveWebSocketParameterResolverConfiguration.class);

  /**
   * Resolver for Spring's native @AuthenticationPrincipal annotation Resolves the principal by
   * expression from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.security.core.annotation.AuthenticationPrincipal
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver authenticationPrincipalParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final AuthenticationPrincipal annotation =
            parameter.getParameterAnnotation(AuthenticationPrincipal.class);
        Assert.state(
            annotation != null,
            "@AuthenticationPrincipal must not be null. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        return context.getPrincipal(
            annotation.expression(), annotation.errorOnInvalidType(), parameter.getParameterType());
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestBody annotation Resolves the Publisher of either
   * WebSocketMessage or deserialized objects from the message publisher stream
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestBody
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver requestBodyParameterResolver(
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final ObjectProvider<ReactiveJSR303ValidationHandler> validationHandlerProvider) {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
      }

      @Override
      @SuppressWarnings({"ReactiveStreamsUnusedPublisher"})
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final SessionStreams streams =
            sessionRegistry
                .getSession(context.pathTemplate(), context.sessionId())
                .orElseThrow(
                    () ->
                        new WebSocketProcessingException(
                            "Cannot resolve parameter @RequestBody %s: Cannot find session with id %s"
                                .formatted(
                                    parameter.getParameter().getName(), context.sessionId())));

        final Flux<WebSocketMessage> messages = streams.inboundFlux();
        final Class<?> parameterType = parameter.getParameterType();
        final ResolvableType genericType =
            ResolvableType.forMethodParameter(parameter).getGeneric(0);
        final boolean isFlux = Flux.class.isAssignableFrom(parameterType);

        final Class<?> genericClass = genericType.resolve();
        Assert.notNull(
            genericClass,
            () ->
                "@RequestBody generic type cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        if (WebSocketMessage.class.isAssignableFrom(genericClass)) {
          return isFlux ? messages : messages.next();
        }

        final boolean isValidationAvailable = validationHandlerProvider.getIfAvailable() != null;
        final Optional<Valid> validationAnnotation =
            Optional.ofNullable(parameter.getParameterAnnotation(Valid.class));
        if (validationAnnotation.isPresent() && !isValidationAvailable) {
          if (log.isDebugEnabled()) {
            log.debug(
                "JSR-303 validation requested for @RequestBody parameter `{}`, but no JSR-303 ValidationHandler is configured.",
                parameter.getParameter().getName());
          }
        }

        if (isFlux) {
          return messages
              .map(WebSocketMessage::getPayloadAsText)
              .map(text -> jsonMapper.deserialize(text, genericClass))
              .flatMap(
                  converted ->
                      this.tryValidate(converted, validationAnnotation, isValidationAvailable));
        } else {
          return messages
              .next()
              .map(WebSocketMessage::getPayloadAsText)
              .map(text -> jsonMapper.deserialize(text, genericClass))
              .flatMap(
                  converted ->
                      this.tryValidate(converted, validationAnnotation, isValidationAvailable));
        }
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final boolean isFlux = Flux.class.isAssignableFrom(parameter.getParameterType());
        final boolean isMono = Mono.class.isAssignableFrom(parameter.getParameterType());
        Assert.state(
            isFlux || isMono,
            () ->
                "@RequestBody must be used with Flux or Mono type. Found: %s"
                    .formatted(parameter.getParameterType()));

        final Class<?> genericClass =
            ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        Assert.state(
            genericClass != null,
            () ->
                "@RequestBody Flux/Mono must have a generic type parameter. Found raw type: %s"
                    .formatted(parameter.getParameterType()));
      }

      private Mono<?> tryValidate(
          final Object converted,
          final Optional<Valid> validationAnnotation,
          final boolean isValidationAvailable) {
        if (validationAnnotation.isPresent() && isValidationAvailable) {
          return Objects.requireNonNull(validationHandlerProvider.getIfAvailable())
              .validate(converted);
        }

        return Mono.just(converted);
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestHeader annotation for HttpHeaders. Resolves the
   * HttpHeaders from the WebSocketSessionContext.
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestHeader
   * @see org.springframework.http.HttpHeaders
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver httpHeadersParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestHeader.class)
            && HttpHeaders.class.equals(
                parameter.getParameterType()); // Use equals, not isAssignableFrom
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        return context.headers();
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestHeader annotation for MultiValueMap. Resolves the headers
   * as MultiValueMap<String, String> from the WebSocketSessionContext.
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestHeader
   * @see org.springframework.util.MultiValueMap
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver multiMapHeadersParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(RequestHeader.class)) {
          return false;
        }

        final Class<?> paramType = parameter.getParameterType();

        // Support MultiValueMap but NOT HttpHeaders (which extends MultiValueMap)
        return MultiValueMap.class.isAssignableFrom(paramType)
            && !HttpHeaders.class.equals(paramType);
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        return context.headers();
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final ResolvableType[] genericTypes =
            ResolvableType.forMethodParameter(parameter).getGenerics();
        Assert.state(
            genericTypes.length == 2
                && genericTypes[0].resolve() == String.class
                && genericTypes[1].resolve() == String.class,
            () ->
                "MultiValueMap parameter `%s` must be of type MultiValueMap<String, String>"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestHeader annotation Resolves the headers as List<?> from the
   * WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestHeader
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver listHeadersParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestHeader.class)
            && List.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final RequestHeader annotation = parameter.getParameterAnnotation(RequestHeader.class);
        Assert.notNull(
            annotation,
            "@RequestHeader cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final Class<?> elementType =
            ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        final String defaultValue = this.resolveDefaultValue(annotation.defaultValue());
        final List<?> values = context.getHeaders(annotation.value(), defaultValue, elementType);

        if (annotation.required() && values.isEmpty()) {
          throw new WebSocketProcessingException(
              "Header `%s` is marked as required but was not present."
                  .formatted(annotation.value()));
        }

        return values;
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final RequestHeader annotation = parameter.getParameterAnnotation(RequestHeader.class);
        Assert.state(
            annotation != null,
            "@RequestHeader cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@RequestHeader value must be specified for parameter `%s`"
                    .formatted(parameter.getParameterName()));

        final ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        Assert.state(
            resolvableType.hasGenerics(),
            () ->
                "Parameter `%s` must be parameterized"
                    .formatted(parameter.getParameter().getName()));
        final Class<?> elementType = resolvableType.getGeneric(0).resolve();
        Assert.state(
            elementType != null,
            () ->
                "Unsupported generic type for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestHeader annotation Resolves the header as provided type
   * from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver headerParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestHeader.class)
            && !List.class.isAssignableFrom(parameter.getParameterType())
            && !HttpHeaders.class.isAssignableFrom(parameter.getParameterType())
            && !MultiValueMap.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final RequestHeader annotation = parameter.getParameterAnnotation(RequestHeader.class);
        Assert.notNull(
            annotation,
            "@RequestHeader cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final String defaultValue = this.resolveDefaultValue(annotation.defaultValue());
        final Optional<Object> value =
            context.getHeader(
                annotation.value(), defaultValue, (Class<Object>) parameter.getParameterType());
        if (annotation.required()) {
          return value.orElseThrow(
              () ->
                  new WebSocketProcessingException(
                      "Header `%s` is marked as required but was not present."
                          .formatted(annotation.value())));
        }

        return value.orElseGet(
            () -> TypeUtils.getDefaultValueForType(parameter.getParameterType()));
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final RequestHeader annotation = parameter.getParameterAnnotation(RequestHeader.class);
        Assert.state(
            annotation != null,
            "@RequestHeader cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@RequestHeader value must be specified for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @PathVariable annotation Resolves the path variable as provided
   * type from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.PathVariable
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver pathVariableParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathVariable.class);
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final PathVariable annotation = parameter.getParameterAnnotation(PathVariable.class);
        Assert.notNull(
            annotation,
            "@PathVariable cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.isTrue(
            !annotation.value().isBlank(),
            () ->
                "@PathVariable value must be specified for parameter `%s`. This resolver seems to be misused. Do not call resolver's methods directly if not needed."
                    .formatted(parameter.getParameter().getName()));

        final Optional<Object> value =
            context.getPathVariable(
                annotation.value(), (Class<Object>) parameter.getParameterType());
        if (annotation.required()) {
          return value.orElseThrow(
              () ->
                  new WebSocketProcessingException(
                      "Parameter `%s` is marked as required but was not present."
                          .formatted(annotation.value())));
        }

        return value.orElseGet(
            () -> TypeUtils.getDefaultValueForType(parameter.getParameterType()));
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestParam annotation Resolves the path variable as List<?>
   * from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestParam
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver listRequestParamParameterResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class)
            && List.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
        Assert.notNull(
            annotation,
            "@RequestParam cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.isTrue(
            !annotation.value().isBlank(),
            () ->
                "@RequestParam value must be specified for parameter `%s`. This resolver seems to be misused. Do not call resolver's methods directly if not needed."
                    .formatted(parameter.getParameter().getName()));

        final Class<?> elementType =
            ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        final String defaultValue = this.resolveDefaultValue(annotation.defaultValue());
        final List<?> values =
            context.getQueryParams(annotation.value(), defaultValue, elementType);
        if (annotation.required() && values.isEmpty()) {
          throw new WebSocketProcessingException(
              "Parameter `%s` is marked as required but was not present."
                  .formatted(annotation.value()));
        }

        return values;
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
        Assert.state(
            annotation != null,
            "@RequestParam cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@RequestParam value must be specified for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));

        final ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        Assert.state(
            resolvableType.hasGenerics(),
            () ->
                "Parameter `%s` must be parameterized"
                    .formatted(parameter.getParameter().getName()));
        final Class<?> elementType = resolvableType.getGeneric(0).resolve();
        Assert.state(
            elementType != null,
            () ->
                "Unsupported generic type for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @RequestParam annotation Resolves the path variable as provided
   * type from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.RequestParam
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  @SuppressWarnings("unchecked")
  public ReactiveWebSocketMethodParameterResolver requestParamResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class)
            && !List.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
        Assert.state(
            annotation != null,
            "@RequestParam cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final String defaultValue = this.resolveDefaultValue(annotation.defaultValue());
        final Optional<Object> value =
            (Optional<Object>)
                context.getQueryParam(
                    annotation.value(), defaultValue, parameter.getParameterType());
        if (annotation.required()) {
          return value.orElseThrow(
              () ->
                  new WebSocketProcessingException(
                      "Parameter `%s` is marked as required but was not present."
                          .formatted(annotation.value())));
        }

        return value.orElseGet(
            () -> TypeUtils.getDefaultValueForType(parameter.getParameterType()));
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
        Assert.state(
            annotation != null,
            "@RequestParam cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@RequestParam value must be specified for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @SessionAttribute annotation Resolves the ReactiveWebSocketSession
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.SessionAttribute
   * @see io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver sessionAttributeParamResolver(
      final ReactiveWebSocketSessionRegistry registry) {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SessionAttribute.class);
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final SessionAttribute annotation =
            parameter.getParameterAnnotation(SessionAttribute.class);
        Assert.notNull(
            annotation,
            "@SessionAttribute cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final Class<?> parameterType = parameter.getParameterType();
        if (Optional.class.isAssignableFrom(parameterType)) {
          return registry
              .getSession(context.pathTemplate(), context.sessionId())
              .map(SessionStreams::metadata);
        }

        final Optional<ReactiveWebSocketSession> session =
            registry
                .getSession(context.pathTemplate(), context.sessionId())
                .map(SessionStreams::metadata);
        if (annotation.required()) {
          return session.orElseThrow(
              () ->
                  new WebSocketProcessingException(
                      "Cannot find session with id %s".formatted(context.sessionId())));
        }

        return session.orElse(null);
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final Class<?> parameterType = parameter.getParameterType();

        if (Optional.class.isAssignableFrom(parameterType)) {
          final ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
          Assert.state(
              resolvableType.hasGenerics(),
              () ->
                  "Optional @SessionAttribute parameter `%s` must be parameterized"
                      .formatted(parameter.getParameter().getName()));

          final ResolvableType genericType = resolvableType.getGeneric(0);
          Assert.state(
              genericType.getRawClass() != null && !genericType.hasUnresolvableGenerics(),
              () ->
                  "Unsupported generic type for @SessionAttribute parameter `%s`: must be a concrete class"
                      .formatted(parameter.getParameter().getName()));

          final Class<?> elementType = genericType.resolve();
          Assert.state(
              elementType != null,
              () ->
                  "Unsupported generic type for @SessionAttribute parameter `%s`"
                      .formatted(parameter.getParameter().getName()));
          Assert.state(
              ReactiveWebSocketSession.class.isAssignableFrom(elementType),
              () ->
                  "Optional @SessionAttribute parameter `%s` must be Optional<ReactiveWebSocketSession>. Found Optional<%s>"
                      .formatted(parameter.getParameter().getName(), elementType.getName()));
        } else {
          Assert.state(
              ReactiveWebSocketSession.class.isAssignableFrom(parameterType),
              () ->
                  "Only 'ReactiveWebSocketSession' or 'Optional<ReactiveWebSocketSession>' "
                      + "are supported for @SessionAttribute. Found '%s'"
                          .formatted(parameterType.getName()));
        }
      }
    };
  }

  /**
   * Resolver for Spring's native @CookieValue annotation Resolves the first cookie value as
   * provided type from the WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.CookieValue
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver cookieValueResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CookieValue.class)
            && !List.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final CookieValue annotation = parameter.getParameterAnnotation(CookieValue.class);
        Assert.notNull(
            annotation,
            "@CookieValue cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final String cookieName = annotation.value();
        final String defaultValue = this.resolveDefaultValue(annotation.defaultValue());
        final Optional<Object> value =
            context.getCookie(cookieName, (Class<Object>) parameter.getParameterType());
        if (annotation.required()) {
          return value.orElseThrow(
              () ->
                  new WebSocketProcessingException(
                      "Cookie `%s` is marked as required but was not present."
                          .formatted(cookieName)));
        }

        return value.orElseGet(
            () ->
                defaultValue != null
                    ? TypeUtils.convert(defaultValue, parameter.getParameterType())
                    : TypeUtils.getDefaultValueForType(parameter.getParameterType()));
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final CookieValue annotation = parameter.getParameterAnnotation(CookieValue.class);
        Assert.state(
            annotation != null,
            "@CookieValue cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@CookieValue value must be specified for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
      }
    };
  }

  /**
   * Resolver for Spring's native @CookieValue annotation Resolves the Cookies list from the
   * WebSocketSessionContext
   *
   * @author Phillip J. Fry
   * @see org.springframework.web.bind.annotation.CookieValue
   * @see ReactiveWebSocketMethodParameterResolver
   * @since 1.0.0
   */
  @Bean
  public ReactiveWebSocketMethodParameterResolver listCookieValueResolver() {
    return new ReactiveWebSocketMethodParameterResolver() {
      @Override
      public boolean supports(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CookieValue.class)
            && List.class.isAssignableFrom(parameter.getParameterType());
      }

      @Override
      public Object resolve(
          final MethodParameter parameter, final WebSocketSessionContext context) {
        final CookieValue annotation = parameter.getParameterAnnotation(CookieValue.class);
        Assert.notNull(
            annotation,
            "@CookieValue cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");

        final Class<?> elementType =
            ResolvableType.forMethodParameter(parameter).getGeneric(0).resolve();
        final List<HttpCookie> values = context.getCookies(annotation.value(), elementType);
        if (annotation.required() && values != null && values.isEmpty()) {
          throw new WebSocketProcessingException(
              "Cookie `%s` is marked as required but was not present."
                  .formatted(annotation.value()));
        }

        return values;
      }

      @Override
      public void preRegister(final MethodParameter parameter) {
        final CookieValue annotation = parameter.getParameterAnnotation(CookieValue.class);
        Assert.state(
            annotation != null,
            "@CookieValue cannot be null here. This resolver seems to be misused. Do not call resolver's methods directly if not needed.");
        Assert.state(
            !annotation.value().isBlank(),
            () ->
                "@CookieValue value must be specified for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));

        final ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        Assert.state(
            resolvableType.hasGenerics(),
            () ->
                "Parameter `%s` must be parameterized"
                    .formatted(parameter.getParameter().getName()));
        final Class<?> elementType = resolvableType.getGeneric(0).resolve();
        Assert.state(
            elementType != null,
            () ->
                "Unsupported generic type for parameter `%s`"
                    .formatted(parameter.getParameter().getName()));
        Assert.state(
            HttpCookie.class.isAssignableFrom(elementType),
            () ->
                "Only List<HttpCookie> is supported for @CookieValue parameter `%s`. Found List<%s>"
                    .formatted(parameter.getParameter().getName(), elementType.getName()));
      }
    };
  }
}
