package org.sensorhub.oshconnect.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.util.List;

@Getter
public class APIResponse<T> {
    private final List<T> items;

    public APIResponse(List<T> items) {
        this.items = items;
    }

    public static <T> APIResponse<T> fromJson(String json, Class<T> clazz) {
        return new Gson().fromJson(json, TypeToken.getParameterized(APIResponse.class, clazz).getType());
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "APIResponse{" +
                "items=" + items +
                '}';
    }
}
