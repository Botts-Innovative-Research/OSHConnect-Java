package org.sensorhub.oshconnect.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    private TimeUtils() {
    }
    
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Changes the epoch time to UTC.
     *
     * @param epochTime The epoch time to convert.
     * @return epoch time in UTC.
     */
    public static long epochTimeToUtc(long epochTime) {
        DateFormat df = new SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(epochTime);

        return date.getTime();
    }

    /**
     * Changes the epoch time to UTC.
     *
     * @param epochTime The epoch time to convert.
     * @return epoch time in UTC.
     */
    public static long epochTimeToUtc(double epochTime) {
        return epochTimeToUtc((long) epochTime * 1000);
    }
}
