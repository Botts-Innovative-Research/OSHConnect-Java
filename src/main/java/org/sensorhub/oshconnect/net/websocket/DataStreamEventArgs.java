package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sensorhub.impl.service.consys.obs.ObsHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.OSHDataStream;
import org.sensorhub.oshconnect.datamodels.ObservationBindingOmJson;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.net.RequestFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
     * Returns the data as an ObservationData object or null if the data is not in JSON format.
     *
     * @return an ObservationData object.
     */
    public ObservationData getObservation() {
        if (format != RequestFormat.JSON) return null;

        ObsHandler.ObsHandlerContextData contextData = new ObsHandler.ObsHandlerContextData();
        contextData.dsInfo = dataStream.getDataStreamResource();

        ByteArrayInputStream body = new ByteArrayInputStream(data);
        var ctx = new RequestContext(body);
        ctx.setData(contextData);
        ctx.setFormat(ResourceFormat.OM_JSON);

        try {
            var binding = new ObservationBindingOmJson(ctx, null, true);
            return binding.deserialize();
        } catch (IOException e) {
            return null;
        }
    }
}
