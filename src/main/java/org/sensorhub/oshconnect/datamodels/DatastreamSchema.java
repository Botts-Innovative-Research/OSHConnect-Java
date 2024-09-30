package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DatastreamSchema {
    private final String obsFormat;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
