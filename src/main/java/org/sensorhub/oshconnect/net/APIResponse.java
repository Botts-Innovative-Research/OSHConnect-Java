package org.sensorhub.oshconnect.net;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

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
        //check that the first character of the response body is a '{' to avoid a JsonSyntaxException
        if (responseBody.charAt(0) != '{') {
            return null;
        }
        return new Gson().fromJson(responseBody, clazz);
    }

    /**
     * Returns the response body as a list of the specified type.
     */
    public <T> List<T> getItems(Class<T> clazz) {
        if (responseBody.charAt(0) != '{') {
            return Collections.emptyList();
        }
        APIResponseItems<T> apiResponse = APIResponseItems.fromJson(responseBody, clazz);
        return apiResponse.getItems();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getResponseBody() {
        return responseBody;
    }
}