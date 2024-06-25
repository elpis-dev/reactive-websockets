package org.elpis.reactive.websockets.security.principal;

import java.security.Principal;

/**
 * Default {@link Principal} implementation that is put to security context when anonymous request is allowed.
 *
 * @author Alex Zharkov
 * @see Principal
 * @since 0.1.0
 */
public class Anonymous implements Principal {

    /**
     * See {@link Principal#getName()}
     */
    @Override
    public String getName() {
        return null;
    }
}
