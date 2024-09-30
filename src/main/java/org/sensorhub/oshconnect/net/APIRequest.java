package org.sensorhub.oshconnect.net;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Setter
@Getter
public class APIRequest {
    private HttpRequestMethod requestMethod = HttpRequestMethod.GET;
    private String url;
    private String body;
    private Map<String, String> params;
    private Map<String, String> headers;
    private String authorizationToken;

    public <T> APIResponse<T> execute(Class<T> clazz) {
        return APIResponse.fromJson(execute(), clazz);
    }

    public String execute() {
        if (url == null)
            throw new IllegalArgumentException("URL cannot be null");

        if (requestMethod == null)
            throw new IllegalArgumentException("Request method cannot be null");

        if ((requestMethod == HttpRequestMethod.POST || requestMethod == HttpRequestMethod.PUT) && body == null)
            throw new IllegalArgumentException("Body cannot be null for POST or PUT requests");

        try {
            HttpClient client = HttpClient.newHttpClient();
            URIBuilder uriBuilder = new URIBuilder(url);

            if (params != null)
                params.forEach(uriBuilder::addParameter);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(new URI(uriBuilder.toString()));

            switch (requestMethod) {
                case GET -> requestBuilder.GET();
                case POST -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                case PUT -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
                case DELETE -> requestBuilder.DELETE();
            }

            if (headers != null)
                headers.forEach(requestBuilder::header);

            if (authorizationToken != null)
                requestBuilder.header("Authorization", "Basic " + authorizationToken);

            return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}