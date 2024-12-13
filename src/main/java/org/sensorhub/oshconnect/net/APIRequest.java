package org.sensorhub.oshconnect.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIRequest {
    private String url;
    private String body;
    private Map<String, String> params;
    private Map<String, String> headers;
    private String authorizationToken;

    public APIResponse get() {
        try {
            HttpURLConnection connection = buildConnection(HttpRequestMethod.GET);
            connection.connect();
            StringBuilder response = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            APIResponse apiResponse = new APIResponse(connection.getResponseCode(), connection.getResponseMessage(), response.toString());
            connection.disconnect();
            return apiResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public APIResponse post() {
        return execute2(HttpRequestMethod.POST);
    }

    public APIResponse put() {
        return execute2(HttpRequestMethod.PUT);
    }

    public APIResponse delete() {
        return execute2(HttpRequestMethod.DELETE);
    }

    private APIResponse execute2(HttpRequestMethod requestMethod) {
        try {
            HttpURLConnection connection = buildConnection(requestMethod);
            connection.connect();
            APIResponse apiResponse = new APIResponse(connection.getResponseCode(), connection.getResponseMessage(), null);
            connection.disconnect();
            return apiResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private HttpURLConnection buildConnection(HttpRequestMethod requestMethod) {
        if (url == null)
            throw new IllegalArgumentException("URL cannot be null");

        if (requestMethod == null)
            throw new IllegalArgumentException("Request method cannot be null");

        if ((requestMethod == HttpRequestMethod.POST || requestMethod == HttpRequestMethod.PUT) && body == null)
            throw new IllegalArgumentException("Body cannot be null for POST or PUT requests");

        try {
            StringBuilder urlWithParamsBuilder = new StringBuilder(url);
            if (params != null) {
                urlWithParamsBuilder.append("?");
                params.forEach((key, value) -> urlWithParamsBuilder.append(key).append("=").append(value).append("&"));
                urlWithParamsBuilder.deleteCharAt(urlWithParamsBuilder.length() - 1); // Remove the last "&"
            }

            URL urlWithParams = new URL(urlWithParamsBuilder.toString());
            HttpURLConnection connection = (HttpURLConnection) urlWithParams.openConnection();
            connection.setRequestMethod(requestMethod.name());

            if (headers != null)
                headers.forEach(connection::setRequestProperty);

            if (authorizationToken != null && !authorizationToken.isBlank())
                connection.setRequestProperty("Authorization", "Basic " + authorizationToken);

            if (requestMethod == HttpRequestMethod.POST || requestMethod == HttpRequestMethod.PUT) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            return connection;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        } catch (ProtocolException e) {
            throw new IllegalArgumentException("Invalid request method: " + requestMethod);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error connecting to URL: " + url);
        }
    }
}