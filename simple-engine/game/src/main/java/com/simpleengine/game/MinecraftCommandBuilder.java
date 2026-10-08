package com.simpleengine.game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftCommandBuilder {
    private MinecraftCommandBuilder() {}

    public static List<String> build(
            File javaExecutable,
            MinecraftLaunchPlan plan,
            String username,
            String version) {

        if (javaExecutable == null) throw new IllegalArgumentException("javaExecutable");
        MinecraftLaunchValidator.validate(plan);

        List<String> command = new ArrayList<>();
        command.add(javaExecutable.getAbsolutePath());
        command.addAll(plan.jvmArguments);
        command.add("-cp");
        command.add(plan.classpath.asString());
        command.add(plan.mainClass);

        MinecraftArguments variables = MinecraftCommandLine.defaults(
                username,
                plan.gameDirectory,
                version,
                plan.classpath.asString());

        command.add("--username");
        command.add(username == null || username.isEmpty() ? "Player" : username);
        command.add("--gameDir");
        command.add(plan.gameDirectory.getAbsolutePath());

        command.addAll(MinecraftCommandLine.resolve(plan.gameArguments, variables));
        return command;
    }
}
