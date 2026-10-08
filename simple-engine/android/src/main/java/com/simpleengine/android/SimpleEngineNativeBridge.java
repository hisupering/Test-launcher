package com.simpleengine.android;

import android.view.Surface;

public final class SimpleEngineNativeBridge {
    private static boolean loaded;
    private SimpleEngineNativeBridge() {}

    public static synchronized boolean load() {
        if (loaded) return true;
        try { System.loadLibrary("simpleengine"); loaded = true; return true; }
        catch (UnsatisfiedLinkError e) { return false; }
    }

    public static native void attachSurface(Surface surface);
    public static native void detachSurface();
    public static native void renderFrame();
    public static native void sendTouch(int action, int pointerId, float x, float y, float pressure);
    public static native int pollTouch(int[] out);
    public static native boolean initAudio();
    public static native void shutdownAudio();
}
