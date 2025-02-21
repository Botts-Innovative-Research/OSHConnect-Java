/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */

package org.sensorhub.oshconnect.net;

/**
 * Enumeration of protocols used by OSH Connect.
 */
public enum Protocol {
    HTTP("http://"),
    HTTPS("https://"),
    WS("ws://"),
    WSS("wss://");

    /**
     * String representation of the protocol's prefix.
     */
    private final String prefix;

    /**
     * Constructor
     *
     * @param prefix String representation of the protocol's prefix.
     */
    Protocol(String prefix) {
        this.prefix = prefix;
    }

    /**
     * String representation of the protocol's prefix.
     */
    public String getPrefix() {
        return prefix;
    }
}
