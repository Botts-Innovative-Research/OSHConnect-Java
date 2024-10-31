package org.sensorhub.oshconnect.net.websocket;

import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event arguments for datastream events.
 */
@Getter
@RequiredArgsConstructor
public class DatastreamEventArgs {
    private final long timestamp;
    private final byte[] data;
    private final RequestFormat format;
    private final OSHDatastream datastream;
}
