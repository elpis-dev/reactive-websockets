package org.elpis.reactive.websockets.context.resource.security;

import org.elpis.reactive.websockets.config.Mode;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.Map;

@SocketController("/auth/security")
public class SecurityChainResource {

    @SocketMapping(value = "/withExtractedAuthentication", mode = Mode.SHARED)
    public Publisher<?> withExtractedAuthentication(@AuthenticationPrincipal final WebSocketPrincipal<String> authentication) {
        return Flux.just(authentication.getAuthentication());
    }

    @SocketMapping(value = "/falseAuthenticationInstance", mode = Mode.SHARED)
    public Publisher<?> withExtractedAuthentication(@AuthenticationPrincipal final Void authentication) {
        return Flux.just(authentication == null);
    }

    @SocketMapping(value = "/anonymous", mode = Mode.SHARED)
    public Publisher<?> anonymous(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(Map.of("anonymous", principal instanceof Anonymous));
    }

    @SocketMapping(value = "/principal", mode = Mode.SHARED)
    public Publisher<?> principal(@AuthenticationPrincipal final Principal principal) {
        return Flux.just(principal)
                .filter(principalInstance -> !Authentication.class.isAssignableFrom(principalInstance.getClass()))
                .map(Principal::getName);
    }


}
