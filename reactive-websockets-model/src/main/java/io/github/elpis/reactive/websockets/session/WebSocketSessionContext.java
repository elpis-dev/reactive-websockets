package io.github.elpis.reactive.websockets.session;

import io.github.elpis.reactive.websockets.Constants;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.util.TypeUtils;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public record WebSocketSessionContext(
    Map<String, String> pathParameters,
    MultiValueMap<String, String> queryParameters,
    HttpHeaders headers,
    Principal authentication,
    MultiValueMap<String, HttpCookie> cookies,
    String sessionId,
    String remoteAddress,
    String pathTemplate) {

  public WebSocketSessionContext {
    pathParameters = pathParameters != null ? Map.copyOf(pathParameters) : Map.of();
    queryParameters =
        queryParameters != null
            ? new LinkedMultiValueMap<>(queryParameters)
            : new LinkedMultiValueMap<>();
    headers = headers != null ? HttpHeaders.readOnlyHttpHeaders(headers) : new HttpHeaders();
    authentication = authentication != null ? authentication : new Anonymous();
    cookies = cookies != null ? new LinkedMultiValueMap<>(cookies) : new LinkedMultiValueMap<>();
  }

  public <T> Optional<T> getCookie(String cookieName, Class<T> type) {
    return Optional.ofNullable(cookies.getFirst(cookieName))
        .map(HttpCookie::getValue)
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<HttpCookie> getCookies(String cookieName, Class<T> type) {
    return Optional.ofNullable(cookies.get(cookieName)).orElse(List.of());
  }

  public <T> Optional<T> getPathVariable(String header, Class<T> type) {
    return Optional.ofNullable(pathParameters.get(header))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> Optional<T> getQueryParam(String queryParam, String defaultValue, Class<T> type) {
    return Optional.ofNullable(queryParameters.get(queryParam))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<T> getQueryParams(String queryParam, String defaultValue, Class<T> type) {
    return Optional.ofNullable(queryParameters.get(queryParam))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  public <T> Optional<T> getHeader(String header, String defaultValue, Class<T> type) {
    return Optional.ofNullable(headers.get(header))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<T> getHeaders(String header, String defaultValue, Class<T> type) {
    return Optional.ofNullable(headers.get(header))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  public <T> T getPrincipal(String expression, boolean errorOnInvalidType, Class<T> type) {
    Object principal =
        StringUtils.hasLength(expression) ? parseExpression(expression, type) : authentication;

    if (principal != null && !type.isAssignableFrom(principal.getClass())) {
      if (errorOnInvalidType) {
        throw new ClassCastException(principal + " is not assignable to " + type);
      } else {
        return null;
      }
    }

    return (T) principal;
  }

  private <T> T parseExpression(String expression, Class<T> type) {
    return Constants.parseExpression(
        expression,
        type,
        builder -> builder.withRootObject(authentication).withAssignmentDisabled(),
        context -> context.setVariable("this", authentication));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Map<String, String> pathParameters = new HashMap<>();
    private MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
    private HttpHeaders headers = new HttpHeaders();
    private Principal authentication = new Anonymous();
    private MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
    private String sessionId;
    private String remoteAddress;
    private String pathTemplate;

    public Builder pathParameters(Map<String, String> pathParameters) {
      this.pathParameters = pathParameters;
      return this;
    }

    public Builder queryParameters(MultiValueMap<String, String> queryParameters) {
      this.queryParameters = queryParameters;
      return this;
    }

    public Builder headers(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    public Builder authentication(Principal authentication) {
      this.authentication = authentication;
      return this;
    }

    public Builder cookies(MultiValueMap<String, HttpCookie> cookies) {
      this.cookies = cookies;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder pathTemplate(String pathTemplate) {
      this.pathTemplate = pathTemplate;
      return this;
    }

    public Builder remoteAddress(String remoteAddress) {
      this.remoteAddress = remoteAddress;
      return this;
    }

    public WebSocketSessionContext build() {
      return new WebSocketSessionContext(
          pathParameters,
          queryParameters,
          headers,
          authentication,
          cookies,
          sessionId,
          remoteAddress,
          pathTemplate);
    }
  }
}
