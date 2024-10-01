package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.datamodels.DatastreamResource;

@Getter
@RequiredArgsConstructor
public class OSHDatastream {
    private final DatastreamResource datastreamResource;
    private final OSHSystem parentSystem;

    public String getId() {
        return datastreamResource.getId();
    }
}
