package org.elpis.reactive.websockets.mertics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.NonNull;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Prometheus metrics registry service. Defines common methods to post statistics data to actuator.
 *
 * @author Alex Zharkov
 * @see MeterRegistry
 * @since 0.1.0
 */
@Service
public final class WebSocketMetricsService {
    private final MeterRegistry meterRegistry;

    public WebSocketMetricsService(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Posts a new static metric based on some data object passed.
     * <p>See {@link WebSessionRegistry#post()} for an example.
     *
     * @param data          any data object to observe
     * @param gaugeFunction {@link Function} that accepts an observed object and returns a {@link io.micrometer.core.instrument.Gauge.Builder Gauge.Builder}
     * @since 0.1.0
     */
    public <T> void withGauge(@NonNull T data, @NonNull Function<T, Gauge.Builder<T>> gaugeFunction) {
        gaugeFunction.apply(data).register(meterRegistry);
    }

    /**
     * Posts a new static metric based on calculated time interval.
     * E.x.
     * <pre>
     * withTimer(stop -> {
     *      ....
     *      return stop.andThen(...return your data...).apply(key, tags);
     * );
     *
     * // OR
     *
     * withTimer(stop -> stop.apply(key, tags))
     * </pre>
     *
     * @param sampleFunction {@link Function} that accepts a stop function and waits it to be called with metric key and tags. Could also provide any data.
     * @return data that could be added to {@code stop.andThen(...return your data...)}
     * @since 0.1.0
     */
    public <T> T withTimer(@NonNull final Function<BiFunction<String, Iterable<Tag>, Long>, T> sampleFunction) {
        final Timer.Sample timer = Timer.start(meterRegistry);

        return sampleFunction.apply((key, tags) -> timer.stop(this.meterRegistry.timer(key, tags)));
    }

    /**
     * Supported metrics keys.
     *
     * @author Alex Zharkov
     * @see Tag
     * @since 0.1.0
     */
    public enum MeterConstants {

        /**
         * Covers currently active sessions.
         */
        ACTIVE_SESSIONS("rws.sessions.active", "Number of active sessions"),

        /**
         * Covers average sessions connection time.
         */
        SESSION_CONNECTION_TIME("rws.session.connection", "");

        public static final String RESULT = "result";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILURE = "FAILURE";

        private final String key;
        private final String description;

        MeterConstants(final String key, final String description) {
            this.key = key;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }
    }
}
