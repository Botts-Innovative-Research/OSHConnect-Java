package org.sensorhub.oshconnect.net;

import lombok.Getter;

@Getter
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
    private final String format;

    RequestFormat(String format) {
        this.format = format;
    }
}
