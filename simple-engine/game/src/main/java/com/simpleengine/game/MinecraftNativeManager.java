package com.simpleengine.game;

import java.nio.file.*;
import java.util.*;

public final class MinecraftNativeManager {
    private MinecraftNativeManager() {}

    public static Path directory(Path gameDirectory,String version,String abi){
        return gameDirectory.resolve("natives").resolve(version).resolve(abi);
    }

    public static List<Path> extract(Path gameDirectory,String version,String abi,List<Path> archives) throws Exception{
        Path out=directory(gameDirectory,version,abi);
        Files.createDirectories(out);
        List<Path> files=new ArrayList<>();
        for(Path archive:archives) files.addAll(MinecraftNativeExtractor.extract(archive,out));
        return files;
    }
}
