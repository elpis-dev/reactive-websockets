package org.elpis.socket.web.context.security.model;

import java.security.Principal;

public class TestPrincipal implements Principal {

    @Override
    public String getName() {
        return TestPrincipal.class.getName();
    }

}
