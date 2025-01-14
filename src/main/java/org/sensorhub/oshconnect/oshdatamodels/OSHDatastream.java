package org.sensorhub.oshconnect.oshdatamodels;

import lombok.Getter;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.util.Utilities;
import org.vast.util.TimeExtent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Getter
public class OSHDatastream {
    private final OSHSystem parentSystem;
    private final String id;
    private IDataStreamInfo datastreamResource;

    public OSHDatastream(OSHSystem parentSystem, String id, IDataStreamInfo datastreamResource) {
        this.parentSystem = parentSystem;
        this.id = id;
        this.datastreamResource = datastreamResource;
    }

    /**
     * Returns the endpoint for the observations of this datastream.
     *
     * @return the endpoint for the observations of this datastream.
     */
    public String getObservationsEndpoint() {
        return Utilities.joinPath(parentSystem.getParentNode().getApiEndpoint(), Service.DATASTREAMS.getEndpoint(), getId(), Service.OBSERVATIONS.getEndpoint());
    }

    /**
     * Returns the latest observations of this datastream.
     *
     * @return A string containing the observations.
     */
    public String getObservations() {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), getObservationsEndpoint()));
        request.setAuthorizationToken(parentSystem.getParentNode().getAuthorizationToken());
        APIResponse response = request.get();
        return response.getResponseBody();
    }

    /**
     * Returns the latest observations of this datastream with the specified parameters.
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
     * @return A string containing the observations.
     */
    public String getObservations(List<String> id, TimeExtent phenomenonTime, TimeExtent resultTime, List<String> foi, List<String> observedProperty, int limit) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), getObservationsEndpoint()));
        request.setAuthorizationToken(parentSystem.getParentNode().getAuthorizationToken());

        Map<String, String> params = new HashMap<>();
        if (id != null && !id.isEmpty()) {
            params.put("id", String.join(",", id));
        }
        if (phenomenonTime != null) {
            if (phenomenonTime.isNow()) {
                params.put("phenomenonTime", "now");
            } else {
                params.put("phenomenonTime", phenomenonTime.isoStringUTC(true));
            }
        }
        if (resultTime != null) {
            if (resultTime.isNow()) {
                params.put("resultTime", "now");
            } else {
                params.put("resultTime", resultTime.isoStringUTC(true));
            }
        }
        if (foi != null && !foi.isEmpty()) {
            params.put("foi", String.join(",", foi));
        }
        if (observedProperty != null && !observedProperty.isEmpty()) {
            params.put("observedProperty", String.join(",", observedProperty));
        }
        if (limit > 0) {
            params.put("limit", Integer.toString(limit));
        }

        request.setParams(params);
        APIResponse response = request.get();
        return response.getResponseBody();
    }

    /**
     * Returns the latest observations of this datastream as a list of Observation objects.
     * Note: This method will return an empty list if the response body is not a JSON object.
     *
     * @return A list of Observation objects.
     */
    public List<Observation> getObservationsList() {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), getObservationsEndpoint()));
        request.setAuthorizationToken(parentSystem.getParentNode().getAuthorizationToken());
        APIResponse response = request.get();
        return response.getItems(Observation.class);
    }

    /**
     * Returns the latest observations of this datastream as a list of Observation objects with the specified parameters.
     * Note: This method will return an empty list if the response body is not a JSON object.
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
     * @return A list of Observation objects.
     */
    public List<Observation> getObservationsList(List<String> id, TimeExtent phenomenonTime, TimeExtent resultTime, List<String> foi, List<String> observedProperty, int limit) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), getObservationsEndpoint()));
        request.setAuthorizationToken(parentSystem.getParentNode().getAuthorizationToken());

        Map<String, String> params = new HashMap<>();
        if (id != null && !id.isEmpty()) {
            params.put("id", String.join(",", id));
        }
        if (phenomenonTime != null) {
            if (phenomenonTime.isNow()) {
                params.put("phenomenonTime", "now");
            } else {
                params.put("phenomenonTime", phenomenonTime.isoStringUTC(true));
            }
        }
        if (resultTime != null) {
            if (resultTime.isNow()) {
                params.put("resultTime", "now");
            } else {
                params.put("resultTime", resultTime.isoStringUTC(true));
            }
        }
        if (foi != null && !foi.isEmpty()) {
            params.put("foi", String.join(",", foi));
        }
        if (observedProperty != null && !observedProperty.isEmpty()) {
            params.put("observedProperty", String.join(",", observedProperty));
        }
        if (limit > 0) {
            params.put("limit", Integer.toString(limit));
        }

        request.setParams(params);
        APIResponse response = request.get();
        return response.getItems(Observation.class);
    }

    /**
     * Push an observation to this datastream.
     *
     * @param observation The observation to add.
     * @return true if the observation was added successfully, false otherwise.
     */
    public boolean pushObservation(Observation observation) {
        APIRequest request = new APIRequest();
        request.setUrl(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), getObservationsEndpoint()));
        request.setAuthorizationToken(parentSystem.getParentNode().getAuthorizationToken());
        request.setBody(observation.toJson());
        APIResponse response = request.post();
        return response.isSuccessful();
    }

    public boolean updateDatastream(IDataStreamInfo dataStreamInfo) throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClientExtras
                .newBuilder(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), parentSystem.getParentNode().getApiEndpoint()))
                .build()
                .updateDataStream(id, dataStreamInfo);
        Integer response = conSys.get();
        boolean success = response != null && response >= 200 && response < 300;

        if (success) {
            return refreshDatastream();
        }
        return false;
    }

    public boolean refreshDatastream() throws ExecutionException, InterruptedException {
        var conSys = ConSysApiClient
                .newBuilder(Utilities.joinPath(parentSystem.getParentNode().getHTTPPrefix(), parentSystem.getParentNode().getApiEndpoint()))
                .build()
                .getDatastreamById(id, ResourceFormat.JSON, true);
        IDataStreamInfo response = conSys.get();
        boolean success = response != null;

        if (success) {
            datastreamResource = response;
        }
        return success;
    }
}
