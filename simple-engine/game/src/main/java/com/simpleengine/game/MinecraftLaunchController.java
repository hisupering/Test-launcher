package com.simpleengine.game;

import com.simpleengine.core.EngineConfig;
import com.simpleengine.core.EngineListener;
import com.simpleengine.core.EngineState;
import com.simpleengine.core.SimpleEngine;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinecraftLaunchController {
    private final SimpleEngine engine;

    public MinecraftLaunchController(EngineListener listener) {
        engine = new SimpleEngine(listener);
    }

    public synchronized void launch(File javaExecutable, MinecraftInstallResult install,
                                    File gameDirectory, MinecraftLaunchContext context) {
        launch(javaExecutable, install, gameDirectory, context, 1024, Collections.emptyList());
    }

    public synchronized void launch(File javaExecutable, MinecraftInstallResult install,
                                    File gameDirectory, MinecraftLaunchContext context,
                                    int ramMb) {
        launch(javaExecutable, install, gameDirectory, context, ramMb, Collections.emptyList());
    }

    public synchronized void launch(File javaExecutable, MinecraftInstallResult install,
                                    File gameDirectory, MinecraftLaunchContext context,
                                    int ramMb, List<File> bundledLwjglJars) {
        if (install == null || install.version == null) throw new IllegalArgumentException("install");
        if (context == null) throw new IllegalArgumentException("context");
        if (javaExecutable == null || !javaExecutable.isFile())
            throw new IllegalArgumentException("Java runtime is missing");

        MinecraftClasspath cp = new MinecraftClasspath();
        if (bundledLwjglJars != null) {
            for (File f : bundledLwjglJars) {
                if (f != null && f.isFile()) cp.add(f);
            }
        }

        for (java.nio.file.Path p : install.libraries) {
            String s = p.toString().replace('\\\\', '/');
            if (s.contains("/org/lwjgl/")) continue;
            cp.add(p.toFile());
        }
        cp.add(install.clientJar.toFile());

        List<String> jvm = new ArrayList<>(
                MinecraftArgumentResolver.resolve(install.version.jvmArguments, context));
        jvm.add("-Xmx" + Math.max(512, ramMb) + "M");
        jvm.addAll(MinecraftLwjgl.jvmNativeProperties(context.nativesDirectory));
        jvm.add("-Dsimple.native.path=" + context.nativesDirectory.getAbsolutePath());

        List<String> game = MinecraftArgumentResolver.resolve(install.version.gameArguments, context);
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(install.version.mainClass);
        commandArgs.addAll(game);

        jvm.add("-cp");
        jvm.add(cp.asString());

        EngineConfig config = new EngineConfig(
                javaExecutable, gameDirectory, context.username,
                install.version.id, jvm, commandArgs);
        engine.launch(config);
    }

    public void stop() { engine.stop(); }
    public EngineState state() { return engine.getState(); }
}
