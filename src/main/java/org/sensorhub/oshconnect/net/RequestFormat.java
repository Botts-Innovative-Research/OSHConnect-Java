package org.sensorhub.oshconnect.net;

/**
 * Enum representing the format of the request.
 */
public enum RequestFormat {
    JSON("application/json"),
    OM_JSON("application/om%2Bjson"),
    SWE_CSV("application/swe%2Bcsv"),
    SWE_JSON("application/swe%2Bjson"),
    SWE_XML("application/swe%2Bxml"),
    SWE_BINARY("application/swe%2Bbinary"),
    PLAIN_TEXT("text/plain");

    /**
     * String representation of the format.
     */
    private final String mimeType;

    RequestFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * String representation of the format.
     */
    public String getMimeType() {
        return mimeType;
    }
}
