package com.simpleengine.android;

import com.simpleengine.core.EngineRuntime;
import com.simpleengine.core.EngineSession;
import com.simpleengine.runtime.JavaRuntime;
import java.io.File;
import java.util.UUID;

public final class SimpleEngineBridge {
    private final File root;
    private final EngineRuntime runtime;

    public SimpleEngineBridge(File filesDir) {
        this.root = new File(filesDir, "simple-engine");
        if (!root.exists()) root.mkdirs();
        this.runtime = new EngineRuntime(root);
        this.runtime.prepare();
    }

    public File getRoot() { return root; }
    public EngineRuntime getRuntime() { return runtime; }

    public EngineSession createSession(String username, String versionId) {
        return new EngineSession(UUID.randomUUID().toString(), username, versionId);
    }

    public boolean hasJavaRuntime(String abi) {
        return JavaRuntime.isUsable(JavaRuntime.resolve(root, abi));
    }

    public void stop() {
        // Process controller will attach here.
    }
}
