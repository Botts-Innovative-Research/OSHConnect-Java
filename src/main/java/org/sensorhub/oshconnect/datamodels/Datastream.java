package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Datastream {
    private final String id;
    private final String name;
    @SerializedName("system@id")
    private final String systemId;
    private final String outputName;
    private final String[] validTime;
    private final String[] phenomenonTime;
    private final String[] resultTime;
    private final ObservedProperty[] observedProperties;
    private final String[] formats;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
