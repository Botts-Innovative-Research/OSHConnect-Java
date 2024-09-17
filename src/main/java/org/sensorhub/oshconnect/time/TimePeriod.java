/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */

package org.sensorhub.oshconnect.time;

import lombok.Getter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class representing time within the context of OpenSensorHub.
 * <p>
 * Time periods can represent indeterminate time, such as "now" or
 * represent ISO UTC Time and usually have a {@link TimePeriod#beginPosition}
 * time and an {@link TimePeriod#endPosition} time.
 * <p>
 * Requests for realtime data can thus be structured as "now" as the
 * {@link TimePeriod#beginPosition} time and time in future represented
 * as ISO UTC time value as the {@link TimePeriod#endPosition}.
 */
@Getter
public class TimePeriod {
    /**
     * Default value for real-time data requests
     */
    public static final String REALTIME_START = "now";
    /**
     * Default value for real-time data requests
     */
    public static final String REALTIME_END = "...";
    /**
     * Default id for instances of <code>TimePeriod</code>
     */
    private static final String DEFAULT_ID = "INDETERMINATE | REALTIME";
    /**
     * Format for ISO UTC time
     */
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * ID associated with this time period.
     */
    private final String id;

    /**
     * The beginning of the time period.
     */
    private String beginPosition = REALTIME_START;

    /**
     * The end of the time period.
     */
    private String endPosition = REALTIME_END;

    /**
     * Indicates the beginning time is unspecified, indeterminate, or "now".
     */
    private boolean indeterminateStart = false;

    /**
     * Indicates the ending time is unspecified, indeterminate, or "now".
     */
    private boolean indeterminateEnd = false;

    /**
     * Invoke this constructor to set up a time period for real-time request of data.
     */
    public TimePeriod() {
        this(DEFAULT_ID);
    }

    /**
     * @param id The id of this TimePeriod.
     */
    public TimePeriod(String id) {
        this(id, null, null);
    }

    /**
     * @param id            The id of this TimePeriod.
     * @param beginPosition The beginning of the time periods time range.
     * @param endPosition   The end of the time periods time range.
     */
    public TimePeriod(String id, String beginPosition, String endPosition) {
        this.id = id;

        if (beginPosition == null) {
            indeterminateStart = true;
        } else {
            try {
                this.beginPosition = offsetTime(beginPosition, 0);
            } catch (ParseException ignored) {
                indeterminateStart = true;
            }
        }

        if (endPosition == null) {
            indeterminateEnd = true;
        } else {
            try {
                this.endPosition = offsetTime(endPosition, 0);
            } catch (ParseException ignored) {
                indeterminateEnd = true;
            }
        }
    }

    /**
     * Helper function to convert an ISO UTC time string to the milliseconds since Jan 1, 1970.
     *
     * @param time The string representation of the time.
     * @return The time as a millisecond value since epoch.
     * @throws ParseException if the time cannot be read or interpreted from the given string.
     */
    public static long getEpochTime(String time) throws ParseException {
        DateFormat df = new SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault());
        Date date = df.parse(time);
        return date.getTime();
    }

    /**
     * Offsets a UTC time by an epoch based offset.
     *
     * @param time   the time given in UTC.
     * @param offset the offset to apply to the given time.
     * @return A new UTC time given time and offset by given value.
     * @throws ParseException If the time string cannot be parsed.
     */
    public static String offsetTime(String time, long offset) throws ParseException {
        long epochTime = getEpochTime(time);
        epochTime += offset;
        return getFormattedTime(epochTime);
    }

    /**
     * Gets an epoch time formatted as UTC string.
     *
     * @param epochTime the epoch time to parse into a formatted string.
     * @return UTC formatted string.
     */
    public static String getFormattedTime(long epochTime) {
        DateFormat df = new SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault());
        Date date = new Date(epochTime);
        return df.format(date);
    }

    public static String getUTCFormattedTime(long epochTime) {
        DateFormat df = new SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(epochTime);
        return df.format(date);
    }

    /**
     * Computes a maximum time range from a set of {@link TimePeriod}s
     * and returns the range as a new {@link TimePeriod}.
     *
     * @param timePeriods The set of time periods used to compute a maximum, longest time period.
     * @return The computed time period.
     */
    public static TimePeriod getMaxTimeRange(List<TimePeriod> timePeriods) {
        long start = 0;
        long end = 0;

        for (TimePeriod timePeriod : timePeriods) {
            if (!timePeriod.isIndeterminateStart()) {
                try {
                    long currentStart = getEpochTime(timePeriod.getBeginPosition());
                    long currentEnd = getEpochTime(timePeriod.getEndPosition());

                    start = (start == 0) ? currentStart : Math.min(start, currentStart);
                    end = (end == 0) ? currentEnd : Math.max(end, currentEnd);
                } catch (ParseException ignored) {

                }
            }
        }

        return new TimePeriod(UUID.randomUUID().toString(), getFormattedTime(start), getFormattedTime(end));
    }

    /**
     * Computes the span of time in milliseconds from this time period.
     *
     * @return the span of time in milliseconds, or -1 if either begin or end position is indeterminate.
     * @throws ParseException If either the beginning position time or end position time cannot be parsed.
     */
    public long getTimeSpanMillis() throws ParseException {
        long millis = -1;

        if (!indeterminateEnd && !indeterminateStart) {
            millis = getEpochTime(endPosition) - getEpochTime(beginPosition);
        }

        return millis;
    }
}
