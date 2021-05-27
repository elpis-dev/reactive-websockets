package org.elpis.reactive.websockets.mertics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class WebSocketMetricsService {
    private final MeterRegistry meterRegistry;

    public WebSocketMetricsService(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public <T> T withGauge(@NonNull T data, @NonNull BiConsumer<T, MeterRegistry> gaugeFunction) {
        gaugeFunction.accept(data, meterRegistry);

        return data;
    }

    public <T> T withTimer(@NonNull final Function<BiFunction<String, Iterable<Tag>, Long>, T> sampleFunction) {
        final Timer.Sample timer = Timer.start(meterRegistry);

        return sampleFunction.apply((key, tags) -> timer.stop(this.meterRegistry.timer(key, tags)));
    }

    public enum MeterConstants {
        ACTIVE_SESSIONS("rws.sessions.active", "Number of active sessions"),
        SESSION_CONNECTION_TIME("rws.session.connection", "");

        public static final String RESULT = "result";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILURE = "FAILURE";

        private String key;
        private String description;

        MeterConstants(final String key, final String description) {
            this.key = key;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
