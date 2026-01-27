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

/**
 * Context information about a WebSocket session, including path parameters, query parameters,
 * headers, authentication, cookies, session ID, remote address, and path template.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
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

  /**
   * Retrieves a cookie by its name and converts its value to the specified type.
   *
   * @param cookieName the name of the cookie
   * @param type the desired type of the cookie value
   * @param <T> the type parameter
   * @return an Optional containing the converted cookie value if present, otherwise an empty
   *     Optional
   */
  public <T> Optional<T> getCookie(String cookieName, Class<T> type) {
    return Optional.ofNullable(cookies.getFirst(cookieName))
        .map(HttpCookie::getValue)
        .map(value -> TypeUtils.convert(value, type));
  }

  /**
   * Retrieves all cookies with the specified name.
   *
   * @param cookieName the name of the cookie
   * @param type the desired type of the cookie value
   * @param <T> the type parameter
   * @return a list of cookies with the specified name
   */
  public <T> List<HttpCookie> getCookies(String cookieName, Class<T> type) {
    return Optional.ofNullable(cookies.get(cookieName)).orElse(List.of());
  }

  /**
   * Retrieves a path variable by its name and converts its value to the specified type.
   *
   * @param header the name of the path variable
   * @param type the desired type of the path variable value
   * @param <T> the type parameter
   * @return an Optional containing the converted path variable value if present, otherwise an empty
   *     Optional
   */
  public <T> Optional<T> getPathVariable(String header, Class<T> type) {
    return Optional.ofNullable(pathParameters.get(header))
        .map(value -> TypeUtils.convert(value, type));
  }

  /**
   * Retrieves a query parameter by its name, with a default value if not present, and converts its
   * value to the specified type.
   *
   * @param queryParam the name of the query parameter
   * @param defaultValue the default value to use if the query parameter is not present
   * @param type the desired type of the query parameter value
   * @param <T> the type parameter
   * @return an Optional containing the converted query parameter value if present, otherwise an
   *     Optional containing the default value
   */
  public <T> Optional<T> getQueryParam(String queryParam, String defaultValue, Class<T> type) {
    return Optional.ofNullable(queryParameters.get(queryParam))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  /**
   * Retrieves all query parameters with the specified name, with a default value if not present,
   * and converts their values to the specified type.
   *
   * @param queryParam the name of the query parameter
   * @param defaultValue the default value to use if the query parameter is not present
   * @param type the desired type of the query parameter values
   * @param <T> the type parameter
   * @return a list of converted query parameter values
   */
  public <T> List<T> getQueryParams(String queryParam, String defaultValue, Class<T> type) {
    return Optional.ofNullable(queryParameters.get(queryParam))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a header by its name, with a default value if not present, and converts its value to
   * the specified type.
   *
   * @param header the name of the header
   * @param defaultValue the default value to use if the header is not present
   * @param type the desired type of the header value
   * @param <T> the type parameter
   * @return an Optional containing the converted header value if present, otherwise an Optional
   *     containing the default value
   */
  public <T> Optional<T> getHeader(String header, String defaultValue, Class<T> type) {
    return Optional.ofNullable(headers.get(header))
        .map(h -> h.stream().findFirst())
        .orElse(Optional.ofNullable(defaultValue))
        .map(value -> TypeUtils.convert(value, type));
  }

  /**
   * Retrieves all headers with the specified name, with a default value if not present, and
   * converts their values to the specified type.
   *
   * @param header the name of the header
   * @param defaultValue the default value to use if the header is not present
   * @param type the desired type of the header values
   * @param <T> the type parameter
   * @return a list of converted header values
   */
  public <T> List<T> getHeaders(String header, String defaultValue, Class<T> type) {
    return Optional.ofNullable(headers.get(header))
        .filter(headerList -> !headerList.isEmpty())
        .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
        .stream()
        .filter(Objects::nonNull)
        .map(value -> TypeUtils.convert(value, type))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the principal from the authentication object based on the provided expression and
   * type.
   *
   * @param expression the expression to evaluate against the authentication object
   * @param errorOnInvalidType whether to throw an error if the principal is not of the expected
   *     type
   * @param type the expected type of the principal
   * @param <T> the type parameter
   * @return the principal of the specified type, or null if not found or of invalid type
   * @throws ClassCastException if errorOnInvalidType is true and the principal is not of the
   *     expected type
   */
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
