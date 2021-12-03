package org.elpis.socket.web.context.security.model;

public interface SecurityProfiles {
    String PERMIT_ALL = "permitAll";
    String GENERIC = "generic";

    String WEB_FILTER_AUTHORIZATION = "webFilterAuthorization";
    String WEB_FILTER_ATTRIBUTE = "webFilterAttribute";

    String SECURITY_CHAIN = "securityChain";
    String CUSTOM_EXCHANGE_MATCHER = "customExchangeMatcher";
}
