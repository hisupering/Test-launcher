package com.simpleengine.game;

import java.io.File;

public final class MinecraftInstance {
    public final String id;
    public final String version;
    public final File directory;

    public MinecraftInstance(String id, String version, File directory) {
        this.id = id;
        this.version = version;
        this.directory = directory;
    }
}
