package com.simpleengine.game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        }

        return new MinecraftInstallResult(v, client, libs, index);
    }
}
