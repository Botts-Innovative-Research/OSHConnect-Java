package org.sensorhub.oshconnect.util;

import org.sensorhub.oshconnect.OSHDataStream;
import org.vast.util.TimeExtent;

import java.util.List;

/**
 * Query string parameters used to filter the results of a {@link OSHDataStream#getObservations(ObservationsQueryBuilder)} request.
 */
public class ObservationsQueryBuilder extends QueryStringBuilder {
    /**
     * List of resource local IDs or unique IDs (URI).
     * Only resources that have one of the provided identifiers are selected.
     */
    public ObservationsQueryBuilder id(List<String> id) {
        return (ObservationsQueryBuilder) addParameter("id", id);
    }

    /**
     * Either a date-time or an interval.
     * Date and time expressions adhere to RFC 3339.
     * Intervals may be bounded or half-bounded (double-dots at start or end).
     */
    public ObservationsQueryBuilder phenomenonTime(TimeExtent phenomenonTime) {
        return (ObservationsQueryBuilder) addParameter("phenomenonTime", phenomenonTime);
    }

    /**
     * Either a date-time or an interval.
     * Date and time expressions adhere to RFC 3339.
     * Intervals may be bounded or half-bounded (double-dots at start or end).
     */
    public ObservationsQueryBuilder resultTime(TimeExtent resultTime) {
        return (ObservationsQueryBuilder) addParameter("resultTime", resultTime);
    }

    /**
     * List of data stream local IDs or unique IDs (URI).
     * Only resources that are associated with a System that has one of the provided identifiers are selected.
     */
    public ObservationsQueryBuilder dataStream(List<String> dataStream) {
        return (ObservationsQueryBuilder) addParameter("dataStream", dataStream);
    }

    /**
     * List of system local IDs or unique IDs (URI).
     * Only resources that are associated with a System that has one of the provided identifiers are selected.
     */
    public ObservationsQueryBuilder system(List<String> system) {
        return (ObservationsQueryBuilder) addParameter("system", system);
    }

    /**
     * List of feature local IDs or unique IDs (URI).
     * Only resources that are associated with a feature of interest that has one of the provided identifiers are selected.
     */
    public ObservationsQueryBuilder foi(List<String> foi) {
        return (ObservationsQueryBuilder) addParameter("foi", foi);
    }

    /**
     * List of property local IDs or unique IDs (URI).
     * Only resources that are associated with an observable property that has one of the provided identifiers are selected.
     */
    public ObservationsQueryBuilder observedProperty(List<String> observedProperty) {
        return (ObservationsQueryBuilder) addParameter("observedProperty", observedProperty);
    }

    /**
     * Limits the number of items that are presented in the response document.
     */
    public ObservationsQueryBuilder limit(int limit) {
        return (ObservationsQueryBuilder) addParameter("limit", limit);
    }
}
