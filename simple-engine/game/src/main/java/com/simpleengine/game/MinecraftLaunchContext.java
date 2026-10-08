package com.simpleengine.game;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
public final class MinecraftLaunchContext {
 public final String username,uuid,accessToken,userType,versionType,assetIndexName,launcherName,launcherVersion;
 public final File gameDirectory,assetsDirectory,nativesDirectory; public final Map<String,String> features;
 public MinecraftLaunchContext(String username,String uuid,String accessToken,String userType,String versionType,File gameDirectory,File assetsDirectory,String assetIndexName,File nativesDirectory,String launcherName,String launcherVersion,Map<String,String> features){this.username=username==null?"Player":username;this.uuid=uuid==null?"00000000-0000-0000-0000-000000000000":uuid;this.accessToken=accessToken==null?"0":accessToken;this.userType=userType==null?"legacy":userType;this.versionType=versionType==null?"release":versionType;this.gameDirectory=gameDirectory;this.assetsDirectory=assetsDirectory;this.assetIndexName=assetIndexName==null?"":assetIndexName;this.nativesDirectory=nativesDirectory;this.launcherName=launcherName==null?"Simple Launcher":launcherName;this.launcherVersion=launcherVersion==null?"1.0":launcherVersion;this.features=features==null?new LinkedHashMap<>():new LinkedHashMap<>(features);}}
