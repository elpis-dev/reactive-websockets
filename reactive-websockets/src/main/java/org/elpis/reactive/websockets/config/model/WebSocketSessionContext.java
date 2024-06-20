package org.elpis.reactive.websockets.config.model;

import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

public class WebSocketSessionContext {
    private Map<String, String> pathParameters = new HashMap<>();

    private MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();

    private HttpHeaders headers = new HttpHeaders();

    private Principal authentication = new Anonymous();

    private String sessionId;

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public MultiValueMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(MultiValueMap<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public Principal getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Principal authentication) {
        this.authentication = authentication;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public <T> Optional<T> getPathVariable(final String header, final Class<T> type) {
        return Optional.ofNullable(this.pathParameters.get(header))
                .map(value -> TypeUtils.convert(value, type));
    }

    public <T> Optional<T> getQueryParam(final String queryParam, final String defaultValue, final Class<T> type) {
        return Optional.ofNullable(this.queryParameters.get(queryParam))
                .map(h -> h.stream().findFirst())
                .orElse(Optional.ofNullable(defaultValue))
                .map(value -> TypeUtils.convert(value, type));
    }

    public <T> List<T> getQueryParams(final String queryParam, final String defaultValue, final Class<T> type) {
        return Optional.ofNullable(this.headers.get(queryParam))
                .filter(headerList -> !headerList.isEmpty())
                .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
                .stream()
                .filter(Objects::nonNull)
                .map(value -> TypeUtils.convert(value, type))
                .collect(Collectors.toList());
    }

    public <T> Optional<T> getHeader(final String header, final String defaultValue, final Class<T> type) {
        return Optional.ofNullable(this.headers.get(header))
                .map(h -> h.stream().findFirst())
                .orElse(Optional.ofNullable(defaultValue))
                .map(value -> TypeUtils.convert(value, type));
    }

    public <T> List<T> getHeaders(final String header, final String defaultValue, final Class<T> type) {
        return Optional.ofNullable(this.headers.get(header))
                .filter(headerList -> !headerList.isEmpty())
                .orElse(Optional.ofNullable(defaultValue).map(List::of).orElseGet(List::of))
                .stream()
                .filter(Objects::nonNull)
                .map(value -> TypeUtils.convert(value, type))
                .collect(Collectors.toList());
    }

    private Object getPrincipalDetails(final Authentication authentication, final Class<?> parameterType) {
        return Principal.class.isAssignableFrom(parameterType) ? authentication.getPrincipal() : authentication.getDetails();
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

        public WebSocketSessionContext build() {
            return this.context;
        }
    }
}
