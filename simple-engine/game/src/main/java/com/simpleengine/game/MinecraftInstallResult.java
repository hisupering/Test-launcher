package com.simpleengine.game;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class MinecraftInstallResult {
    public final MinecraftVersionDetails version;
    public final Path clientJar;
    public final List<Path> libraries;
    public final Path assetIndex;

    public MinecraftInstallResult(MinecraftVersionDetails version, Path clientJar, List<Path> libraries, Path assetIndex) {
        this.version = version;
        this.clientJar = clientJar;
        this.libraries = Collections.unmodifiableList(libraries);
        this.assetIndex = assetIndex;
    }
}
