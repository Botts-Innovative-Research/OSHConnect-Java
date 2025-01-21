package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.OSHDataStream;
import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.RequestFormat;

/**
 * Event arguments for data stream events.
 */
@Getter
@RequiredArgsConstructor
public class DataStreamEventArgs {
    private final long timestamp;
    private final byte[] data;
    private final RequestFormat format;
    private final OSHDataStream dataStream;

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
