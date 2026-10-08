package com.simpleengine.core;

public interface EngineListener {
    void onStateChanged(EngineState state);
    void onLog(String line);
    void onError(Throwable error);
}
