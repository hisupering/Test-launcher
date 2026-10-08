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
        launch(javaExecutable, install, gameDirectory, context, 1024);
    }

    public synchronized void launch(
            File javaExecutable,
            MinecraftInstallResult install,
            File gameDirectory,
            MinecraftLaunchContext context,
            int ramMb) {

        if (install == null || install.version == null) throw new IllegalArgumentException("install");
        if (context == null) throw new IllegalArgumentException("context");
        if (javaExecutable == null || !javaExecutable.isFile())
            throw new IllegalArgumentException("Java runtime is missing");

        MinecraftClasspath cp = new MinecraftClasspath();
        for (java.nio.file.Path p : install.libraries) cp.add(p.toFile());
        cp.add(install.clientJar.toFile());

        List<String> jvm = new ArrayList<>(MinecraftArgumentResolver.resolve(install.version.jvmArguments, context));
        jvm.add("-Xmx" + Math.max(512, ramMb) + "M");
        jvm.addAll(MinecraftLwjgl.jvmNativeProperties(context.nativesDirectory));

        List<String> game = MinecraftArgumentResolver.resolve(install.version.gameArguments, context);
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(install.version.mainClass);
        commandArgs.addAll(game);

        jvm.add("-cp");
        jvm.add(cp.asString());

        EngineConfig config = new EngineConfig(
                javaExecutable,
                gameDirectory,
                context.username,
                install.version.id,
                jvm,
                commandArgs);

        engine.launch(config);
    }

    public void stop() { engine.stop(); }
    public EngineState state() { return engine.getState(); }
}
