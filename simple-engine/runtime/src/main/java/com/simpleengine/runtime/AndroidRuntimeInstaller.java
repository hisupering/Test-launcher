package com.simpleengine.runtime;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Locale;

public final class AndroidRuntimeInstaller {
    private static final String BASE =
            "https://github.com/AngelAuraMC/angelauramc-openjdk-build/releases/download/download_jre21/";
    private AndroidRuntimeInstaller() {}

    public static File ensure(File root) throws Exception {
        String abi = normalizeAbi(System.getProperty("os.arch", ""));
        File runtime = new File(root, "java/" + abi);
        File java = new File(runtime, "bin/java");
        if (java.isFile()) { java.setExecutable(true, false); return java; }

        String asset = "jre21-android-" + abi + ".tar.xz";
        String sha = shaFor(abi);
        File archive = new File(root, "downloads/" + asset);
        if (!archive.isFile() || !sha256(archive).equalsIgnoreCase(sha)) {
            archive.getParentFile().mkdirs();
            download(new URL(BASE + asset), archive);
            if (!sha256(archive).equalsIgnoreCase(sha))
                throw new IOException("JRE checksum mismatch");
        }

        File staging = new File(root, "java/.staging-" + abi);
        delete(staging);
        staging.mkdirs();
        extract(archive, staging);

        File actual = new File(staging, "bin/java");
        if (!actual.isFile()) {
            File[] dirs = staging.listFiles(File::isDirectory);
            if (dirs != null && dirs.length == 1) actual = new File(dirs[0], "bin/java");
        }
        if (!actual.isFile()) throw new IOException("JRE archive has no bin/java");

        delete(runtime);
        runtime.getParentFile().mkdirs();
        if (!actual.getParentFile().getParentFile().renameTo(runtime))
            copyTree(actual.getParentFile().getParentFile(), runtime);
        delete(staging);
        java = new File(runtime, "bin/java");
        java.setExecutable(true, false);
        return java;
    }

    private static String normalizeAbi(String arch) {
        arch = arch.toLowerCase(Locale.ROOT);
        if (arch.contains("aarch64") || arch.contains("arm64")) return "arm64";
        if (arch.contains("x86_64") || arch.contains("amd64")) return "x86_64";
        if (arch.contains("86")) return "x86";
        return "arm";
    }

    private static String shaFor(String abi) {
        switch (abi) {
            case "arm64": return "8d41ec401ee59f7722df60ed991f81ad146e130452804bfdd8a05d3436f7bbfe";
            case "x86_64": return "cb88723961f5f9ad63afa1f212eb199816c27cabfd7dc66567bde1d8fb69713b";
            case "x86": return "9b8c7d10c5f751acb3b33506593da44ece52a0fd03e0b3c283ba08a7f285a40";
            default: return "96c297487def64666e379a9a363d9955c05b1a0b091b0cf24af88359a66f394a";
        }
    }

    private static void download(URL url, File out) throws Exception {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(60000);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "SimpleLauncher/0.1");
        try (InputStream in = c.getInputStream(); OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
            byte[] buf = new byte[1024 * 256]; int n;
            while ((n = in.read(buf)) >= 0) { if (n > 0) os.write(buf, 0, n); }
        } finally { c.disconnect(); }
    }

    private static String sha256(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            byte[] b = new byte[1024 * 128]; int n;
            while ((n = in.read(b)) > 0) md.update(b, 0, n);
        }
        StringBuilder s = new StringBuilder();
        for (byte b : md.digest()) s.append(String.format(Locale.ROOT, "%02x", b));
        return s.toString();
    }

    private static void extract(File archive, File out) throws Exception {
        try (InputStream raw = new BufferedInputStream(new FileInputStream(archive));
             XZCompressorInputStream xz = new XZCompressorInputStream(raw);
             TarArchiveInputStream tar = new TarArchiveInputStream(xz)) {
            TarArchiveEntry e;
            while ((e = tar.getNextTarEntry()) != null) {
                Path target = out.toPath().resolve(e.getName()).normalize();
                if (!target.startsWith(out.toPath().normalize())) throw new IOException("Unsafe JRE path");
                if (e.isDirectory()) Files.createDirectories(target);
                else {
                    Files.createDirectories(target.getParent());
                    try (OutputStream os = Files.newOutputStream(target)) {
                        byte[] b = new byte[1024 * 128]; int n;
                        while ((n = tar.read(b)) > 0) os.write(b, 0, n);
                    }
                    target.toFile().setExecutable(true, false);
                }
            }
        }
    }

    private static void copyTree(File from, File to) throws IOException {
        Files.walk(from.toPath()).forEach(p -> {
            try {
                Path rel = from.toPath().relativize(p);
                Path dest = to.toPath().resolve(rel);
                if (Files.isDirectory(p)) Files.createDirectories(dest);
                else { Files.createDirectories(dest.getParent()); Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING); dest.toFile().setExecutable(p.toFile().canExecute(), false); }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }

    private static void delete(File f) throws IOException {
        if (!f.exists()) return;
        Files.walk(f.toPath()).sorted((a,b)->b.compareTo(a)).forEach(p -> {
            try { Files.deleteIfExists(p); } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
}
