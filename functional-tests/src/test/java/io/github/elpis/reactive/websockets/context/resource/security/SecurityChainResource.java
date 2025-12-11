package io.github.elpis.reactive.websockets.context.resource.security;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.security.Principal;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import reactor.core.publisher.Flux;

@MessageEndpoint("/auth/security")
public class SecurityChainResource {

  @OnMessage(value = "/withExtractedAuthentication", mode = Mode.BROADCAST)
  public Publisher<?> withExtractedAuthentication(
      @AuthenticationPrincipal final WebSocketPrincipal<String> authentication) {
    return Flux.just(authentication.getAuthentication());
  }

  @OnMessage(value = "/falseAuthenticationInstance", mode = Mode.BROADCAST)
  public Publisher<?> withExtractedAuthentication(
      @AuthenticationPrincipal final Void authentication) {
    return Flux.just(authentication == null);
  }

  @OnMessage(value = "/anonymous", mode = Mode.BROADCAST)
  public Publisher<?> anonymous(@AuthenticationPrincipal final Principal principal) {
    return Flux.just(Map.of("anonymous", principal instanceof Anonymous));
  }

  @OnMessage(value = "/principal", mode = Mode.BROADCAST)
  public Publisher<?> principal(@AuthenticationPrincipal final Principal principal) {
    return Flux.just(principal)
        .filter(
            principalInstance ->
                !Authentication.class.isAssignableFrom(principalInstance.getClass()))
        .map(Principal::getName);
  }
}
