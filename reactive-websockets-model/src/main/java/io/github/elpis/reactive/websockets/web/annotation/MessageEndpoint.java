package io.github.elpis.reactive.websockets.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a "Websocket Controller/Resource".
 *
 * <p>Annotation works in a similar way as {@link org.springframework.stereotype.Controller @Controller} but generally will be combined
 * with {@link OnMessage @SocketMapping} annotation. Includes {@link Component @Component} to create a bean from annotated class
 *
 * @author Phillip J. Fry
 * @see Component
 * @see OnMessage
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Controller
@Documented
@Inherited
public @interface MessageEndpoint {

    /**
     * The base websocket path mapping (e.g. {@code "/home"}) for all the included {@link OnMessage @SocketMapping} annotated methods.
     * for all the {@link OnMessage @SocketMapping} annotated methods - the final path would be resulted to concatenation of
     * {@link MessageEndpoint @MessageEndpoint.value} and {@link OnMessage#value() @SocketMapping.value}:
     *
     * @since 1.0.0
     */
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
