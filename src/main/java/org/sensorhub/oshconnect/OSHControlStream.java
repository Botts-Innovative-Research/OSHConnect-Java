package org.sensorhub.oshconnect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.oshconnect.datamodels.ControlStreamResource;

@Getter
@RequiredArgsConstructor
public class OSHControlStream {
    private final OSHSystem parentSystem;
    private final ControlStreamResource controlStreamResource;

    public String getId() {
        return controlStreamResource.getId();
    }
}
