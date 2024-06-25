package org.elpis.reactive.websockets.web.annotation;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a "Websocket Controller/Resource".
 *
 * <p>Annotation works in a similar way as {@link org.springframework.stereotype.Controller @Controller} but generally will be combined
 * with {@link SocketMapping @SocketMapping} annotation. Includes {@link Component @Component} to create a bean from annotated class
 *
 * @author Alex Zharkov
 * @see Component
 * @see SocketMapping
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Controller
@Documented
@Inherited
public @interface SocketController {

    /**
     * The base websocket path mapping (e.g. {@code "/home"}) for all the included {@link SocketMapping @SocketMapping} annotated methods.
     * for all the {@link SocketMapping @SocketMapping} annotated methods - the final path would be resulted to concatenation of
     * {@link SocketController @SocketController.value} and {@link SocketMapping#value() @SocketMapping.value}:
     *
     * @since 0.1.0
     */
    String value() default "";
}
