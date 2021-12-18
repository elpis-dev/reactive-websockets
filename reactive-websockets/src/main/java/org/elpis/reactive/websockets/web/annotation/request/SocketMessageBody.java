package org.elpis.reactive.websockets.web.annotation.request;

import java.lang.annotation.*;

/**
 * Marks a {@link org.elpis.reactive.websockets.web.annotation.controller.Inbound @Inbound} annotated method parameter to take a {@link reactor.core.publisher.Flux Flux} or {@link reactor.core.publisher.Mono Mono} of {@link org.springframework.web.reactive.socket.WebSocketMessage WebSocketMessage}.
 * <pre class="code">
 * public void receiveFlux(@SocketMessageBody final Flux<WebSocketMessage> stream) {
 *    //...
 * }
 *
 * public void receiveMono(@SocketMessageBody final Mono<WebSocketMessage> stream) {
 *    //...
 * }
 * </pre>
 *
 * @author Alex Zharkov
 * @see org.elpis.reactive.websockets.web.annotation.controller.Inbound
 * @see org.springframework.web.reactive.socket.WebSocketMessage
 * @see org.elpis.reactive.websockets.config.annotation.impl.MessageBodyAnnotationEvaluator
 * @since 0.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMessageBody {
}
