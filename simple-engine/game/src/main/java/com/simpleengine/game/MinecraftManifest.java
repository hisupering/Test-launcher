package com.simpleengine.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinecraftManifest {
    private final String latestRelease;
    private final String latestSnapshot;
    private final List<MinecraftVersion> versions;

    public MinecraftManifest(String latestRelease, String latestSnapshot, List<MinecraftVersion> versions) {
        this.latestRelease = latestRelease;
        this.latestSnapshot = latestSnapshot;
        this.versions = Collections.unmodifiableList(new ArrayList<>(versions));
    }

    public String getLatestRelease() { return latestRelease; }
    public String getLatestSnapshot() { return latestSnapshot; }
    public List<MinecraftVersion> getVersions() { return versions; }

    public MinecraftVersion find(String id) {
        for (MinecraftVersion version : versions) {
            if (version.id.equals(id)) return version;
        }
        return null;
    }
}
