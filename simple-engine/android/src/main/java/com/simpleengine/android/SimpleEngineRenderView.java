package com.simpleengine.android;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
public final class SimpleEngineRenderView extends SurfaceView implements SurfaceHolder.Callback {
 private volatile boolean running;
 private Thread renderThread;
 public SimpleEngineRenderView(Context context){super(context);getHolder().addCallback(this);setFocusable(true);}
 public void surfaceCreated(SurfaceHolder h){if(!SimpleEngineNativeBridge.load())return;SimpleEngineNativeBridge.attachSurface(h.getSurface());running=true;renderThread=new Thread(()->{while(running){SimpleEngineNativeBridge.renderFrame();try{Thread.sleep(16);}catch(InterruptedException e){Thread.currentThread().interrupt();break;}}},"SimpleEngine-Render");renderThread.start();}
 public void surfaceDestroyed(SurfaceHolder h){running=false;if(renderThread!=null){renderThread.interrupt();renderThread=null;}if(SimpleEngineNativeBridge.load())SimpleEngineNativeBridge.detachSurface();}
 public void surfaceChanged(SurfaceHolder h,int format,int width,int height){}
}
