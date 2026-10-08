package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftFileDownloader {
    private MinecraftFileDownloader() {}

    public static Path ensure(URL url, Path target, String sha1) throws MinecraftDownloadException {
        if (Files.isRegularFile(target)) {
            if (sha1 == null || sha1.isEmpty() || MinecraftChecksums.sha1(target).equalsIgnoreCase(sha1)) return target;
            Files.delete(target);
        }
        return MinecraftDownloader.download(url, target, sha1);
    }
}
