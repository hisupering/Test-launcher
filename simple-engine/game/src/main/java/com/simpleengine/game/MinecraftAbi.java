package com.simpleengine.game;

public enum MinecraftAbi {
    ARM64("arm64-v8a","aarch64"),
    ARMV7("armeabi-v7a","arm"),
    X86_64("x86_64","x86_64");

    public final String androidName;
    public final String javaArch;
    MinecraftAbi(String androidName,String javaArch){this.androidName=androidName;this.javaArch=javaArch;}

    public static MinecraftAbi from(String abi){
        for(MinecraftAbi a:values()) if(a.androidName.equals(abi)) return a;
        throw new IllegalArgumentException("Unsupported Android ABI: "+abi);
    }
}
