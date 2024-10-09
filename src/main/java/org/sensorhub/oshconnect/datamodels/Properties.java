package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Properties {
    private final String uid;
    private final String featureType;
    private final String name;
    private final String description;
    private final String[] validTime;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
