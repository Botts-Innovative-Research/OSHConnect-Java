package org.sensorhub.oshconnect.util;

import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.Arrays;
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
public class QueryStringBuilder {
    private static final Instant MIN_TIME = Instant.parse("0000-01-01T00:00:00Z");
    private static final Instant MAX_TIME = Instant.parse("9999-12-31T23:59:59.999Z");

    /**
     * The map of parameters.
     * The key is the parameter name, and the value is the parameter value.
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

    /**
     * Convert a variable number of elements to a list.
     *
     * @param elements The elements to convert.
     * @return The list of elements.
     */
    @SafeVarargs
    protected static <T> List<T> convertToList(T... elements) {
        return Arrays.asList(elements);
    }

    /**
     * Add a parameter to the query string.
     * If the key or value is null or empty, the parameter will not be added.
     *
     * @param key   The name of the parameter.
     * @param value The value of the parameter.
     */
    public QueryStringBuilder addParameter(String key, String value) {
        if (key == null || key.isEmpty()) return this;
        if (value == null || value.isEmpty()) return this;
        parameters.put(key, value);
        return this;
    }

    /**
     * Add a parameter to the query string.
     * The value will be converted to a string using its {@link Object#toString()} method.
     * If the value is null, the parameter will not be added.
     *
     * @param key   The name of the parameter.
     * @param value The value of the parameter.
     */
    public <T> QueryStringBuilder addParameter(String key, T value) {
        if (value != null)
            addParameter(key, value.toString());
        return this;
    }

    /**
     * Add a parameter with a list of values. The values will be joined with commas.
     */
    public <T> QueryStringBuilder addParameter(String key, List<T> values) {
        if (values != null && !values.isEmpty()) {
            StringBuilder csv = new StringBuilder();
            for (T value : values) {
                if (value == null) continue;
                csv.append(value);
                csv.append(',');
            }
            csv.deleteCharAt(csv.length() - 1); // Remove trailing comma
            addParameter(key, csv.toString());
        }
        return this;
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

        if (value.isNow()) {
            addParameter(key, TimeExtent.SPECIAL_VALUE_NOW);
        } else if (value.isInstant()) {
            return addParameter(key, value.begin().toString());
        } else {
            String timeExtent = formatInstant(value.begin()) +
                    '/' +
                    formatInstant(value.end());
            return addParameter(key, timeExtent);
        }

        return this;
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
     * Dates before 0000-01-01T00:00:00Z and dates after 9999-12-31T23:59:59.999Z are considered unbounded.
     * This is a workaround for the fact that OSH throws an error if the date is outside this range.
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return TimeExtent.SPECIAL_VALUE_NOW;
        } else if (instant.isBefore(MIN_TIME)) {
            return TimeExtent.SPECIAL_VALUE_UNBOUNDED;
        } else if (instant.isAfter(MAX_TIME)) {
            return TimeExtent.SPECIAL_VALUE_UNBOUNDED;
        }
        return instant.toString();
    }

    /**
     * The map of parameters.
     * The key is the parameter name, and the value is the parameter value.
     * This will not contain any parameters with null or empty values.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }
}