package org.elpis.reactive.websockets.web.annotation.controller;

import java.lang.annotation.*;

/**
 * Annotation is used to mark a method that would be sending messages to client. Unlike the {@link Inbound}
 * this allows return any instance of {@link org.reactivestreams.Publisher Publisher} from annotated method:
 *
 * <pre class="code">
 * &#064;Outbound
 * public Publisher<?> handleOutbound() {
 *    return Flux.just(...);
 * }
 * </pre>
 *
 * <p><strong>NOTE: </strong> All objects except {@link String} or {@link org.springframework.web.reactive.socket.CloseStatus CloseStatus} sent with {@link org.reactivestreams.Publisher Publisher}
 * would be mapped with {@link com.fasterxml.jackson.databind.ObjectMapper ObjectMapper} to JSON
 *
 * @author Alex Zharkov
 * @since 0.1.0
 * @see org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody
 * @see SocketResource
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Outbound {

    /**
     * The websocket path mapping (e.g. {@code "/home"}).
     * <p>Path patterns are also supported (e.g. {@code "/home/**"}).
     * Path mapping may contain placeholders (e.g. <code>"/{resourceId}"</code>) which would be resolved to path variables.
     * <p>A handler method that is not mapped to any path explicitly is effectively mapped to an empty path.
     * <p>Since each websocket resource (controller) is annotated with {@link SocketResource @SocketResource} that could set the base path
     * for all the {@link Inbound @Inbound} and {@link Outbound @Outbound} annotated methods - the final path would be resulted to concatenation of
     * {@link SocketResource#value() @SocketResource.value} and {@link #value() @Outbound.value}:
     *
     * <pre class="code">
     * &#064;SocketResource("/home")
     * public class HomeSocketResource {
     *
     *    &#064;Outbound
     *    public Publisher<?> handleOutbound() {
     *       return Flux.just(...);
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
