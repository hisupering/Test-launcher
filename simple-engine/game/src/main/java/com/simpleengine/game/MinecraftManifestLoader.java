package com.simpleengine.game;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class MinecraftManifestLoader {
    private MinecraftManifestLoader() {}

    public static String loadJson(URL source) throws IOException {
        try (var in = source.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /*
     * Minimal dependency-free extraction for the top-level latest IDs.
     * Full version-object parsing is implemented behind the resolver API next.
     */
    public static MinecraftManifest parse(String json) {
        if (json == null || json.isEmpty()) throw new IllegalArgumentException("json");
        String release = extractLatest(json, "release");
        String snapshot = extractLatest(json, "snapshot");
        return new MinecraftManifest(release, snapshot, java.util.Collections.emptyList());
    }

    private static String extractLatest(String json, String key) {
        String marker = "\""+key+"\"";
        int keyPos = json.indexOf(marker);
        if (keyPos < 0) return null;
        int colon = json.indexOf(':', keyPos + marker.length());
        if (colon < 0) return null;
        int first = json.indexOf('"', colon + 1);
        int second = first < 0 ? -1 : json.indexOf('"', first + 1);
        return first >= 0 && second > first ? json.substring(first + 1, second) : null;
    }
}
