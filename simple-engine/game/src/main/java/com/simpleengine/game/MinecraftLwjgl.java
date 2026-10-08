package com.simpleengine.game;

import java.nio.file.Path;
import java.util.*;

public final class MinecraftLwjgl {
    private MinecraftLwjgl(){}

    public static List<String> jvmNativeProperties(Path natives){
        String p=natives.toAbsolutePath().toString();
        return Arrays.asList("-Djava.library.path="+p,"-Dorg.lwjgl.librarypath="+p);
    }

    public static List<String> lwjglModules(List<MinecraftLibrary> libraries){
        List<String> result=new ArrayList<>();
        for(MinecraftLibrary l:libraries) if(l.name!=null && l.name.toLowerCase().contains("lwjgl")) result.add(l.name);
        return result;
    }
}
