package com.simpleengine.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinecraftAssetIndex {
    public final Map<String, String> objects;

    private MinecraftAssetIndex(Map<String, String> objects) {
        this.objects = Collections.unmodifiableMap(objects);
    }

    public static MinecraftAssetIndex parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject source = root.getAsJsonObject("objects");
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, com.google.gson.JsonElement> e : source.entrySet()) {
            JsonObject object = e.getValue().getAsJsonObject();
            if (object.has("hash")) result.put(e.getKey(), object.get("hash").getAsString());
        }
        return new MinecraftAssetIndex(result);
    }
}
