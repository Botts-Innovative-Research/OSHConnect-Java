package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.datamodels.DatastreamResource;
import org.sensorhub.oshconnect.net.RequestFormat;

@Getter
@RequiredArgsConstructor
public class OSHDatastream {
    private final DatastreamResource datastreamResource;
    private final OSHSystem parentSystem;

    public String getId() {
        return datastreamResource.getId();
    }

    /**
     * Returns the endpoint for the observations of this datastream.
     *
     * @return the endpoint for the observations of this datastream.
     */
    public String getObservationsEndpoint() {
        return parentSystem.getParentNode().getApiEndpoint() + "/datastreams/" + getId() + "/observations";
    }

    /**
     * Returns the endpoint for the observations of this datastream in the specified format.
     *
     * @param format the format of the request.
     * @return the endpoint for the observations of this datastream.
     */
    public String getObservationsEndpoint(RequestFormat format) {
        return getObservationsEndpoint() + "?format=" + format.getFormat();
    }

    /**
     * Returns the endpoint for the observations of this datastream in the specified format and replay speed.
     *
     * @param format      the format of the request.
     * @param replaySpeed the replay speed of the request.
     * @return the endpoint for the observations of this datastream.
     */
    public String getObservationsEndpoint(RequestFormat format, int replaySpeed) {
        return getObservationsEndpoint(format) + "&replaySpeed=" + replaySpeed;
    }

    /**
     * Returns the endpoint for the observations of this datastream in the specified format, replay speed, and time range.
     *
     * @param format      the format of the request.
     * @param replaySpeed the replay speed of the request.
     * @param start       the start time of the request.
     * @param end         the end time of the request.
     * @return the endpoint for the observations of this datastream.
     */
    public String getObservationsEndpoint(RequestFormat format, int replaySpeed, String start, String end) {
        return getObservationsEndpoint(format, replaySpeed) + "&phenomenonTime=" + start + "/" + end;
    }
}
