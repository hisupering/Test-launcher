package com.simpleengine.game;

import java.io.File;

public final class MinecraftClientFiles {
    private MinecraftClientFiles() {}

    public static File clientJar(File versionsDirectory, String version) {
        return new File(new File(versionsDirectory, version), version + ".jar");
    }

    public static File versionJson(File versionsDirectory, String version) {
        return new File(new File(versionsDirectory, version), version + ".json");
    }

    public static void prepareVersionDirectory(File versionsDirectory, String version) {
        if (version == null || version.isEmpty()) throw new IllegalArgumentException("version");
        new File(versionsDirectory, version).mkdirs();
    }
}
