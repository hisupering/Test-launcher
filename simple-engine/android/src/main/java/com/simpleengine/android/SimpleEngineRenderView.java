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
            while (running) {
                SimpleEngineNativeBridge.renderFrame();
                try {
                    Thread.sleep(16L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "SimpleEngine-Render");
        renderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        if (renderThread != null) {
            renderThread.interrupt();
            renderThread = null;
        }
        if (SimpleEngineNativeBridge.load()) {
            SimpleEngineNativeBridge.detachSurface();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!SimpleEngineNativeBridge.load()) return true;

        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_POINTER_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_CANCEL) {

            if (action == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    sendPointer(event, i);
                }
            } else {
                sendPointer(event, actionIndex);
            }
        }
        return true;
    }

    private static void sendPointer(MotionEvent event, int index) {
        if (index < 0 || index >= event.getPointerCount()) return;
        SimpleEngineNativeBridge.sendTouch(
                event.getActionMasked(),
                event.getPointerId(index),
                event.getX(index),
                event.getY(index),
                event.getPressure(index));
    }
}
