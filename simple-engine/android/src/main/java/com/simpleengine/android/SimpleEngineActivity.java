package com.simpleengine.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public final class SimpleEngineActivity extends Activity {
    private SimpleEngineBridge engine;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        engine = new SimpleEngineBridge(getFilesDir());
        TextView view = new TextView(this);
        view.setText("Simple Engine");
        view.setTextSize(22);
        view.setPadding(32, 32, 32, 32);
        setContentView(view);
    }

    @Override
    protected void onDestroy() {
        if (engine != null) engine.stop();
        super.onDestroy();
    }
}
