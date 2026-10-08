package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Path;

public final class MinecraftLibrary {
    public final String name;
    public final URL artifactUrl;
    public final String sha1;
    public final Path localPath;

    public MinecraftLibrary(String name, URL artifactUrl, String sha1, Path localPath) {
        this.name = name;
        this.artifactUrl = artifactUrl;
        this.sha1 = sha1;
        this.localPath = localPath;
    }
}
