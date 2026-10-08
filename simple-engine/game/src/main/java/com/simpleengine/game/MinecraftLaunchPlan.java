package com.simpleengine.game;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class MinecraftLaunchPlan {
    public final String mainClass;
    public final File gameDirectory;
    public final MinecraftClasspath classpath;
    public final List<String> jvmArguments;
    public final List<String> gameArguments;

    public MinecraftLaunchPlan(
            String mainClass,
            File gameDirectory,
            MinecraftClasspath classpath,
            List<String> jvmArguments,
            List<String> gameArguments) {
        this.mainClass = mainClass;
        this.gameDirectory = gameDirectory;
        this.classpath = classpath;
        this.jvmArguments = Collections.unmodifiableList(jvmArguments);
        this.gameArguments = Collections.unmodifiableList(gameArguments);
    }
}
