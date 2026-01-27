package io.github.elpis.reactive.websockets.context.controller;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session")
public class SessionController {
  private final List<String> history =
      List.of(
          "Maintenance Notice: The system will be down for maintenance at midnight.",
          "Update: New features have been added to your dashboard.");

  private final ReactiveWebSocketTemplate template;
  private final ReactiveWebSocketSessionRegistry sessionRegistry;

  public SessionController(
      ReactiveWebSocketTemplate template, ReactiveWebSocketSessionRegistry sessionRegistry) {

    this.template = template;
    this.sessionRegistry = sessionRegistry;
  }

  @PostMapping("/many")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<String> sendBroadcast() {
    final Set<String> sessionIds =
        sessionRegistry.getAllSessions("/template/session/multi/single").stream()
            .map(sessionStreams -> sessionStreams.metadata().getSessionId())
            .collect(Collectors.toSet());

    return template
        .sendToSessions(
            "/template/session/multi/single",
            sessionIds,
            "Please be informed about the following updates.")
        .then(Mono.empty());
  }

  @PostMapping("/many/stream")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<String> sendBroadcastMany() {
    final Set<String> sessionIds =
        sessionRegistry.getAllSessions("/template/session/multi/many").stream()
            .map(sessionStreams -> sessionStreams.metadata().getSessionId())
            .collect(Collectors.toSet());

    return template
        .sendToSessions("/template/session/multi/many", sessionIds, Flux.fromIterable(history))
        .then(Mono.empty());
  }
}
