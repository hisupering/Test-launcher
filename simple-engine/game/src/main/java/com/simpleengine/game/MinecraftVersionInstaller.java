package com.simpleengine.game;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftVersionInstaller {
    private MinecraftVersionInstaller() {}

    public static MinecraftVersionDetails install(
            String versionJson,
            Path versionsRoot,
            Path librariesRoot) throws Exception {

        var root = com.google.gson.JsonParser.parseString(versionJson).getAsJsonObject();
        String id = root.get("id").getAsString();
        Path versionDir = versionsRoot.resolve(id);
        Files.createDirectories(versionDir);

        Path jsonFile = versionDir.resolve(id + ".json");
        Files.write(jsonFile, versionJson.getBytes(StandardCharsets.UTF_8));

        return MinecraftVersionJsonParser.parse(
                versionJson,
                librariesRoot,
                versionDir);
    }
}
