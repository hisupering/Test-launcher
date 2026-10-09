package com.simpleengine.android;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public final class SimpleEngineRenderView extends SurfaceView implements SurfaceHolder.Callback {
    private volatile boolean running;
    private Thread renderThread;

    public SimpleEngineRenderView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!SimpleEngineNativeBridge.load()) return;

        SimpleEngineNativeBridge.attachSurface(holder.getSurface());
        running = true;
        renderThread = new Thread(() -> {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    SimpleEngineNativeBridge.renderFrame();
                    try {
                        Thread.sleep(16L);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                // EGL is created by renderFrame and must be torn down on this same thread.
                SimpleEngineNativeBridge.detachSurface();
            }
        }, "SimpleEngine-Render");
        renderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        Thread thread = renderThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            renderThread = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // The render thread queries the actual EGL surface dimensions each frame.
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!SimpleEngineNativeBridge.load()) return true;

        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (action == MotionEvent.ACTION_MOVE) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        sendPointer(event, action, actionIndex, i);
                    }
                } else {
                    sendPointer(event, action, actionIndex, actionIndex);
                }
                return true;
            default:
                return true;
        }
    }

    private static void sendPointer(MotionEvent event, int action, int actionIndex, int index) {
        if (index < 0 || index >= event.getPointerCount()) return;
        SimpleEngineNativeBridge.sendTouch(
                action,
                actionIndex,
                event.getPointerId(index),
                event.getX(index),
                event.getY(index),
                event.getPressure(index));
    }
}
