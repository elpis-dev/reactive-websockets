package org.elpis.reactive.websockets.web.annotation.controller;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a "Websocket Controller/Resource".
 *
 * <p>Annotation works in a similar way as {@link org.springframework.stereotype.Controller @Controller} but generally will be combined
 * with {@link Inbound @Inbound} and {@link Outbound @Outbound} annotations. Includes {@link Component @Component} to create a bean from annotated class

 * @author Alex Zharkov
 * @since 0.1.0
 * @see Inbound
 * @see Outbound
 * @see Component
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
@Inherited
public @interface SocketResource {

    /**
     * The base websocket path mapping (e.g. {@code "/home"}) for all the included {@link Inbound @Inbound} and {@link Outbound @Outbound} annotated methods.
     * for all the {@link Inbound @Inbound} and {@link Outbound @Outbound} annotated methods - the final path would be resulted to concatenation of
     * {@link #value() @SocketResource.value} and {@link Inbound#value() @Inbound.value} or {@link Outbound#value() @Outbound.value}:
     *
     * @since 0.1.0
     */
    String value() default "";
}
