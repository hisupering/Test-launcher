package com.simpleengine.game;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinecraftArguments {
    private final Map<String, String> values = new LinkedHashMap<>();

    public MinecraftArguments put(String key, String value) {
        if (key != null && value != null) values.put(key, value);
        return this;
    }

    public String resolve(String value) {
        if (value == null) return null;
        String result = value;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace(" + entry.getKey() + ", entry.getValue());
        }
        return result;
    }

    public File resolveFile(String value) {
        String resolved = resolve(value);
        return resolved == null ? null : new File(resolved);
    }
}
