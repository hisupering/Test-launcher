package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class MinecraftVersionMetadata {
    public final String id;
    public final String mainClass;
    public final URL clientUrl;
    public final String clientSha1;
    public final Path clientPath;
    public final List<MinecraftLibrary> libraries;

    public MinecraftVersionMetadata(
            String id,
            String mainClass,
            URL clientUrl,
            String clientSha1,
            Path clientPath,
            List<MinecraftLibrary> libraries) {
        this.id = id;
        this.mainClass = mainClass;
        this.clientUrl = clientUrl;
        this.clientSha1 = clientSha1;
        this.clientPath = clientPath;
        this.libraries = Collections.unmodifiableList(libraries);
    }
}
