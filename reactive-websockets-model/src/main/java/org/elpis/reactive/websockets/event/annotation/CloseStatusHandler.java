package org.elpis.reactive.websockets.event.annotation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class handles cases when websocket sessions are closing down.
 *
 * <p>Annotation works in a similar way as {@link org.springframework.web.bind.annotation.ControllerAdvice @ControllerAdvice} is used for exception handling.
 * For effective use should be combined with {@link SessionCloseStatus @SessionCloseStatus} and {@link EventSelector @EventSelector} annotations. Includes {@link Component @Component} to create a bean from annotated class
 *
 * @author Alex Zharkov
 * @see SessionCloseStatus
 * @see EventSelector
 * @see Component
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Qualifier
public @interface CloseStatusHandler {
}
