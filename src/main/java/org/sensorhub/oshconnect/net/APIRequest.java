package org.sensorhub.oshconnect.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

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
            StringBuilder urlWithParams = new StringBuilder(url);
            if (params != null) {
                urlWithParams.append("?");
                params.forEach((key, value) -> urlWithParams.append(key).append("=").append(value).append("&"));
                urlWithParams.deleteCharAt(urlWithParams.length() - 1); // Remove the last "&"
            }

            URL url = new URL(urlWithParams.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod.name());

            if (headers != null)
                headers.forEach(connection::setRequestProperty);

            if (authorizationToken != null)
                connection.setRequestProperty("Authorization", "Basic " + authorizationToken);

            if (requestMethod == HttpRequestMethod.POST || requestMethod == HttpRequestMethod.PUT) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            connection.connect();
            StringBuilder response = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            connection.disconnect();

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}