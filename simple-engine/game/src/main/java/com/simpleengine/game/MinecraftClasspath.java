package com.simpleengine.game;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinecraftClasspath {
    private final List<File> entries = new ArrayList<>();

    public MinecraftClasspath add(File file) {
        if (file != null) entries.add(file);
        return this;
    }

    public List<File> entries() {
        return Collections.unmodifiableList(entries);
    }

    public String asString() {
        StringBuilder result = new StringBuilder();
        for (File entry : entries) {
            if (result.length() > 0) result.append(File.pathSeparatorChar);
            result.append(entry.getAbsolutePath());
        }
        return result.toString();
    }
}
