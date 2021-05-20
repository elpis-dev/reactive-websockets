package org.elpis.reactive.websockets.security.principal;

import java.security.Principal;

public class Anonymous implements Principal {

    @Override
    public String getName() {
        return null;
    }
}
