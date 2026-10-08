package com.simpleengine.game;

import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftAssetInstaller {
    private MinecraftAssetInstaller() {}

    public static int installObjects(Path assetsRoot, Path indexFile) throws Exception {
        if (!Files.isRegularFile(indexFile)) throw new IllegalArgumentException("Missing asset index: " + indexFile);
        MinecraftAssetIndex index = MinecraftAssetIndex.parse(Files.readString(indexFile));
        int count = 0;
        for (String hash : index.objects.values()) {
            MinecraftAssetDownloader.ensureObject(assetsRoot, hash);
            count++;
        }
        return count;
    }
}
