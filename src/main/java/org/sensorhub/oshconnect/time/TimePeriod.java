package org.sensorhub.oshconnect.time;

import lombok.Getter;

import java.time.Instant;

@Getter
public class TimePeriod {
    private static final String ISO_8601_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z$";

    /**
     * The beginning of the time period.
     */
    private final Instant startTime;
    /**
     * The end of the time period.
     */
    private final Instant endTime;
    /**
     * Indicates the beginning time is unspecified, indeterminate, or "now".
     */
    private final boolean indeterminateStart;
    /**
     * Indicates the ending time is unspecified, indeterminate, or "now".
     */
    private final boolean indeterminateEnd;

    public TimePeriod(String startTime, String endTime) {
        this(parseTime(startTime), parseTime(endTime));
    }

    public TimePeriod(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.indeterminateStart = this.startTime == null;
        this.indeterminateEnd = this.endTime == null;
    }

    /**
     * Parses a time string into an Instant object.
     *
     * @param time The time string to parse.
     * @return The Instant object representing the time, or null if the time string is invalid.
     */
    private static Instant parseTime(String time) {
        if (time == null || time.isEmpty() || !time.matches(ISO_8601_REGEX)) {
            return null;
        }
        return Instant.parse(time);
    }

    /**
     * Computes the span of time in milliseconds from this time period.
     *
     * @return the span of time in milliseconds, or -1 if either begin or end position is indeterminate.
     */
    public long getTimeSpan() {
        if (indeterminateStart || indeterminateEnd) {
            return -1;
        }
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }
}
