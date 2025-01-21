package org.sensorhub.oshconnect.util;

import lombok.Getter;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for building query strings for OpenSensorHub API requests.
 * <p>
 * Note that if the key or value of a parameter is null or empty, it will not be included in the query string.
 * As such, it is safe to add parameters unconditionally.
 * <p>
 * The resulting query string may be obtained by calling {@link #getQueryString()},
 * and may be an empty string if no or null parameters were added.
 */
@Getter
public class QueryStringBuilder {
    private static final Instant MIN_TIME = Instant.parse("0000-01-01T00:00:00Z");
    private static final Instant MAX_TIME = Instant.parse("9999-12-31T23:59:59.999Z");
    private static final String SPECIAL_VALUE_NOW = "now";

    /**
     * The map of parameters. The key is the parameter name, and the value is the parameter value.
     * This will not contain any parameters with null or empty values.
     */
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * Create a new QueryStringBuilder from a map of parameters.
     *
     * @param map The map of parameters.
     * @return The new QueryStringBuilder.
     */
    public static QueryStringBuilder fromMap(Map<String, String> map) {
        QueryStringBuilder builder = new QueryStringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public QueryStringBuilder addParameter(String key, String value) {
        if (key == null || key.isEmpty()) return this;
        if (value == null || value.isEmpty()) return this;
        parameters.put(key, value);
        return this;
    }

    /**
     * Add a parameter with a list of values. The values will be joined with commas.
     */
    public QueryStringBuilder addParameter(String key, List<String> values) {
        if (values == null || values.isEmpty()) return this;
        return addParameter(key, String.join(",", values));
    }

    public QueryStringBuilder addParameter(String key, Integer value) {
        return value == null ? this : addParameter(key, Integer.toString(value));
    }

    public QueryStringBuilder addParameter(String key, Double value) {
        return value == null ? this : addParameter(key, Double.toString(value));
    }

    public QueryStringBuilder addParameter(String key, Boolean value) {
        return value == null ? this : addParameter(key, Boolean.toString(value));
    }

    public QueryStringBuilder addParameter(String key, Long value) {
        return value == null ? this : addParameter(key, Long.toString(value));
    }

    public QueryStringBuilder addParameter(String key, Float value) {
        return value == null ? this : addParameter(key, Float.toString(value));
    }

    /**
     * Add a time extent parameter.
     * This will format the time extent as a string in one of the following formats:
     * <ul>
     *     <li>"now" if the time extent represents the special case of the current time.</li>
     *     <li>{@code time} if the time extent represents an instant.</li>
     *     <li>{@code begin/end} if the time extent represents a period of time.</li>
     * </ul>
     */
    public QueryStringBuilder addParameter(String key, TimeExtent value) {
        if (value == null) return this;

        if (value.begin() == null && value.end() == null) {
            return addParameter(key, SPECIAL_VALUE_NOW);
        } else if (value.isInstant()) {
            return addParameter(key, value.begin().toString());
        } else {
            String timeExtent = formatInstant(value.begin()) +
                    '/' +
                    formatInstant(value.end());
            return addParameter(key, timeExtent);
        }
    }

    public QueryStringBuilder addParameter(String key, Instant value) {
        return value == null ? this : addParameter(key, value.toString());
    }

    /**
     * Get the query string representation of the parameters.
     * If no parameters were added or all parameters were null, this will return an empty string.
     *
     * @return The query string.
     */
    public String getQueryString() {
        if (parameters.isEmpty()) return "";

        StringBuilder queryString = new StringBuilder();
        queryString.append('?');
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            queryString.append(entry.getKey());
            queryString.append('=');
            queryString.append(entry.getValue());
            queryString.append('&');
        }
        queryString.deleteCharAt(queryString.length() - 1);
        return queryString.toString();
    }

    @Override
    public String toString() {
        return getQueryString();
    }

    /**
     * Format an instant as a string for use in a time extent parameter.
     * Dates before 0000-01-01T00:00:00Z are clamped to 0000-01-01T00:00:00Z and
     * dates after 9999-12-31T23:59:59.999Z are clamped to 9999-12-31T23:59:59.999Z.
     * This is a workaround for the fact that OSH throws an error if the date is outside this range.
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return SPECIAL_VALUE_NOW;
        } else if (instant.isBefore(MIN_TIME)) {
            return MIN_TIME.toString();
        } else if (instant.isAfter(MAX_TIME)) {
            return MAX_TIME.toString();
        }
        return instant.toString();
    }
}