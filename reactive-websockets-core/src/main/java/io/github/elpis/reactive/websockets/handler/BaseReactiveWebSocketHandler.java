package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.config.CloseInitiator;
import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManager;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.ServerSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import io.github.elpis.reactive.websockets.exception.ErrorResponseException;
import io.github.elpis.reactive.websockets.exception.WebSocketProcessingException;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.security.Principal;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Base WebSocket handler providing raw session management.
 *
 * <p>Single Responsibility: Manage WebSocket session lifecycle and stream wiring.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Create WebSocketSessionContext from handshake info
 *   <li>Create SessionStreams and register in Registry
 *   <li>Wire WebSocket receive → inboundSink
 *   <li>Wire outboundSink → mapOutput() → WebSocket send
 *   <li>Fire lifecycle events (connect, close)
 *   <li>Handle ErrorResponseException from outboundSink
 * </ul>
 *
 * <p>This class does NOT handle:
 *
 * <ul>
 *   <li>Flow control (heartbeat, rate limiting, backpressure) - see AdaptiveWebSocketHandler
 *   <li>Type conversion beyond mapOutput() - annotation processor generates this
 *   <li>Exception handling - annotation processor generates this
 * </ul>
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public abstract class BaseReactiveWebSocketHandler implements WebSocketHandler {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final ReactiveWebSocketSessionRegistry sessionRegistry;
  private final JsonMapper jsonMapper;

  private final String pathTemplate;

  private final ReactiveWebSocketEventManager<ClientSessionClosedEvent> closedEventManager;
  private final ReactiveWebSocketEventManager<ServerSessionClosedEvent>
      serverSessionClosedEventManager;
  private final ReactiveWebSocketEventManager<SessionConnectedEvent> sessionConnectedEventManager;

  /**
   * Creates a BaseWebSocketHandler with minimal dependencies.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   * @param jsonMapper JSON mapper for serialization/deserialization
   * @param pathTemplate the WebSocket path template (e.g., "/chat/{room}")
   */
  protected BaseReactiveWebSocketHandler(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final String pathTemplate) {

    this.sessionRegistry = sessionRegistry;
    this.jsonMapper = jsonMapper;
    this.pathTemplate = pathTemplate;
    this.closedEventManager = eventManagerFactory.getEventManager(ClientSessionClosedEvent.class);
    this.serverSessionClosedEventManager =
        eventManagerFactory.getEventManager(ServerSessionClosedEvent.class);
    this.sessionConnectedEventManager =
        eventManagerFactory.getEventManager(SessionConnectedEvent.class);
  }

  /**
   * Process messages using the session streams.
   *
   * <p><b>CRITICAL:</b> This method MUST return Mono&lt;Void&gt; for proper lifecycle management.
   * Never call .subscribe() inside this method!
   *
   * <p>Subclasses override this to implement their message handling logic:
   *
   * <ul>
   *   <li>Get messages from streams.inboundFlux()
   *   <li>Process through user handlers
   *   <li>Send via streams.outboundSink() or ReactiveWebSocketTemplate
   *   <li>Return Mono&lt;Void&gt; from the reactive chain
   * </ul>
   *
   * <p>Example:
   *
   * <pre>{@code
   * protected Mono<Void> processMessages(context, streams) {
   *   return streams.inboundFlux()
   *       .map(this::deserialize)
   *       .transform(messages -> userHandler.handleMessages(messages))
   *       .doOnNext(result -> streams.outboundSink().tryEmitNext(result))
   *       .onErrorResume(e -> {
   *           log.error("Error: {}", e.getMessage());
   *           return Mono.empty();
   *       })
   *       .then();
   * }
   * }</pre>
   *
   * @param context the WebSocket session context
   * @param incomingStream the incoming WebSocketMessage stream
   * @param outboundSink the outbound message sink
   * @return Mono&lt;Void&gt; that completes when processing chain completes
   */
  protected abstract Publisher<?> processMessages(
      final WebSocketSessionContext context,
      final Flux<WebSocketMessage> incomingStream,
      final Sinks.Many<Object> outboundSink);

  @Override
  public Mono<Void> handle(final WebSocketSession session) {
    return Mono.deferContextual(Mono::just)
        .flatMap(contextView -> (Mono<String>) contextView.get("sessionId"))
        .flatMap(
            sessionId -> {
              final HandshakeInfo handshakeInfo = session.getHandshakeInfo();
              if (log.isTraceEnabled()) {
                log.trace(
                    "Establishing WebSocketSession [id={}, uri={}, address={}]",
                    sessionId,
                    handshakeInfo.getUri(),
                    handshakeInfo.getRemoteAddress());
              }

              final ReactiveWebSocketSession reactiveWebSocketSession =
                  ReactiveWebSocketSession.builder()
                      .isOpen(session::isOpen)
                      .sessionId(sessionId)
                      .onClose(
                          (eventSessionId, closeStatus) ->
                              Mono.just(this.getServerClosedEvent(eventSessionId, closeStatus))
                                  .doOnNext(serverSessionClosedEventManager::fire)
                                  .then(Mono.empty()))
                      .build();

              final SessionStreams streams = SessionStreams.create(reactiveWebSocketSession);
              getSessionRegistry().register(this.pathTemplate, sessionId, streams);

              sessionConnectedEventManager.fire(
                  SessionConnectedEvent.builder()
                      .webSocketSessionInfo(reactiveWebSocketSession)
                      .build());

              final Mono<Void> clientClosedSessionListener =
                  session
                      .closeStatus()
                      .doOnNext(
                          closeStatus -> {
                            final SessionCloseInfo sessionCloseInfo =
                                SessionCloseInfo.builder()
                                    .closeStatus(closeStatus)
                                    .session(reactiveWebSocketSession)
                                    .build();
                            final ClientSessionClosedEvent event =
                                new ClientSessionClosedEvent(sessionCloseInfo);

                            closedEventManager.fire(event);
                          })
                      .then();

              final Flux<Void> serverClosedSessionListener =
                  serverSessionClosedEventManager
                      .asFlux()
                      .map(ServerSessionClosedEvent::payload)
                      .filter(
                          sessionCloseInfo ->
                              sessionCloseInfo.getSession().getSessionId().equals(sessionId))
                      .flatMap(
                          sessionCloseInfo -> session.close(sessionCloseInfo.getCloseStatus()));

              return session
                  .getHandshakeInfo()
                  .getPrincipal()
                  .switchIfEmpty(Mono.just(new Anonymous()))
                  .flatMap(
                      principal -> {
                        final WebSocketSessionContext webSocketSessionContext =
                            this.getSessionContext(
                                pathTemplate, sessionId, handshakeInfo, principal);
                        final Mono<Void> chain =
                            this.buildChain(session, webSocketSessionContext, streams);

                        return Mono.when(
                            chain, clientClosedSessionListener, serverClosedSessionListener.then());
                      })
                  // TODO: See if this is enough
                  .doOnError(throwable -> log.error(throwable.getMessage()));
            });
  }

  private ServerSessionClosedEvent getServerClosedEvent(
      final String sessionId, final CloseStatus closeStatus) {
    final ReactiveWebSocketSession webSocketSession =
        ReactiveWebSocketSession.builder().sessionId(sessionId).build();

    final SessionCloseInfo sessionCloseInfo =
        SessionCloseInfo.builder()
            .closeStatus(closeStatus)
            .session(webSocketSession)
            .closeInitiator(CloseInitiator.SERVER)
            .build();

    return new ServerSessionClosedEvent(sessionCloseInfo);
  }

  /**
   * Converts output objects to WebSocketMessages.
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>WebSocketMessage - passed through unchanged
   *   <li>byte[] - converted to binary message
   *   <li>InputStream - streamed as binary message
   *   <li>Any other object - serialized to JSON text message
   * </ul>
   *
   * @param session the WebSocket session for message creation
   * @param publisher the publisher of objects to convert
   * @return Flux of WebSocketMessages
   */
  protected Flux<WebSocketMessage> mapOutput(
      final WebSocketSession session, final Publisher<?> publisher) {
    return Flux.from(publisher)
        .flatMap(
            any -> {
              if (any instanceof WebSocketMessage webSocketMessage) {
                return Flux.just(webSocketMessage);
              } else if (any instanceof byte[] binary) {
                return Mono.just(session.binaryMessage(factory -> factory.wrap(binary)));
              } else if (InputStream.class.isAssignableFrom(any.getClass())) {
                return DataBufferUtils.readByteChannel(
                        () -> Channels.newChannel((InputStream) any),
                        DefaultDataBufferFactory.sharedInstance,
                        // TODO: Make buffer size configurable
                        4096)
                    .map(dataBuffer -> session.binaryMessage(factory -> dataBuffer));
              }

              return this.jsonMapper.applyWithFlux(any).map(session::textMessage);
            });
  }

  /**
   * Builds the WebSocket message processing chain.
   *
   * <p>This method follows the Subscribe Safety Guide:
   *
   * <ol>
   *   <li>Creates SessionStreams and registers in the registry
   *   <li>Wires WebSocket receive → inboundSink
   *   <li>Wires outboundSink → mapOutput() → WebSocket send
   *   <li>Handles ErrorResponseException from outboundSink
   *   <li>Calls processMessages() which returns Mono&lt;Void&gt;
   *   <li>Coordinates all chains with Mono.when() for proper lifecycle
   *   <li>Cleans up on close via doFinally()
   * </ol>
   *
   * <p><b>CRITICAL:</b> This returns Mono&lt;Void&gt; and lets the framework subscribe. Never call
   * .subscribe() manually in handlers!
   *
   * <p>Subclasses (like AdaptiveWebSocketHandler) can override this to add flow control.
   *
   * @param session the WebSocket session
   * @param webSocketSessionContext the session context with path/query params
   * @return Mono&lt;Void&gt; that completes when session closes
   */
  protected Mono<Void> buildChain(
      final WebSocketSession session,
      final WebSocketSessionContext webSocketSessionContext,
      final SessionStreams streams) {

    final String sessionId = webSocketSessionContext.sessionId();
    final String path = this.getPathTemplate();

    if (log.isDebugEnabled()) {
      log.debug("Registered session {} for path {}", sessionId, path);
    }

    final Flux<WebSocketMessage> incomingMessages = session.receive();
    final Mono<Void> input =
        incomingMessages
            .doOnNext(msg -> streams.inboundSink().tryEmitNext(msg))
            .doOnError(
                e -> {
                  if (log.isErrorEnabled()) {
                    log.error("Inbound error for session {}: {}", sessionId, e.getMessage());
                  }
                })
            .then();

    final Flux<WebSocketMessage> outboundMessages =
        mapOutput(session, streams.outboundFlux())
            .onErrorResume(
                ErrorResponseException.class,
                e -> {
                  if (log.isDebugEnabled()) {
                    log.debug(
                        "Caught a client-targeted exception. Sending error response to session {}",
                        sessionId);
                  }
                  return mapOutput(session, Flux.just(e.getPayload()));
                });

    final Mono<Void> output =
        session
            .send(outboundMessages)
            .doOnError(
                e -> {
                  if (log.isErrorEnabled()) {
                    log.error("Outbound error for session {}: {}", sessionId, e.getMessage());
                  }
                });

    final Publisher<?> processing =
        this.getProcessingPublisher(
            webSocketSessionContext, streams.inboundFlux(), streams.outboundSink(), session);

    return Mono.when(input, output, processing)
        .doFinally(
            signal -> {
              if (log.isTraceEnabled()) {
                log.debug("Session {} terminating with signal: {}", sessionId, signal);
              }
              sessionRegistry.unregister(path, sessionId);
              streams.close();
            });
  }

  Flux<?> getProcessingPublisher(
      final WebSocketSessionContext webSocketSessionContext,
      final Flux<WebSocketMessage> incomingStream,
      final Sinks.Many<Object> outboundSink,
      final WebSocketSession session) {

    try {
      final Publisher<?> processing =
          this.processMessages(webSocketSessionContext, incomingStream, outboundSink);
      return Flux.from(processing)
          .onErrorResume(
              WebSocketProcessingException.class,
              e ->
                  Flux.from(session.close(CloseStatus.PROTOCOL_ERROR.withReason(e.getMessage())))
                      .flatMap(__ -> Flux.empty()))
          .doOnError(
              e -> {
                if (log.isErrorEnabled()) {
                  log.error(
                      "Outbound message stream resulted in exception. Processing error for session {}: {}",
                      webSocketSessionContext.sessionId(),
                      e.getMessage());
                }
              });
    } catch (WebSocketProcessingException e) {
      if (log.isErrorEnabled()) {
        log.error(
            "Message processing method call resulted in exception. Processing error for session {}: {}",
            webSocketSessionContext.sessionId(),
            e.getMessage());
      }
      return Flux.from(session.close(CloseStatus.PROTOCOL_ERROR.withReason(e.getMessage())));
    }
  }

  private WebSocketSessionContext getSessionContext(
      final String pathTemplate,
      final String sessionId,
      final HandshakeInfo handshakeInfo,
      final Principal principal) {

    final String uriPath = handshakeInfo.getUri().getPath();
    final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

    final Map<String, String> pathParameters = uriTemplate.match(uriPath);
    final MultiValueMap<String, String> queryParameters =
        UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build().getQueryParams();
    final HttpHeaders headers = handshakeInfo.getHeaders();
    final MultiValueMap<String, HttpCookie> cookies = handshakeInfo.getCookies();

    final String remoteAddress =
        handshakeInfo.getRemoteAddress() != null
            ? handshakeInfo.getRemoteAddress().getAddress().getHostAddress()
            : null;

    return WebSocketSessionContext.builder()
        .pathTemplate(pathTemplate)
        .authentication(principal)
        .pathParameters(pathParameters)
        .queryParameters(queryParameters)
        .headers(headers)
        .cookies(cookies)
        .sessionId(sessionId)
        .remoteAddress(remoteAddress)
        .build();
  }

  /**
   * Gets the WebSocket path template.
   *
   * @return the path template (e.g., "/chat/{room}")
   */
  public String getPathTemplate() {
    return pathTemplate;
  }

  /**
   * Gets the session registry.
   *
   * @return the WebSocketSessionRegistry
   */
  protected ReactiveWebSocketSessionRegistry getSessionRegistry() {
    return sessionRegistry;
  }
}
