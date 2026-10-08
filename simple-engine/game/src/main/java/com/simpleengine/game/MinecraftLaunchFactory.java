package com.simpleengine.game;

import java.io.File;
import java.util.ArrayList;

public final class MinecraftLaunchFactory {
    private MinecraftLaunchFactory() {}

    public static MinecraftLaunchPlan create(MinecraftInstallResult install, File gameDirectory) {
        MinecraftClasspath cp = new MinecraftClasspath();
        for (java.nio.file.Path p : install.libraries) cp.add(p.toFile());
        cp.add(install.clientJar.toFile());

        MinecraftArguments vars = MinecraftCommandLine.defaults(
                "Player", gameDirectory, install.version.id, cp.asString());

        return new MinecraftLaunchPlan(
                install.version.mainClass,
                gameDirectory,
                cp,
                MinecraftCommandLine.resolve(install.version.jvmArguments, vars),
                MinecraftCommandLine.resolve(install.version.gameArguments, vars));
    }
}
