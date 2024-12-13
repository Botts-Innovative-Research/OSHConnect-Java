package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SystemResource {
    private final String type = "Feature";
    private final String id;
    private final Properties properties;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static SystemResource fromJson(String json) {
        return new Gson().fromJson(json, SystemResource.class);
    }
}
