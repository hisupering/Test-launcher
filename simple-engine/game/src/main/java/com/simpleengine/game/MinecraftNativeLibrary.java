package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Path;

public final class MinecraftNativeLibrary {
    public final String name;
    public final String classifier;
    public final URL url;
    public final String sha1;
    public final Path archivePath;
    public final String path;

    public MinecraftNativeLibrary(String name,String classifier,URL url,String sha1,Path archivePath,String path){
        this.name=name;this.classifier=classifier;this.url=url;this.sha1=sha1;this.archivePath=archivePath;this.path=path;
    }
}
