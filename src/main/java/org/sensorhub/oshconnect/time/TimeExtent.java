package org.sensorhub.oshconnect.time;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable class for storing a time instant or time period.
 * <p/>
 * This class also supports special cases of time instants at 'now', time
 * periods beginning or ending at 'now', and open-ended time periods.
 */
public class TimeExtent {
    // Workaround since OSH only likes 4 digit years
    private static final Instant MIN_TIME = Instant.parse("0000-01-01T00:00:00Z");
    private static final Instant MAX_TIME = Instant.parse("9999-12-31T23:59:59.999Z");
    public static final String SPECIAL_VALUE_NOW = "now";
    public static final String SPECIAL_VALUE_UNBOUNDED = "..";
    public static final TimeExtent ALL_TIMES = TimeExtent.period(MIN_TIME, MAX_TIME);
    public static final TimeExtent ALL_FUTURE_TIMES = TimeExtent.period(Instant.now(), MAX_TIME);
    public static final TimeExtent ALL_PAST_TIMES = TimeExtent.period(MIN_TIME, Instant.now());
    public static final TimeExtent TIME_NOW = new TimeExtent();

    /**
     * The beginning of the time period. Null means 'now', Instant.MIN means unbounded
     */
    protected Instant startTime = null;
    /**
     * The end of the time period. Null means 'now', Instant.MAX means unbounded
     */
    protected Instant endTime = null;

    protected TimeExtent() {

    }

    /**
     * @return A time extent representing the special value 'now'
     */
    public static TimeExtent now() {
        return TIME_NOW;
    }

    /**
     * @return A time extent representing the current time instant,
     * that is to say the value returned by {@link Instant#now()}
     */
    public static TimeExtent currentTime() {
        return TimeExtent.instant(Instant.now());
    }

    /**
     * @param time Time instant
     * @return A time extent representing a time instant
     */
    public static TimeExtent instant(Instant time) {
        if (time == null) return now();

        TimeExtent timeExtent = new TimeExtent();
        timeExtent.startTime = timeExtent.endTime = time;
        return timeExtent;
    }

    /**
     * @param begin Beginning of time period
     * @param end   End of time period
     * @return A time extent representing a time period
     */
    public static TimeExtent period(Instant begin, Instant end) {
        if (begin == null)
            throw new NullPointerException("begin cannot be null");
        if (end == null)
            throw new NullPointerException("end cannot be null");
        if (begin.isAfter(end))
            throw new IllegalArgumentException("begin cannot be after end");

        TimeExtent timeExtent = new TimeExtent();
        timeExtent.startTime = begin;
        timeExtent.endTime = end;
        return timeExtent;
    }

    /**
     * @param start Start time instant
     * @return An open-ended time extent starting at the specified time
     */
    public static TimeExtent startingAt(Instant start) {
        return TimeExtent.period(start, MAX_TIME);
    }

    /**
     * @param end End time instant
     * @return An open time extent ending at the specified time
     */
    public static TimeExtent endingAt(Instant end) {
        return TimeExtent.period(MIN_TIME, end);
    }

    /**
     * @param end End time instant
     * @return A time extent starting 'now' and ending at the specified time
     */
    public static TimeExtent startingNow(Instant end) {
        if (end == null)
            throw new NullPointerException("end cannot be null");
        if (end.compareTo(Instant.now()) < 0)
            throw new IllegalArgumentException("end must be after current time");

        TimeExtent timeExtent = new TimeExtent();
        timeExtent.startTime = null;
        timeExtent.endTime = end;
        return timeExtent;
    }

    /**
     * @return True if this time extent begins at 'now', false otherwise
     */
    public boolean startsNow() {
        return startTime == null;
    }

    /**
     * @return True if this time extent ends at 'now', false otherwise
     */
    public boolean endsNow() {
        return endTime == null;
    }

    /**
     * @return True if this time extent represents a time instant,
     * false if it represents a time period
     */
    public boolean isInstant() {
        return Objects.equals(startTime, endTime);
    }

    /**
     * @return A string representation of the time extent in ISO 8601 format.
     */
    public String isoStringUTC() {
        if (isInstant()) {
            if (startTime == null) {
                return SPECIAL_VALUE_NOW;
            }
            return startTime.toString();
        }

        return String.valueOf(startTime == null ? SPECIAL_VALUE_NOW : startTime) +
                '/' +
                (endTime == null ? SPECIAL_VALUE_NOW : endTime);
    }

    public Instant getStartTime() {
        return startTime == null ? Instant.now() : startTime;
    }

    public Instant getEndTime() {
        return endTime == null ? Instant.now() : endTime;
    }
}
