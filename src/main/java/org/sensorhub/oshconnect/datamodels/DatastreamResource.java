package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DatastreamResource {
    private final String id;
    private final String name;
    @SerializedName("system@id")
    private final String systemId;
    private final String outputName;
    private final String description;
    private final String[] validTime;
    private final String[] phenomenonTime;
    private final String[] resultTime;
    private final ObservedProperty[] observedProperties;
    private final String[] formats;
    private final String type;
    private final String resultType;
    @SerializedName("procedureLink@link")
    private final Link procedureLink;
    @SerializedName("deploymentLink@link")
    private final Link deploymentLink;
    @SerializedName("ultimateFeatureOfInterest@link")
    private final Link ultimateFeatureOfInterest;
    @SerializedName("samplingFeature@link")
    private final Link samplingFeature;
    private final String[] parameters;
    private final Link[] links;
    private final DatastreamSchema schema;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
