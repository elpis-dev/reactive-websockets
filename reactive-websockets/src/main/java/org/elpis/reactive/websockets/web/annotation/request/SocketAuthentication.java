package org.elpis.reactive.websockets.web.annotation.request;

import org.elpis.reactive.websockets.web.annotation.controller.SocketMapping;

import java.lang.annotation.*;

/**
 * Marks a {@link SocketMapping @ReceiveMapping} annotated method parameter to grab a user {@link java.security.Principal Principal} from security context.
 * <p> Accepts {@link java.security.Principal Principal}, {@link org.springframework.security.core.Authentication Authentication} or custom implementation of those. Could also
 * accept any {@link Object} which will default to calling {@link org.springframework.security.core.Authentication#getDetails() Authentication.getDetails}:
 * <pre class="code">
 * public void extractedAuthentication(@SocketAuthentication final String authentication) {
 *    // Will extract {@link org.springframework.security.core.Authentication#getDetails()} and pass to method
 * }
 *
 * public void principal(@SocketAuthentication final Principal principal) {
 *    // Will extract {@link org.springframework.security.core.Authentication#getPrincipal()} or take {@link java.security.Principal} from context and pass to method
 * }
 *
 * public void authentication(@SocketAuthentication final Authentication authentication) {
 *    // Will take {@link org.springframework.security.core.Authentication} from context and pass to method
 * }
 *
 * public void customPrincipal(@SocketAuthentication final MyCustomPrincipal principal) {
 *    // Will extract custom {@link org.springframework.security.core.Authentication#getPrincipal()} or take custom {@link java.security.Principal} from context and pass to method
 * }
 *
 * public void customAuthentication(@SocketAuthentication final MyCustomAuthentication authentication) {
 *    // Will take custom {@link org.springframework.security.core.Authentication} from context and pass to method
 * }
 * </pre>
 *
 * @author Alex Zharkov
 * @see java.security.Principal
 * @see org.springframework.security.core.Authentication
 * @since 0.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketAuthentication {

}
