package com.simpleengine.game;

import java.util.Map;

public final class OperatingSystemRules {
    private OperatingSystemRules() {}

    public static boolean allowed(com.google.gson.JsonObject library, String osName, String osArch, String osVersion, Map<String, Boolean> features) {
        if (library == null || !library.has("rules")) return true;
        boolean allowed = false;
        for (com.google.gson.JsonElement element : library.getAsJsonArray("rules")) {
            com.google.gson.JsonObject rule = element.getAsJsonObject();
            boolean matches = matchesOs(rule.getAsJsonObject("os"), osName, osArch, osVersion)
                    && matchesFeatures(rule.getAsJsonObject("features"), features);
            if (matches) allowed = "allow".equals(string(rule, "action"));
        }
        return allowed;
    }

    private static boolean matchesOs(com.google.gson.JsonObject os, String name, String arch, String version) {
        if (os == null) return true;
        if (os.has("name") && !os.get("name").getAsString().equals(name)) return false;
        if (os.has("arch") && !os.get("arch").getAsString().equals(arch)) return false;
        if (os.has("version") && (version == null || !version.matches(os.get("version").getAsString()))) return false;
        return true;
    }

    private static boolean matchesFeatures(com.google.gson.JsonObject required, Map<String, Boolean> features) {
        if (required == null) return true;
        for (Map.Entry<String, com.google.gson.JsonElement> e : required.entrySet()) {
            boolean actual = features != null && Boolean.TRUE.equals(features.get(e.getKey()));
            if (actual != e.getValue().getAsBoolean()) return false;
        }
        return true;
    }

    private static String string(com.google.gson.JsonObject o, String key) {
        return o.has(key) ? o.get(key).getAsString() : null;
    }
}
