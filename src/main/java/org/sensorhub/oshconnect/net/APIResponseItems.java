package org.sensorhub.oshconnect.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import lombok.Getter;

@Getter
public class APIResponseItems<T> {
    private final List<T> items;

    public APIResponseItems(List<T> items) {
        this.items = items;
    }

    public static <T> APIResponseItems<T> fromJson(String json, Class<T> clazz) {
        return new Gson().fromJson(json, TypeToken.getParameterized(APIResponseItems.class, clazz).getType());
    }
}
