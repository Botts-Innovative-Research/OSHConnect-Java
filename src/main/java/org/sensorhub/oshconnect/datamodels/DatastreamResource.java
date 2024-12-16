package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DatastreamResource {
    /**
     * Local resource ID. If set on creation, the server may ignore it.
     */
    private final String id;
    /**
     * Human readable name of the resource.
     */
    private final String name;
    /**
     * Human readable description of the resource.
     */
    private final String description;
    /**
     * Validity period of the resource.
     */
    private final List<String> validTime;
    /**
     * List of available formats.
     */
    private final List<String> formats;
    /**
     * ID of the system producing the observations.
     */
    @SerializedName("system@id")
    private final String systemId;
    /**
     * Link to the system producing the observations.
     */
    @SerializedName("system@link")
    private final Link systemLink;
    /**
     * Name of the system output feeding this datastream.
     */
    private final String outputName;
    /**
     * Link to the procedure used to acquire observations
     * (only provided if all observations in the datastream share the same procedure).
     */
    @SerializedName("procedureLink@link")
    private final Link procedureLink;
    /**
     * Link to the deployment during which the observations are/were collected
     * (only provided if all observations in the datastream share the same deployment).
     */
    @SerializedName("deploymentLink@link")
    private final Link deploymentLink;
    /**
     * Link to the ultimate feature of interest
     * (only provided if all observations in the datastream share the same feature of interest).
     */
    @SerializedName("featureOfInterest@link")
    private final Link featureOfInterestLink;
    /**
     * Link to the sampling feature
     * (only provided if all observations in the datastream share the same sampling feature)
     */
    @SerializedName("samplingFeature@link")
    private final Link samplingFeature;
    private final List<ObservedProperty> observedProperties;
    private final List<String> phenomenonTime;
    /**
     * An indication of how often feature of interest properties are observed.
     */
    private final String phenomenonTimeInterval;
    private final List<String> resultTime;
    /**
     * An indication of how often observation results are produced.
     */
    private final String resultTimeInterval;
    private final String type;
    private final String resultType;
    /**
     * Flag indicating if the datastream is currently streaming data.
     */
    private final Boolean live;
    /**
     * Other links to related resources.
     */
    private final List<Link> links;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
