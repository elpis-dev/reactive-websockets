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

public class WebSocketSessionContext {
  private Map<String, String> pathParameters = new HashMap<>();
  private MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
  private HttpHeaders headers = new HttpHeaders();
  private Principal authentication = new Anonymous();
  private MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();

  private String sessionId;
  private String remoteAddress;
  private String pathTemplate;

  public Map<String, String> getPathParameters() {
    return pathParameters;
  }

  private void setPathParameters(Map<String, String> pathParameters) {
    this.pathParameters = pathParameters;
  }

  public MultiValueMap<String, String> getQueryParameters() {
    return queryParameters;
  }

  private void setQueryParameters(MultiValueMap<String, String> queryParameters) {
    this.queryParameters = queryParameters;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  private void setHeaders(HttpHeaders headers) {
    this.headers = headers;
  }

  public Principal getAuthentication() {
    return authentication;
  }

  private void setAuthentication(Principal authentication) {
    this.authentication = authentication;
  }

  public String getSessionId() {
    return sessionId;
  }

  private void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  private void setRemoteAddress(final String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public void setCookies(final MultiValueMap<String, HttpCookie> cookies) {
    this.cookies = cookies;
  }

  public MultiValueMap<String, HttpCookie> getCookies() {
    return cookies;
  }

  public String getPathTemplate() {
    return pathTemplate;
  }

  public void setPathTemplate(String pathTemplate) {
    this.pathTemplate = pathTemplate;
  }

  public <T> Optional<T> getCookie(final String cookieName, final Class<T> type) {
    return Optional.ofNullable(this.cookies.getFirst(cookieName))
        .map(HttpCookie::getValue)
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<HttpCookie> getCookies(final String cookieName, final Class<T> type) {
    return Optional.ofNullable(this.cookies.get(cookieName)).orElse(List.of());
  }

  public <T> Optional<T> getPathVariable(final String header, final Class<T> type) {
    return Optional.ofNullable(this.pathParameters.get(header))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> Optional<T> getQueryParam(
      final String queryParam, final String defaultValue, final Class<T> type) {
    return Optional.ofNullable(this.queryParameters.get(queryParam))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<T> getQueryParams(
      final String queryParam, final String defaultValue, final Class<T> type) {
    return Optional.ofNullable(this.queryParameters.get(queryParam))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  public <T> Optional<T> getHeader(
      final String header, final String defaultValue, final Class<T> type) {
    return Optional.ofNullable(this.headers.get(header))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  public <T> List<T> getHeaders(
      final String header, final String defaultValue, final Class<T> type) {
    return Optional.ofNullable(this.headers.get(header))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  public <T> T getPrincipal(
      final String expression, final boolean errorOnInvalidType, final Class<T> type) {
    final Object principal =
        StringUtils.hasLength(expression)
            ? this.parseExpression(expression, type)
            : this.getAuthentication();

    if (principal != null && !type.isAssignableFrom(principal.getClass())) {
      if (errorOnInvalidType) {
        throw new ClassCastException(principal + " is not assignable to " + type);
      } else {
        return null;
      }
    }

    return (T) principal;
  }

  private <T> T parseExpression(final String expression, final Class<T> type) {
    return Constants.parseExpression(
        expression,
        type,
        builder -> builder.withRootObject(this.getAuthentication()).withAssignmentDisabled(),
        context -> context.setVariable("this", this.getAuthentication()));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final WebSocketSessionContext context = new WebSocketSessionContext();

    public Builder pathParameters(Map<String, String> pathParameters) {
      this.context.setPathParameters(pathParameters);
      return this;
    }

    public Builder queryParameters(MultiValueMap<String, String> queryParameters) {
      this.context.setQueryParameters(queryParameters);
      return this;
    }

    public Builder headers(HttpHeaders headers) {
      this.context.setHeaders(headers);
      return this;
    }

    public Builder authentication(Principal authentication) {
      this.context.setAuthentication(authentication);
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.context.setSessionId(sessionId);
      return this;
    }

    public Builder pathTemplate(String pathTemplate) {
      this.context.setPathTemplate(pathTemplate);
      return this;
    }

    public Builder remoteAddress(String remoteAddress) {
      this.context.setRemoteAddress(remoteAddress);
      return this;
    }

    public WebSocketSessionContext build() {
      return this.context;
    }
  }
}
