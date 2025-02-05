package org.sensorhub.oshconnect;

import lombok.Getter;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.ObservationData;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.util.QueryStringBuilder;
import org.sensorhub.oshconnect.util.Utilities;
import org.vast.util.TimeExtent;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Getter
public class OSHDataStream implements OSHStream {
    private final OSHSystem parentSystem;
    private final String id;
    private IDataStreamInfo dataStreamResource;

    public OSHDataStream(OSHSystem parentSystem, String id, IDataStreamInfo dataStreamResource) {
        this.parentSystem = parentSystem;
        this.id = id;
        this.dataStreamResource = dataStreamResource;
    }

    /**
     * Returns the endpoint for the observations of this data stream.
     */
    public String getEndpoint() {
        return Utilities.joinPath(parentSystem.getParentNode().getApiEndpoint(), Service.DATASTREAMS.getEndpoint(), getId(), Service.OBSERVATIONS.getEndpoint());
    }

    /**
     * Returns the observation with the specified ID.
     *
     * @param observationId The ID of the observation to get.
     * @return The observation with the specified ID.
     * @throws ExecutionException   If the execution of the request fails.
     * @throws InterruptedException If the execution of the request is interrupted.
     */
    public ObservationData getObservation(String observationId) throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().getObservation(observationId, dataStreamResource).get();
    }

    /**
     * Returns the latest observations of this data stream.
     *
     * @return A list of ObservationData objects.
     */
    public List<ObservationData> getObservations() throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().getObservations(id, dataStreamResource).get();
    }

    /**
     * Returns the latest observations of this data stream with the specified parameters.
     * Any parameter that is null or empty will be ignored.
     *
     * @param id               List of resource local IDs or unique IDs (URI).
     *                         Only resources that have one of the provided identifiers are selected.
     * @param phenomenonTime   Only resources with a phenomenonTime property that intersects the value of the phenomenonTime parameter are selected.
     * @param resultTime       Only resources with a phenomenonTime property that intersects the value of the phenomenonTime parameter are selected.
     * @param foi              List of feature local IDs or unique IDs (URI).
     *                         Only resources that are associated with a feature of interest that has one of the provided identifiers are selected.
     * @param observedProperty List of property local IDs or unique IDs (URI).
     *                         Only resources that are associated with an observable property that has one of the provided identifiers are selected.
     * @param limit            This parameter limits the number of items that are presented in the response document.
     * @return A list of ObservationData objects.
     */
    public List<ObservationData> getObservations(List<String> id, TimeExtent phenomenonTime, TimeExtent resultTime, List<String> foi, List<String> observedProperty, Integer limit) throws ExecutionException, InterruptedException {
        QueryStringBuilder queryString = QueryStringBuilder.fromMap(new HashMap<>())
                .addParameter("id", id)
                .addParameter("phenomenonTime", phenomenonTime)
                .addParameter("resultTime", resultTime)
                .addParameter("foi", foi)
                .addParameter("observedProperty", observedProperty)
                .addParameter("limit", limit);

        return getConnectedSystemsApiClientExtras().getObservations(this.id, dataStreamResource, queryString.toString()).get();
    }

    /**
     * Push an observation to this data stream.
     *
     * @param observation The observation to add.
     * @return The ID of the observation if the operation was successful, otherwise null.
     */
    public String pushObservation(ObservationData observation) throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().pushObservation(id, dataStreamResource, observation).get();
    }

    /**
     * Updates the data stream properties on the server
     * Note: After updating the data stream, the properties are refreshed from the server,
     * not set to the provided properties.
     *
     * @param dataStreamInfo The data stream info to update the data stream with.
     * @return true if the operation was successful, otherwise false.
     */
    public boolean updateDataStream(IDataStreamInfo dataStreamInfo) throws ExecutionException, InterruptedException {
        Integer response = getConnectedSystemsApiClientExtras().updateDataStream(id, dataStreamInfo).get();
        boolean success = response != null && response >= 200 && response < 300;

        if (success) {
            return refreshDataStream();
        }
        return false;
    }

    /**
     * Refreshes the data stream properties from the server.
     *
     * @return true if the operation was successful, otherwise false.
     */
    public boolean refreshDataStream() throws ExecutionException, InterruptedException {
        IDataStreamInfo response = getConnectedSystemsApiClient().getDatastreamById(id, ResourceFormat.JSON, true).get();
        boolean success = response != null;

        if (success) {
            dataStreamResource = response;
        }
        return success;
    }

    public ConSysApiClient getConnectedSystemsApiClient() {
        return parentSystem.getConnectedSystemsApiClient();
    }

    public ConSysApiClientExtras getConnectedSystemsApiClientExtras() {
        return parentSystem.getConnectedSystemsApiClientExtras();
    }
}
