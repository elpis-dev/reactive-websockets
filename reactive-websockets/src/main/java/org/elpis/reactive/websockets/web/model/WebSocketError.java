package org.elpis.reactive.websockets.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WebSocketError {
    private String message;
}
