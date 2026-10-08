package com.simpleengine.game;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public final class MinecraftNativeExtractor {
    private MinecraftNativeExtractor() {}

    public static List<Path> extract(Path archive, Path output) throws IOException {
        Files.createDirectories(output);
        List<Path> result=new ArrayList<>();
        try(ZipInputStream in=new ZipInputStream(Files.newInputStream(archive))){
            ZipEntry e;
            while((e=in.getNextEntry())!=null){
                if(e.isDirectory() || !isNative(e.getName())) continue;
                String name=e.getName();
                int slash=name.lastIndexOf('/');
                String fileName=slash>=0?name.substring(slash+1):name;
                if(fileName.isEmpty()) continue;
                Path target=output.resolve(fileName).normalize();
                if(!target.startsWith(output.normalize())) continue;
                Files.copy(in,target,StandardCopyOption.REPLACE_EXISTING);
                target.toFile().setExecutable(true,false);
                result.add(target);
            }
        }
        return result;
    }

    private static boolean isNative(String name){
        return name.endsWith(".so") || name.endsWith(".dll") || name.endsWith(".dylib");
    }
}
