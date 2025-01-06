package org.sensorhub.oshconnect.oshdatamodels;

import org.sensorhub.oshconnect.datamodels.ControlStreamResource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OSHControlStream {
    private final ControlStreamResource controlStreamResource;
    private final OSHSystem parentSystem;

    public String getId() {
        return controlStreamResource.getId();
    }
}
