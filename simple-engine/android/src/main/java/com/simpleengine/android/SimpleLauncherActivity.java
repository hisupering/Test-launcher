package com.simpleengine.android;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public final class SimpleLauncherActivity extends Activity {
 private SimpleEngineBridge engine;
 @Override protected void onCreate(Bundle state){
  super.onCreate(state); engine=new SimpleEngineBridge(getFilesDir());
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(32,32,32,32);
  TextView title=new TextView(this); title.setText("Simple Launcher"); title.setTextSize(28); root.addView(title);
  TextView status=new TextView(this); status.setText("Simple Engine ready"); root.addView(status);
  Button button=new Button(this); button.setText("Open Simple Engine"); button.setOnClickListener(v->setContentView(new SimpleEngineRenderView(this))); root.addView(button);
  setContentView(root);
 }
 @Override protected void onDestroy(){if(engine!=null)engine.stop();super.onDestroy();}
}