package com.simpleengine.game;

import java.nio.file.*;
import java.util.*;

public final class MinecraftRuntimeFiles {
    private MinecraftRuntimeFiles(){}

    public static Path lwjglNativeDirectory(Path gameDirectory,String version,String abi){
        return MinecraftNativeManager.directory(gameDirectory,version,abi);
    }

    public static List<String> prepareJvm(Path gameDirectory,String version,String abi){
        return MinecraftLwjgl.jvmNativeProperties(lwjglNativeDirectory(gameDirectory,version,abi));
    }
}
