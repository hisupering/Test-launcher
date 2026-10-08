package com.simpleengine.game;

import java.util.Collections;
import java.util.Map;

public final class MinecraftVersion {
    public final String id;
    public final String type;
    public final String url;
    public final String sha1;
    public final Map<String, Object> metadata;

    public MinecraftVersion(String id, String type, String url, String sha1, Map<String, Object> metadata) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id");
        this.id = id;
        this.type = type == null ? "release" : type;
        this.url = url;
        this.sha1 = sha1;
        this.metadata = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }
}
