package org.elpis.reactive.websockets.util;

import org.springframework.lang.NonNull;

import javax.annotation.PreDestroy;

public class TerminateBean {
    private final Runnable runnable;

    private TerminateBean(final Runnable runnable) {
        this.runnable = runnable;
    }

    @PreDestroy
    public void onDestroy() {
        this.runnable.run();
    }

    public static TerminateBean with(@NonNull final Runnable runnable) {
        return new TerminateBean(runnable);
    }
}