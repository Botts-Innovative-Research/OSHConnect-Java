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

public class APIRequest {
    @Setter
    @Getter
    private HttpRequestMethod requestMethod = HttpRequestMethod.GET;
    @Setter
    @Getter
    private String url;
    @Setter
    @Getter
    private String body;
    @Setter
    @Getter
    private Map<String, String> params;
    @Setter
    @Getter
    private Map<String, String> headers;
    @Setter
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

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(uriBuilder.toString()));

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

            System.out.println(requestBuilder.build().uri());
            String response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
            System.out.println(response);

            return response;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}