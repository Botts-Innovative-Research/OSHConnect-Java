package org.sensorhub.oshconnect.net;

public enum HttpRequestMethod {
    GET, POST, PUT, DELETE;

    public static HttpRequestMethod fromString(String method) {
        return switch (method) {
            case "GET" -> GET;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "DELETE" -> DELETE;
            default -> throw new IllegalArgumentException("Invalid request method");
        };
    }
}