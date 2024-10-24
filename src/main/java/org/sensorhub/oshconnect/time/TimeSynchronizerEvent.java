package org.sensorhub.oshconnect.time;

import lombok.Getter;

/**
 * Represents an event with a timestamp for time synchronization.
 *
 * @param <T> The type of the event.
 */
@Getter
public class TimeSynchronizerEvent<T> {
    private final long timestamp;
    private final T event;

    public TimeSynchronizerEvent(long timestamp, T event) {
        this.timestamp = timestamp;
        this.event = event;
    }
}
