package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftAssetDownloader {
    private MinecraftAssetDownloader() {}

    public static Path downloadIndex(URL url, Path target, String sha1) throws Exception {
        return MinecraftFileDownloader.ensure(url, target, sha1);
    }

    public static Path objectPath(Path assetsRoot, String hash) {
        return assetsRoot.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
    }

    public static URL objectUrl(String hash) throws Exception {
        return new URL("https://resources.download.minecraft.net/"
                + hash.substring(0, 2) + "/" + hash);
    }

    public static Path ensureObject(Path assetsRoot, String hash) throws Exception {
        Path target = objectPath(assetsRoot, hash);
        Files.createDirectories(target.getParent());
        return MinecraftFileDownloader.ensure(objectUrl(hash), target, hash);
    }
}
