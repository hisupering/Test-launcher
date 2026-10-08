package com.simpleengine.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

public final class MinecraftDownloader {
    public static final String MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private MinecraftDownloader() {}

    public static Path download(URL source, Path destination, String expectedSha1)
            throws MinecraftDownloadException {
        try {
            Files.createDirectories(destination.getParent());
            HttpURLConnection connection = (HttpURLConnection) source.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "SimpleLauncher/1.0");

            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new MinecraftDownloadException("HTTP " + code + " for " + source);
            }

            Path temp = destination.resolveSibling(destination.getFileName() + ".part");
            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(temp)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            } finally {
                connection.disconnect();
            }

            if (expectedSha1 != null && !expectedSha1.isEmpty()) {
                String actual = sha1(temp);
                if (!expectedSha1.equalsIgnoreCase(actual)) {
                    Files.deleteIfExists(temp);
                    throw new MinecraftDownloadException(
                            "SHA-1 mismatch: expected " + expectedSha1 + ", got " + actual);
                }
            }

            Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (MinecraftDownloadException e) {
            throw e;
        } catch (Exception e) {
            throw new MinecraftDownloadException("Download failed: " + source, e);
        }
    }

    private static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder result = new StringBuilder();
            for (byte b : digest.digest()) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IOException("Unable to calculate SHA-1", e);
        }
    }
}
