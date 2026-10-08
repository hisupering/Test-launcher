package com.simpleengine.game;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftVersionResolver {
    private MinecraftVersionResolver() {}

    public static MinecraftLaunchPlan buildPlan(
            MinecraftVersionMetadata metadata,
            File gameDirectory,
            List<String> jvmArguments,
            List<String> gameArguments) {

        if (metadata == null) throw new IllegalArgumentException("metadata");
        if (gameDirectory == null) throw new IllegalArgumentException("gameDirectory");

        MinecraftClasspath classpath = new MinecraftClasspath();
        for (MinecraftLibrary library : metadata.libraries) {
            if (library.localPath != null) classpath.add(library.localPath.toFile());
        }
        if (metadata.clientPath != null) classpath.add(metadata.clientPath.toFile());

        return new MinecraftLaunchPlan(
                metadata.mainClass,
                gameDirectory,
                classpath,
                new ArrayList<>(jvmArguments == null ? List.of() : jvmArguments),
                new ArrayList<>(gameArguments == null ? List.of() : gameArguments));
    }
}
