package com.simpleengine.game;

import com.google.gson.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public final class MinecraftVersionJsonParser {
    private MinecraftVersionJsonParser() {}
    public static MinecraftVersionDetails parse(String json, Path librariesRoot, Path clientRoot) {
        JsonObject root=JsonParser.parseString(json).getAsJsonObject();
        String id=s(root,"id"), type=s(root,"type"), main=s(root,"mainClass");
        if(main==null) main=s(root,"mainClassLegacy");
        JsonObject dl=root.getAsJsonObject("downloads"), client=dl==null?null:dl.getAsJsonObject("client");
        URL cu=url(client,"url"); String cs=s(client,"sha1"); Path cp=clientRoot.resolve(id+".jar");
        String ai=null,as=null; URL au=null;
        if(root.has("assetIndex")){JsonObject a=root.getAsJsonObject("assetIndex");ai=s(a,"id");as=s(a,"sha1");au=url(a,"url");}
        List<MinecraftLibrary> libs=new ArrayList<>();
        if(root.has("libraries")) for(JsonElement e:root.getAsJsonArray("libraries")){
            JsonObject l=e.getAsJsonObject(); if(!allowed(l)) continue;
            JsonObject d=l.has("downloads")?l.getAsJsonObject("downloads"):null;
            if(d==null||!d.has("artifact")) continue; JsonObject a=d.getAsJsonObject("artifact");
            URL u=url(a,"url"); String p=s(a,"path"); if(u!=null&&p!=null) libs.add(new MinecraftLibrary(s(l,"name"),u,s(a,"sha1"),librariesRoot.resolve(p)));
        }
        List<String> jvm=new ArrayList<>(), game=new ArrayList<>();
        if(root.has("arguments")){JsonObject a=root.getAsJsonObject("arguments");read(a.getAsJsonArray("jvm"),jvm);read(a.getAsJsonArray("game"),game);}
        else if(root.has("minecraftArguments")) Collections.addAll(game,root.get("minecraftArguments").getAsString().split("\\s+"));
        return new MinecraftVersionDetails(id,type,main,cu,cs,cp,ai,au,as,libs,jvm,game);
    }
    private static void read(JsonArray a,List<String> out){if(a==null)return;for(JsonElement e:a)if(e.isJsonPrimitive())out.add(e.getAsString());else{JsonObject o=e.getAsJsonObject();if(allowed(o)&&o.has("value")){JsonElement v=o.get("value");if(v.isJsonArray())for(JsonElement x:v.getAsJsonArray())out.add(x.getAsString());else out.add(v.getAsString());}}}
    private static boolean allowed(JsonObject o){if(!o.has("rules"))return true;boolean allow=false;for(JsonElement e:o.getAsJsonArray("rules")){JsonObject r=e.getAsJsonObject();if(!r.has("os")){allow="allow".equals(s(r,"action"));continue;}String n=s(r.getAsJsonObject("os"),"name");if("linux".equals(n)||"android".equals(n)||n==null)allow="allow".equals(s(r,"action"));}return allow;}
    private static String s(JsonObject o,String k){return o!=null&&o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():null;}
    private static URL url(JsonObject o,String k){try{String v=s(o,k);return v==null?null:new URL(v);}catch(Exception e){throw new IllegalArgumentException("Invalid URL: "+k,e);}}
}
