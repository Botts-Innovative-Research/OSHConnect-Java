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
    /**
     * URL of target resource.
     */
    private final URL href;
    /**
     * Link relation type.
     */
    private final String rel;
    /**
     * Media type of target resource.
     */
    private final String type;
    /**
     * Language tag of target resource (2-letter language code, followed by optional 2-letter region code).
     */
    private final String hreflang;
    /**
     * Title of target resource.
     */
    private final String title;
    /**
     * Unique identifier of target resource.
     */
    private final URI uid;
    /**
     * Semantic type of target resource (RFC 6690).
     */
    private final URI rt;
    /**
     * Interface used to access target resource (RFC 6690).
     */
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
