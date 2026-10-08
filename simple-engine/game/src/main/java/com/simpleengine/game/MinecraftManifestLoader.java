package com.simpleengine.game;

import com.google.gson.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftManifestLoader {
    private MinecraftManifestLoader() {}

    public static String loadJson(URL source) throws IOException {
        try (var in = source.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static MinecraftManifest parse(String json) {
        if (json == null || json.isEmpty()) throw new IllegalArgumentException("json");
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject latest = root.getAsJsonObject("latest");
        String release = latest == null ? null : str(latest,"release");
        String snapshot = latest == null ? null : str(latest,"snapshot");
        List<MinecraftVersion> versions = new ArrayList<>();
        JsonArray list = root.getAsJsonArray("versions");
        if (list != null) for (JsonElement e : list) {
            JsonObject v=e.getAsJsonObject();
            String id=str(v,"id"), type=str(v,"type"), url=str(v,"url"), sha1=str(v,"sha1");
            if(id!=null) {
                try { versions.add(new MinecraftVersion(id,type,url==null?null:url,sha1,null)); }
                catch(Exception ignored) {}
            }
        }
        return new MinecraftManifest(release,snapshot,versions);
    }

    private static String str(JsonObject o,String k) {
        return o!=null&&o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():null;
    }
}
