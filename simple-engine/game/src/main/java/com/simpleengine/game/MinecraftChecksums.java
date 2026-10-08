package com.simpleengine.game;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public final class MinecraftChecksums {
    private MinecraftChecksums() {}

    public static String sha1(Path file) throws MinecraftDownloadException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] b = new byte[65536];
                int n;
                while ((n = in.read(b)) >= 0) digest.update(b, 0, n);
            }
            StringBuilder s = new StringBuilder();
            for (byte b : digest.digest()) s.append(String.format("%02x", b));
            return s.toString();
        } catch (Exception e) {
            throw new MinecraftDownloadException("SHA-1 failed: " + file, e);
        }
    }
}
