package org.sensorhub.oshconnect.net.websocket;

import org.sensorhub.oshconnect.datamodels.Observation;
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

    /**
     * Returns the data as an Observation object or null if the data is not in JSON format.
     *
     * @return an Observation object.
     */
    public Observation getObservation() {
        if (format == RequestFormat.JSON) {
            return Observation.fromJson(data);
        }
        return null;
    }
}
