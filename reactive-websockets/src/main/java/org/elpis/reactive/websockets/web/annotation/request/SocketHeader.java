package org.elpis.reactive.websockets.web.annotation.request;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.*;

/**
 * Marks a {@link org.elpis.reactive.websockets.web.annotation.controller.Inbound @Inbound} and {@link org.elpis.reactive.websockets.web.annotation.controller.Outbound @Outbound} annotated method parameter to take a specified header from request.
 * Works similar to {@link org.springframework.web.bind.annotation.RequestHeader @RequestHeader}.
 *
 * <p> Accepts all primitives, their wrappers, {@link java.math.BigInteger}, {@link java.math.BigDecimal}, {@link String} and any {@link Enum}
 *
 * @author Alex Zharkov
 * @see org.elpis.reactive.websockets.util.TypeUtils#convert(String, Class)
 * @see org.elpis.reactive.websockets.config.annotation.impl.HeaderAnnotationEvaluator
 * @since 0.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//TODO: Add ability to pass whole headers Map
public @interface SocketHeader {

    /**
     * Name alias.
     *
     * @since 0.1.0
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The name of the request header to bind to.
     *
     * @since 0.1.0
     */
    @AliasFor("value")
    String name() default "";

    /**
     * Marks whether the header is required.
     * <p>Defaults to {@code true}, leading to an exception being thrown
     * if the header is missing in the request. Switch this to
     * {@code false} if you prefer a {@code null} value if the header is
     * not present in the request.
     * <p>Alternatively, provide a {@link #defaultValue}, which implicitly
     * sets this flag to {@code false}.
     *
     * @since 0.1.0
     */
    boolean required() default true;

    /**
     * The default value to use as a fallback.
     * <p>Supplying a default value implicitly sets {@link #required} to
     * {@code false}.
     *
     * @since 0.1.0
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;

}
