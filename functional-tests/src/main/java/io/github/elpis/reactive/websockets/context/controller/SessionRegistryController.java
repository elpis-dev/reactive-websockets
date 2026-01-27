package io.github.elpis.reactive.websockets.context.controller;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionMaintenanceService;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sessionRegistry")
public class SessionRegistryController {
  private final ReactiveWebSocketSessionRegistry sessionRegistry;
  private final ReactiveWebSocketSessionMaintenanceService
      reactiveWebSocketSessionMaintenanceService;

  public SessionRegistryController(
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final ReactiveWebSocketSessionMaintenanceService reactiveWebSocketSessionMaintenanceService) {

    this.sessionRegistry = sessionRegistry;
    this.reactiveWebSocketSessionMaintenanceService = reactiveWebSocketSessionMaintenanceService;
  }

  @PostMapping("/count")
  public Mono<Long> getSessionCount(@RequestBody final String path) {
    return Mono.just(sessionRegistry.getSessionCount(path));
  }

  @GetMapping("/totalCount")
  public Mono<Long> getTotalSessionCount() {
    return Mono.just(sessionRegistry.getTotalSessionCount());
  }

  @GetMapping(value = "/paths", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> getActivePaths() {
    return Flux.fromIterable(sessionRegistry.getAllPaths().stream().sorted().toList());
  }

  @DeleteMapping("/shutdown")
  public void doShutdown() {
    reactiveWebSocketSessionMaintenanceService.shutdown();
  }
}
