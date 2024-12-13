package org.sensorhub.oshconnect.net;

import com.google.gson.Gson;

import java.util.List;

import lombok.Getter;

@Getter
public class APIResponse {
    private final int responseCode;
    private final String responseMessage;
    private final String responseBody;

    public APIResponse(int responseCode, String responseMessage, String responseBody) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseBody = responseBody;
    }

    /**
     * Returns true if the response code is in the 200 range.
     */
    public boolean isSuccessful() {
        return responseCode >= 200 && responseCode < 300;
    }

    /**
     * Returns the response body as the specified type.
     */
    public <T> T getItem(Class<T> clazz) {
        return new Gson().fromJson(responseBody, clazz);
    }

    /**
     * Returns the response body as a list of the specified type.
     */
    public <T> List<T> getItems(Class<T> clazz) {
        APIResponseItems<T> apiResponse = APIResponseItems.fromJson(responseBody, clazz);
        return apiResponse.getItems();
    }
}