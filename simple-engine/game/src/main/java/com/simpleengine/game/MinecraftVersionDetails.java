package com.simpleengine.game;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class MinecraftVersionDetails {
    public final String id, type, mainClass, assetIndexId, clientSha1, assetIndexSha1;
    public final URL clientUrl, assetIndexUrl;
    public final Path clientPath;
    public final List<MinecraftLibrary> libraries, jvmArguments, gameArguments;
    public MinecraftVersionDetails(String id,String type,String mainClass,URL clientUrl,String clientSha1,Path clientPath,String assetIndexId,URL assetIndexUrl,String assetIndexSha1,List<MinecraftLibrary> libraries,List<String> jvmArguments,List<String> gameArguments){
        this.id=id;this.type=type;this.mainClass=mainClass;this.clientUrl=clientUrl;this.clientSha1=clientSha1;this.clientPath=clientPath;this.assetIndexId=assetIndexId;this.assetIndexUrl=assetIndexUrl;this.assetIndexSha1=assetIndexSha1;
        this.libraries=Collections.unmodifiableList(libraries);this.jvmArguments=Collections.unmodifiableList(jvmArguments);this.gameArguments=Collections.unmodifiableList(gameArguments);
    }
}
