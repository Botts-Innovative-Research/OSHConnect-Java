package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Observation {
    @Getter
    @SerializedName("datastream@Id")
    private final String datastreamId;
    @Getter
    @SerializedName("foi@id")
    private final String foiId;
    @Getter
    private final String phenomenonTime;
    @Getter
    private final String resultTime;
    private final JsonElement result;
    @Getter
    @SerializedName("result@links")
    private final Link[] resultLinks;

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
