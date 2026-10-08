package com.simpleengine.game;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftManifestLoader {
    private MinecraftManifestLoader() {}

    public static String loadJson(URL source) throws IOException {
        try (var in = source.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /*
     * The parser is deliberately kept behind this small API.
     * The Android module can provide a JSON implementation without
     * coupling the core engine to a launcher framework.
     */
    public static MinecraftManifest parse(String json) {
        if (json == null || json.isEmpty()) throw new IllegalArgumentException("json");
        String release = extract(json, "\"release\"", "\"id\"", 0);
        String snapshot = extract(json, "\"snapshot\"", "\"id\"", 0);
        return new MinecraftManifest(
                release,
                snapshot,
                new ArrayList<>()
        );
    }

    private static String extract(String json, String key, String nestedKey, int from) {
        int keyPos = json.indexOf(key, from);
        if (keyPos < 0) return null;
        int idPos = json.indexOf(nestedKey, keyPos);
        if (idPos < 0) return null;
        int colon = json.indexOf(':', idPos);
        int first = json.indexOf('"', colon + 1);
        int second = json.indexOf('"', first + 1);
        return first >= 0 && second > first ? json.substring(first + 1, second) : null;
    }
}
