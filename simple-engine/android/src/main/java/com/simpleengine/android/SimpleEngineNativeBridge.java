package com.simpleengine.android;

import android.view.Surface;

/** JNI boundary for the independent Simple Engine native surface and input queue. */
public final class SimpleEngineNativeBridge {
    private static boolean loaded;

    private SimpleEngineNativeBridge() {}

    public static synchronized boolean load() {
        if (loaded) return true;
        try {
            System.loadLibrary("simpleengine");
            loaded = true;
            return true;
        } catch (UnsatisfiedLinkError error) {
            return false;
        }
    }

    public static native void attachSurface(Surface surface);

    /** Must be called on the render thread to destroy EGL resources on their owner thread. */
    public static native void detachSurface();

    public static native void renderFrame();

    public static native void sendTouch(
            int action, int actionIndex, int pointerId,
            float x, float y, float pressure);

    /**
     * Poll one event. meta = [action, actionIndex, pointerId, timestampMs];
     * values = [xPx, yPx, pressure]. Returns 1 when an event was read, otherwise 0.
     */
    public static native int pollTouch(int[] meta, float[] values);

    public static native boolean initAudio();
    public static native void shutdownAudio();
}
