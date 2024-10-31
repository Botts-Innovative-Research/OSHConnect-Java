package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.net.URL;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Link {
    private final URL href;
    private final String rel;
    private final String type;
    private final String hreflang;
    private final String title;
    private final URI uid;
    private final URI rt;
    @SerializedName("if")
    private final URI interfaceUri;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
