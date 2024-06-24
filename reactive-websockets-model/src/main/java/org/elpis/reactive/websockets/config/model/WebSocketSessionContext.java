package org.elpis.reactive.websockets.config.model;

import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

public class WebSocketSessionContext {
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

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
        return Optional.ofNullable(this.queryParameters.get(queryParam))
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

    public <T> T getPrincipal(final String expression, final boolean errorOnInvalidType, final Class<T> type) {
        final Principal principal = StringUtils.hasLength(expression)
                ? this.parseExpression(expression)
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

    private Principal parseExpression(final String expression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(this.getAuthentication());
        context.setVariable("this", this.getAuthentication());
        return SPEL_EXPRESSION_PARSER.parseExpression(expression).getValue(context, Principal.class);
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
