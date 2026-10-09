package com.simpleengine.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MinecraftInstaller {
    private MinecraftInstaller() {}

    public static MinecraftInstallResult install(
            String versionJson,
            Path versionsRoot,
            Path librariesRoot,
            Path assetsRoot) throws Exception {

        MinecraftVersionDetails v = MinecraftVersionInstaller.install(
                versionJson, versionsRoot, librariesRoot);

        Path client = MinecraftFileDownloader.ensure(
                v.clientUrl, v.clientPath, v.clientSha1);

        List<Path> libs = new ArrayList<>();
        for (MinecraftLibrary library : v.libraries) {
            libs.add(MinecraftFileDownloader.ensure(
                    library.artifactUrl, library.localPath, library.sha1));
        }

        Path index = null;
        if (v.assetIndexUrl != null) {
            index = assetsRoot.resolve("indexes").resolve(v.assetIndexId + ".json");
            Files.createDirectories(index.getParent());
            MinecraftAssetDownloader.downloadIndex(
                    v.assetIndexUrl, index, v.assetIndexSha1);
            downloadAssetObjects(index, assetsRoot);
        }

        return new MinecraftInstallResult(v, client, libs, index);
    }

    /**
     * Downloads content-addressed assets concurrently. Existing files with the expected
     * size are reused so every launch does not re-hash the entire assets directory.
     */
    private static void downloadAssetObjects(Path index, Path assetsRoot) throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(index)).getAsJsonObject();
        JsonObject objects = root.getAsJsonObject("objects");
        if (objects == null) return;

        Map<String, Long> hashes = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : objects.entrySet()) {
            JsonElement e = entry.getValue();
            if (!e.isJsonObject() || !e.getAsJsonObject().has("hash")) continue;
            JsonObject object = e.getAsJsonObject();
            String hash = object.get("hash").getAsString();
            long size = object.has("size") ? object.get("size").getAsLong() : -1L;
            hashes.put(hash, size);
        }

        int threads = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CompletionService<Void> completed = new ExecutorCompletionService<>(pool);
        int pending = 0;
        try {
            for (Map.Entry<String, Long> entry : hashes.entrySet()) {
                String hash = entry.getKey();
                long expectedSize = entry.getValue();
                Path target = MinecraftAssetDownloader.objectPath(assetsRoot, hash);
                if (Files.isRegularFile(target) && (expectedSize < 0 || Files.size(target) == expectedSize)) continue;
                completed.submit(() -> {
                    MinecraftAssetDownloader.ensureObject(assetsRoot, hash);
                    return null;
                });
                pending++;
            }

            for (int i = 0; i < pending; i++) {
                try {
                    completed.take().get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) throw (Exception) cause;
                    throw new RuntimeException(cause);
                }
            }
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
