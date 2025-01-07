package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Properties {
    /**
     * Feature type identifier, either a URI, a CURIE, or a simple token.
     */
    private final String featureType;
    /**
     * Globally unique identifier of the feature.
     */
    private final String uid;
    /**
     * Human-readable name of the feature.
     */
    private final String name;
    /**
     * Human-readable description of the feature.
     */
    private final String description;
    /**
     * Type of asset represented by this system.
     */
    private final String assetType;
    /**
     * Time period during which the system description is valid.
     */
    private final List<String> validTime;
    /**
     * Link to the system kind description (i.e., its nature or specifications).
     */
    @SerializedName("systemKind@link")
    private final Link systemKindLink;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
