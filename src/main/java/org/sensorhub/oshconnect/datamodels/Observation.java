package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Observation {
    /**
     * Local ID of the observation.
     */
    @Getter
    private final String id;
    /**
     * Local ID of the datastream that the observation is part of.
     */
    @Getter
    @SerializedName("datastream@id")
    private final String datastreamId;
    /**
     * Local ID of the sampling feature that is the target of the observation.
     */
    @Getter
    @SerializedName("samplingFeature@id")
    private final String samplingFeatureId;
    /**
     * Link to the procedure/method used to make the observation.
     */
    @Getter
    @SerializedName("procedure@link")
    private final Link procedureLink;
    /**
     * Time at which the observation result is a valid estimate of the sampling feature property(ies).
     * Defaults to the same value as {@link #resultTime}.
     */
    @Getter
    private final String phenomenonTime;
    /**
     * Time at which the observation result was generated.
     */
    @Getter
    private final String resultTime;
    /**
     * Result of the observation.
     * Must be valid according to the result schema provided in the datastream metadata.
     */
    private final JsonElement result;
    /**
     * Link to external result data (e.g. large raster dataset served by a tiling service).
     */
    @Getter
    @SerializedName("result@link")
    private final Link resultLink;
    /**
     * Links to related resources.
     */
    @Getter
    private final List<Link> links;

    public String getResult() {
        return result.toString();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static Observation fromJson(String json) {
        return new Gson().fromJson(json, Observation.class);
    }

    public static Observation fromJson(byte[] json) {
        return fromJson(new String(json));
    }
}
