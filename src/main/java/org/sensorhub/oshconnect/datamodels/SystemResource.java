package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SystemResource {
    private final String type;
    private final String id;
    private final Properties properties;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
