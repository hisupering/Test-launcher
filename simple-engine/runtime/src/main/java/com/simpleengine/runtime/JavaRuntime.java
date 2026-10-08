package com.simpleengine.runtime;

import java.io.File;

public final class JavaRuntime {
    private JavaRuntime() {}

    public static boolean isUsable(File executable) {
        return executable != null && executable.isFile() && executable.canExecute();
    }

    public static File resolve(File root, String abi) {
        return new File(root, "java/" + abi + "/bin/java");
    }
}
