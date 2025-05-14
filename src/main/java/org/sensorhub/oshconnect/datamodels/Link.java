package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.net.URL;

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

    public Link(URL href, String rel, String type, String hreflang, String title, URI uid, URI rt, URI interfaceUri) {
        this.href = href;
        this.rel = rel;
        this.type = type;
        this.hreflang = hreflang;
        this.title = title;
        this.uid = uid;
        this.rt = rt;
        this.interfaceUri = interfaceUri;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    /**
     * URL of target resource.
     */
    public URL getHref() {
        return href;
    }

    /**
     * Link relation type.
     */
    public String getRel() {
        return rel;
    }

    /**
     * Media type of target resource.
     */
    public String getType() {
        return type;
    }

    /**
     * Language tag of target resource (2-letter language code, followed by optional 2-letter region code).
     */
    public String getHreflang() {
        return hreflang;
    }

    /**
     * Title of target resource.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Unique identifier of target resource.
     */
    public URI getUid() {
        return uid;
    }

    /**
     * Semantic type of target resource (RFC 6690).
     */
    public URI getRt() {
        return rt;
    }

    /**
     * Interface used to access target resource (RFC 6690).
     */
    public URI getInterfaceUri() {
        return interfaceUri;
    }
}
