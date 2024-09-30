package org.sensorhub.oshconnect.datamodels;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.URL;

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
}
