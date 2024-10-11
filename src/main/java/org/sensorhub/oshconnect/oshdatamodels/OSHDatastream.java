package org.sensorhub.oshconnect.oshdatamodels;

import org.sensorhub.oshconnect.datamodels.DatastreamResource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}
