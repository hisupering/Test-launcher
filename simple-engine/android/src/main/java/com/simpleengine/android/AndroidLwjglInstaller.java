package com.simpleengine.android;

import android.content.Context;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class AndroidLwjglInstaller {
    private AndroidLwjglInstaller() {}

    public static final class Bundle {
        public final List<File> jars;
        public final File natives;
        Bundle(List<File> jars, File natives) { this.jars = jars; this.natives = natives; }
    }

    public static Bundle ensure(Context context, File root) throws IOException {
        String abi = android.os.Build.SUPPORTED_ABIS.length == 0 ? "arm64" : android.os.Build.SUPPORTED_ABIS[0];
        if (!"arm64-v8a".equals(abi)) {
            throw new IOException("Simple Launcher Android LWJGL bridge currently targets arm64-v8a");
        }
        File out = new File(root, "lwjgl/android/" + abi);
        File jarsDir = new File(out, "jars");
        File natives = new File(out, "natives");
        if (!hasJars(jarsDir) || !hasNative(natives)) {
            delete(out);
            jarsDir.mkdirs();
            natives.mkdirs();
            copyAssetTree(context, "simple-lwjgl/" + "arm64/jars", jarsDir);
            copyAssetTree(context, "simple-lwjgl/" + "arm64/natives", natives);
        }
        if (!hasJars(jarsDir) || !hasNative(natives)) {
            throw new IOException("Android LWJGL bridge is not packaged in this APK");
        }
        List<File> jars = new ArrayList<>();
        File[] fs = jarsDir.listFiles((d,n)->n.endsWith(".jar") && !n.endsWith("-sources.jar") && !n.endsWith("-javadoc.jar"));
        if (fs != null) {
            Arrays.sort(fs, Comparator.comparing(File::getName));
            Collections.addAll(jars, fs);
        }
        return new Bundle(Collections.unmodifiableList(jars), natives);
    }

    private static boolean hasJars(File d) {
        File[] f = d.listFiles((x,n)->n.endsWith(".jar"));
        return f != null && f.length > 0;
    }

    private static boolean hasNative(File d) {
        File[] f = d.listFiles((x,n)->n.endsWith(".so"));
        return f != null && f.length > 0;
    }

    private static void copyAssetTree(Context c, String assetPath, File out) throws IOException {
        String[] children = c.getAssets().list(assetPath);
        if (children == null || children.length == 0) {
            try (InputStream in = c.getAssets().open(assetPath);
                 OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) != -1) os.write(b, 0, n);
            }
            return;
        }
        out.mkdirs();
        for (String child : children) {
            copyAssetTree(c, assetPath + "/" + child, new File(out, child));
        }
    }

    private static void delete(File f) throws IOException {
        if (!f.exists()) return;
        Files.walk(f.toPath()).sorted(Comparator.reverseOrder()).forEach(p -> {
            try { Files.deleteIfExists(p); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
}
