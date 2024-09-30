/**
 * Copyright (c) 2023. Botts Innovative Research, Inc.
 * All Rights Reserved.
 */

package org.sensorhub.oshconnect.net;

import lombok.Getter;

/**
 * Enumeration of protocols used by OSH Connect.
 */
@Getter
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
}
