package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.sensorhub.oshconnect.net.APIRequest;
import org.sensorhub.oshconnect.net.APIResponse;
import org.sensorhub.oshconnect.net.HttpRequestMethod;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class System {
    private final String type;
    private final String id;
    private final Properties properties;
    @Setter
    private transient Node parentNode;

    public String toJSON() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJSON();
    }

    public String getDataStreamsEndpoint() {
        return parentNode.getSystemsEndpoint() + "/" + id + "/datastreams";
    }

    public List<Datastream> discoverDataStreams() {
        APIRequest request = new APIRequest();
        request.setRequestMethod(HttpRequestMethod.GET);
        request.setUrl(parentNode.getHTTPPrefix() + getDataStreamsEndpoint());
        if (parentNode.getAuthorizationToken() != null) {
            request.setAuthorizationToken(parentNode.getAuthorizationToken());
        }

        APIResponse<Datastream> response = request.execute(Datastream.class);
        List<Datastream> datastreams = response.getItems();
        datastreams.forEach(datastream -> datastream.setParentSystem(this));
        return datastreams;
    }
}
