package com.simpleengine.android;
import android.app.Activity;
import android.os.Bundle;
public final class SimpleEngineActivity extends Activity {
 private SimpleEngineBridge engine;
 private SimpleEngineRenderView view;
 @Override protected void onCreate(Bundle state){super.onCreate(state);engine=new SimpleEngineBridge(getFilesDir());view=new SimpleEngineRenderView(this);setContentView(view);}
 @Override protected void onDestroy(){if(view!=null)view.getHolder().getSurface();if(engine!=null)engine.stop();super.onDestroy();}
}
