package com.simpleengine.core;

import java.io.File;

public final class EngineRuntime {
    private final File root;

    public EngineRuntime(File root) {
        if (root == null) throw new IllegalArgumentException("root");
        this.root = root;
    }

    public File root() {
        return root;
    }

    public File versionsDirectory() {
        return new File(root, "versions");
    }

    public File librariesDirectory() {
        return new File(root, "libraries");
    }

    public File assetsDirectory() {
        return new File(root, "assets");
    }

    public File instancesDirectory() {
        return new File(root, "instances");
    }

    public void prepare() {
        versionsDirectory().mkdirs();
        librariesDirectory().mkdirs();
        assetsDirectory().mkdirs();
        instancesDirectory().mkdirs();
    }
}
