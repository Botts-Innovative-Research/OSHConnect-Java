package org.sensorhub.oshconnect.util;

import org.sensorhub.oshconnect.OSHNode;
import org.vast.util.TimeExtent;

import java.util.List;

/**
 * Query string parameters used to filter the results of a {@link OSHNode#discoverSystems(SystemsQueryBuilder)} request.
 */
public class SystemsQueryBuilder extends QueryStringBuilder {
    /**
     * List of resource local IDs or unique IDs (URI).
     * Only resources that have one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder id(List<String> id) {
        return (SystemsQueryBuilder) addParameter("id", id);
    }

    /**
     * The bounding box is provided as four or six numbers,
     * depending on whether the coordinate reference system includes a vertical axis (height or depth).
     * The order of coordinates is as follows:
     * <ul>
     *     <li>Lower left corner, coordinate axis 1</li>
     *     <li>Lower left corner, coordinate axis 2</li>
     *     <li>Minimum value, coordinate axis 3 (optional)</li>
     *     <li>Upper right corner, coordinate axis 1</li>
     *     <li>Upper right corner, coordinate axis 2</li>
     *     <li>Maximum value, coordinate axis 3 (optional)</li>
     */
    public SystemsQueryBuilder bbox(List<Double> bbox) {
        if (bbox == null || (bbox.size() != 4 && bbox.size() != 6))
            throw new IllegalArgumentException("bbox must be a list of 4 or 6 numbers");
        return (SystemsQueryBuilder) addParameter("bbox", bbox);
    }

    /**
     * The bounding box is provided as four or six numbers,
     * depending on whether the coordinate reference system includes a vertical axis (height or depth).
     * The order of coordinates is as follows:
     * <ul>
     *     <li>Lower left corner, coordinate axis 1</li>
     *     <li>Lower left corner, coordinate axis 2</li>
     *     <li>Minimum value, coordinate axis 3 (optional)</li>
     *     <li>Upper right corner, coordinate axis 1</li>
     *     <li>Upper right corner, coordinate axis 2</li>
     *     <li>Maximum value, coordinate axis 3 (optional)</li>
     */
    public SystemsQueryBuilder bbox(Double... bbox) {
        return bbox(convertToList(bbox));
    }

    /**
     * Only features that have a validTime property that intersects the value of datetime are selected.
     * If history is supported for a feature type, the following also applies:
     * - If datetime is a time instant or now, only the description valid at the specified time is selected.
     * - If datetime is a time period, only the latest description valid during the period is selected.
     * - The response can never include more than one description of the same feature.
     */
    public SystemsQueryBuilder datetime(TimeExtent datetime) {
        return (SystemsQueryBuilder) addParameter("datetime", datetime);
    }

    /**
     * WKT geometry and operator to filter resources on their location or geometry.
     * Only features that have a geometry that intersects the value of geom are selected.
     * <p>
     * Examples:
     * - geom=LINESTRING((-86.53 12.45), (-86.54 12.46), (-86.55 12.47))
     * - geom=POLYGON((0 0,4 0,4 4,0 4,0 0))
     */
    public SystemsQueryBuilder geom(String geom) {
        return (SystemsQueryBuilder) addParameter("geom", geom);
    }

    /**
     * List of keywords used for full-text search.
     * Only resources that have textual fields that contain one of the specified keywords are selected.
     * The resource name and description properties are always searched.
     * It is up to the server to decide which other textual fields are searched.
     * <p>
     * Examples:
     * - q=temp
     * - q=gps,imu
     */
    public SystemsQueryBuilder q(List<String> q) {
        return (SystemsQueryBuilder) addParameter("q", q);
    }

    /**
     * List of resource local IDs or unique IDs (URI).
     * Only resources that have a parent that has one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder parent(List<String> parent) {
        return (SystemsQueryBuilder) addParameter("parent", parent);
    }

    /**
     * List of procedure local IDs or unique IDs (URI).
     * Only systems that implement a procedure that has one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder procedure(List<String> procedure) {
        return (SystemsQueryBuilder) addParameter("procedure", procedure);
    }

    /**
     * List of feature local IDs or unique IDs (URI).
     * Only resources that are associated with a feature of interest that has one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder foi(List<String> foi) {
        return (SystemsQueryBuilder) addParameter("foi", foi);
    }

    /**
     * List of property local IDs or unique IDs (URI).
     * Only resources that are associated with an observable property that has one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder observedProperty(List<String> observedProperty) {
        return (SystemsQueryBuilder) addParameter("observedProperty", observedProperty);
    }

    /**
     * List of property local IDs or unique IDs (URI).
     * Only resources that are associated with a controllable property that has one of the provided identifiers are selected.
     */
    public SystemsQueryBuilder controlledProperty(List<String> controlledProperty) {
        return (SystemsQueryBuilder) addParameter("controlledProperty", controlledProperty);
    }

    /**
     * If true, instructs the server to include subsystems in the search results.
     * <p>
     * Default: false
     */
    public SystemsQueryBuilder recursive(boolean recursive) {
        return (SystemsQueryBuilder) addParameter("recursive", recursive);
    }

    /**
     * Limits the number of items that are presented in the response document.
     * <p>
     * Default: 100
     */
    public SystemsQueryBuilder limit(int limit) {
        return (SystemsQueryBuilder) addParameter("limit", limit);
    }
}
