package com.simpleengine.game;

import com.simpleengine.core.EngineListener;
import com.simpleengine.core.EngineState;
import com.simpleengine.core.SimpleEngine;
import com.simpleengine.core.EngineConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftLaunchController {
    private final SimpleEngine engine;
    public MinecraftLaunchController(EngineListener listener) { engine = new SimpleEngine(listener); }

    public synchronized void launch(
            File javaExecutable,
            MinecraftInstallResult install,
            File gameDirectory,
            MinecraftLaunchContext context) {

        if (install == null || install.version == null) throw new IllegalArgumentException("install");
        if (context == null) throw new IllegalArgumentException("context");

        MinecraftClasspath cp = new MinecraftClasspath();
        for (java.nio.file.Path p : install.libraries) cp.add(p.toFile());
        cp.add(install.clientJar.toFile());

        List<String> jvm = MinecraftArgumentResolver.resolve(install.version.jvmArguments, context);
        List<String> game = MinecraftArgumentResolver.resolve(install.version.gameArguments, context);

        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(install.version.mainClass);
        commandArgs.addAll(game);

        List<String> jvmArgs = new ArrayList<>(jvm);
        jvmArgs.add("-cp");
        jvmArgs.add(cp.asString());

        EngineConfig config = new EngineConfig(
                javaExecutable,
                gameDirectory,
                context.username,
                install.version.id,
                jvmArgs,
                commandArgs);

        engine.launch(config);
    }

    public void stop() { engine.stop(); }
    public EngineState state() { return engine.getState(); }
}
