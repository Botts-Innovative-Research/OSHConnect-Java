package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ObservedProperty {
    private final String definition;
    private final String label;
    private final String description;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }
}
