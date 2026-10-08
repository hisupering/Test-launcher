package com.simpleengine.core;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class EngineConfig {
    public final File javaExecutable;
    public final File gameDirectory;
    public final String username;
    public final String versionId;
    public final List<String> jvmArguments;
    public final List<String> gameArguments;

    public EngineConfig(
            File javaExecutable,
            File gameDirectory,
            String username,
            String versionId,
            List<String> jvmArguments,
            List<String> gameArguments) {
        if (javaExecutable == null) throw new IllegalArgumentException("javaExecutable");
        if (gameDirectory == null) throw new IllegalArgumentException("gameDirectory");
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username");
        }
        if (versionId == null || versionId.trim().isEmpty()) {
            throw new IllegalArgumentException("versionId");
        }
        this.javaExecutable = javaExecutable;
        this.gameDirectory = gameDirectory;
        this.username = username;
        this.versionId = versionId;
        this.jvmArguments = jvmArguments == null
                ? Collections.emptyList() : Collections.unmodifiableList(jvmArguments);
        this.gameArguments = gameArguments == null
                ? Collections.emptyList() : Collections.unmodifiableList(gameArguments);
    }
}
