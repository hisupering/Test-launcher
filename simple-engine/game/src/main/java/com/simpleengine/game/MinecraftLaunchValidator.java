package com.simpleengine.game;

import java.io.File;

public final class MinecraftLaunchValidator {
    private MinecraftLaunchValidator() {}

    public static void validate(MinecraftLaunchPlan plan) {
        if (plan == null) throw new IllegalArgumentException("plan");
        if (plan.mainClass == null || plan.mainClass.isEmpty()) {
            throw new IllegalArgumentException("Minecraft main class is missing");
        }
        if (plan.gameDirectory == null) {
            throw new IllegalArgumentException("Game directory is missing");
        }
        if (!plan.gameDirectory.exists() && !plan.gameDirectory.mkdirs()) {
            throw new IllegalArgumentException("Unable to create game directory");
        }

        for (File entry : plan.classpath.entries()) {
            if (!entry.isFile()) {
                throw new IllegalArgumentException("Missing classpath file: " + entry);
            }
        }
    }
}
