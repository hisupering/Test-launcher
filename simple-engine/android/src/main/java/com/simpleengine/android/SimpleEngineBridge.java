package com.simpleengine.android;

import com.simpleengine.runtime.JavaRuntime;
import java.io.File;

public final class SimpleEngineBridge {
    private final File root;

    public SimpleEngineBridge(File filesDir) {
        this.root = new File(filesDir, "simple-engine");
        if (!root.exists()) root.mkdirs();
    }

    public File getRoot() {
        return root;
    }

    public boolean hasJavaRuntime(String abi) {
        return JavaRuntime.isUsable(JavaRuntime.resolve(root, abi));
    }

    public void stop() {
        // Process lifecycle will be connected to SimpleEngine in the next milestone.
    }
}
