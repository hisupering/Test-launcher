package com.simpleengine.core;

import java.io.*;
import java.util.*;

public final class SimpleEngine {
    private final EngineListener listener;
    private volatile EngineState state = EngineState.IDLE;
    private volatile Process process;

    public SimpleEngine(EngineListener listener) { this.listener = listener; }
    public EngineState getState() { return state; }

    public synchronized void launch(EngineConfig config) {
        if (process != null && process.isAlive()) throw new IllegalStateException("Simple Engine is already running");
        setState(EngineState.PREPARING);
        if (config == null || config.javaExecutable == null || !config.javaExecutable.isFile()) { fail(new IllegalStateException("Java runtime not found")); return; }
        if (!config.gameDirectory.exists() && !config.gameDirectory.mkdirs()) { fail(new IllegalStateException("Cannot create game directory")); return; }

        List<String> command = new ArrayList<>();
        command.add(config.javaExecutable.getAbsolutePath());
        command.addAll(config.jvmArguments);
        command.add("-Dsimple.engine=true");
        command.add("-Dsimple.version=" + config.versionId);
        command.add("-Dsimple.username=" + config.username);
        command.add("-Djava.io.tmpdir=" + new File(config.gameDirectory, "tmp").getAbsolutePath());
        command.addAll(config.gameArguments);

        setState(EngineState.STARTING);
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(config.gameDirectory);
            builder.redirectErrorStream(true);
            process = builder.start();
            final Process started = process;
            Thread output = new Thread(() -> readOutput(started), "simple-engine-output");
            output.setDaemon(true); output.start();
            Thread waiter = new Thread(() -> waitFor(started), "simple-engine-waiter");
            waiter.setDaemon(true); waiter.start();
            setState(EngineState.RUNNING);
        } catch (Throwable t) { fail(t); }
    }

    private void waitFor(Process p) {
        try {
            int exit = p.waitFor();
            if (p == process) process = null;
            if (state != EngineState.STOPPING) setState(exit == 0 ? EngineState.STOPPED : EngineState.FAILED);
            log("Java process exited with code " + exit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (state != EngineState.STOPPING) fail(e);
        }
    }

    public synchronized void stop() {
        Process p = process;
        if (p == null || !p.isAlive()) return;
        setState(EngineState.STOPPING);
        p.destroy();
        try { if (!p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) p.destroyForcibly(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        process = null;
        setState(EngineState.STOPPED);
    }

    private void readOutput(Process p) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line; while ((line = reader.readLine()) != null) log(line);
        } catch (Throwable t) { if (state != EngineState.STOPPING) fail(t); }
    }
    private void setState(EngineState next) { state = next; if (listener != null) listener.onStateChanged(next); }
    private void log(String line) { if (listener != null) listener.onLog(line); }
    private void fail(Throwable error) { setState(EngineState.FAILED); if (listener != null) listener.onError(error); }
}
