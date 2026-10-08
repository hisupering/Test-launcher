package com.simpleengine.android;
import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceView;
public final class SimpleEngineSurfaceView extends SurfaceView {
 public SimpleEngineSurfaceView(Context c){super(c);setFocusable(true);}
 @Override public boolean onTouchEvent(MotionEvent e){if(SimpleEngineNativeBridge.load())SimpleEngineNativeBridge.sendTouch(e.getActionMasked(),e.getX(),e.getY(),e.getPressure());return true;}
}
