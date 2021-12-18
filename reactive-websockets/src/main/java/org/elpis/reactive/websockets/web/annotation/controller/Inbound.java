package org.elpis.reactive.websockets.web.annotation.controller;

import java.lang.annotation.*;

/**
 * Annotation is used to mark a method that would be accepting incoming messages from client. Unlike the {@link Outbound @Outbound}
 * this allows to declare a {@link org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody @SocketMessageBody} method parameter
 * of Flux or Mono type:
 *
 * <pre class="code">
 * &#064;Inbound
 * public void handleInbound(@SocketMessageBody final Flux<WebSocketMessage> messageFlux) {
 *    //...
 * }
 * </pre>
 *
 * Or same with Mono publisher:
 * <pre class="code">
 * &#064;Inbound
 * public void handleInbound(@SocketMessageBody final Mono<WebSocketMessage> messageFlux) {
 *    //...
 * }
 * </pre>
 *
 * @author Alex Zharkov
 * @since 0.1.0
 * @see org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody
 * @see SocketResource
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inbound {

    /**
     * The websocket path mapping (e.g. {@code "/home"}).
     * <p>Path patterns are also supported (e.g. {@code "/home/**"}).
     * Path mapping may contain placeholders (e.g. <code>"/{resourceId}"</code>) which would be resolved to path variables.
     * <p>A handler method that is not mapped to any path explicitly is effectively mapped to an empty path.
     * <p>Since each websocket resource (controller) is annotated with {@link SocketResource} that could set the base path
     * for all the {@link Inbound} and {@link Outbound} annotated methods - the final path would be resulted to concatenation of
     * {@link SocketResource#value()} and current value:
     *
     * <pre class="code">
     * &#064;SocketResource("/home")
     * public class HomeSocketResource {
     *
     *    &#064;Inbound("/index")
     *    public void handleInbound(@SocketMessageBody final Flux<WebSocketMessage> stream) {
     *       // Will result to "/home/index" path for current endpoint
     *    }
     *
     * }
     * </pre>
     *
     * @since 0.1.0
     */
    //TODO: Support multiple
    String value() default "";
}
