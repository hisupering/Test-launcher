package com.simpleengine.game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftCommandBuilder {
    private MinecraftCommandBuilder() {}

    public static List<String> build(
            File javaExecutable,
            MinecraftLaunchPlan plan,
            String username) {

        if (javaExecutable == null) throw new IllegalArgumentException("javaExecutable");
        if (plan == null) throw new IllegalArgumentException("plan");

        List<String> command = new ArrayList<>();
        command.add(javaExecutable.getAbsolutePath());
        command.addAll(plan.jvmArguments);

        command.add("-cp");
        command.add(plan.classpath.asString());

        command.add(plan.mainClass);
        command.add("--username");
        command.add(username == null || username.isEmpty() ? "Player" : username);
        command.add("--gameDir");
        command.add(plan.gameDirectory.getAbsolutePath());
        command.addAll(plan.gameArguments);

        return command;
    }
}
