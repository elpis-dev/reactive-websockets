package org.elpis.socket.web.context;

import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.elpis.reactive.websockets.config.annotation.impl.*;
import org.elpis.socket.web.context.resource.data.HeaderSocketResource;
import org.elpis.socket.web.context.resource.data.MessageBodySocketResource;
import org.elpis.socket.web.context.resource.data.PathVariableSocketResource;
import org.elpis.socket.web.context.resource.data.QueryParamSocketResource;
import org.elpis.socket.web.context.resource.security.SecurityChainResource;
import org.elpis.socket.web.context.resource.security.WebFilterSecurityResource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableReactiveSockets
@Import({
        // Data Socket Controllers
        HeaderSocketResource.class,
        PathVariableSocketResource.class,
        QueryParamSocketResource.class,
        MessageBodySocketResource.class,

        // Security Socket Controllers
        WebFilterSecurityResource.class,
        SecurityChainResource.class,

        // Annotation evaluators
        AuthenticationAnnotationEvaluator.class,
        HeaderAnnotationEvaluator.class,
        MessageBodyAnnotationEvaluator.class,
        PathVariableAnnotationEvaluator.class,
        QueryParameterAnnotationEvaluator.class
})
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }

    public enum Example {
        VOID
    }
}
