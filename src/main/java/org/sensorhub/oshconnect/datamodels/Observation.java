package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Observation {
    @SerializedName("datastream@Id")
    private final String datastreamId;
    @SerializedName("foi@id")
    private final String foiId;
    private final String phenomenonTime;
    private final String resultTime;
    private final JsonElement result;
    @SerializedName("result@links")
    private final Link[] resultLinks;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
