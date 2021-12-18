package org.elpis.reactive.websockets.web.annotation.request;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a {@link org.elpis.reactive.websockets.web.annotation.controller.Inbound @Inbound} and {@link org.elpis.reactive.websockets.web.annotation.controller.Outbound @Outbound} annotated method parameter to take a named path variable from uri.
 * Works similar to {@link org.springframework.web.bind.annotation.PathVariable @PathVariable}.
 *
 * <p> Accepts all primitives, their wrappers, {@link java.math.BigInteger}, {@link java.math.BigDecimal}, {@link String} and any {@link Enum}
 *
 * @author Alex Zharkov
 * @see org.elpis.reactive.websockets.util.TypeUtils#convert(String, Class)
 * @see org.elpis.reactive.websockets.config.annotation.impl.PathVariableAnnotationEvaluator
 * @since 0.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketPathVariable {

    /**
     * Name alias.
     * @since 0.1.0
     */
    @AliasFor("name")
    String value();

    /**
     * The name of the path variable to bind to.
     * @since 0.1.0
     */
    @AliasFor("value")
    String name() default "";

    /**
     * Marks whether the path variable is required.
     * <p>Defaults to {@code true}, leading to an exception being thrown
     * if the path variable is missing in the request uri. Switch this to
     * {@code false} if you prefer a {@code null} value if the path variable is
     * not present in the request.
     * @since 0.1.0
     */
    boolean required() default true;

}
