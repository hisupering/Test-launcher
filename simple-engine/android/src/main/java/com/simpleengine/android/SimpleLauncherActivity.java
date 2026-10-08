package com.simpleengine.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import com.simpleengine.game.*;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SimpleLauncherActivity extends Activity {
    private SimpleEngineBridge engine;
    private android.content.SharedPreferences prefs;
    private LinearLayout root;
    private TextView status;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        engine = new SimpleEngineBridge(getFilesDir());
        prefs = getSharedPreferences("simple_launcher", MODE_PRIVATE);
        if (prefs.getBoolean("setup_complete", false)) showHome(); else showSetup();
    }

    private void base(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28,28,28,28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView t = new TextView(this); t.setText(title); t.setTextSize(28); t.setGravity(Gravity.CENTER);
        root.addView(t, new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
    }

    private void showSetup() {
        base("Simple Launcher");
        TextView info = new TextView(this);
        info.setText("Setup اولیه\nظاهر، RAM و نام بازیکن را انتخاب کن.");
        info.setTextSize(17); root.addView(info);

        TextView themeLabel = new TextView(this); themeLabel.setText("Theme"); root.addView(themeLabel);
        RadioGroup theme = new RadioGroup(this);
        RadioButton dark = new RadioButton(this); dark.setText("Dark"); dark.setId(1);
        RadioButton light = new RadioButton(this); light.setText("Light"); light.setId(2);
        theme.addView(dark); theme.addView(light); theme.check(prefs.getBoolean("dark",true)?1:2); root.addView(theme);

        TextView ram = new TextView(this); ram.setText("RAM بازی: "+prefs.getInt("ram_mb",1024)+" MB"); root.addView(ram);
        SeekBar bar = new SeekBar(this); bar.setMax(16); bar.setProgress(Math.max(0,Math.min(16,(prefs.getInt("ram_mb",1024)-512)/256)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean f){ram.setText("RAM بازی: "+(512+p*256)+" MB");}
            public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}
        }); root.addView(bar);

        EditText name = new EditText(this); name.setSingleLine(); name.setHint("Player name"); name.setText(prefs.getString("username","Player")); root.addView(name);
        Button next = new Button(this); next.setText("Next");
        next.setOnClickListener(v->{prefs.edit().putBoolean("dark",theme.getCheckedRadioButtonId()==1).putInt("ram_mb",512+bar.getProgress()*256).putString("username",name.getText().toString().trim().isEmpty()?"Player":name.getText().toString().trim()).apply(); showAccount();});
        root.addView(next);
    }

    private void showAccount() {
        base("Simple Account");
        TextView t = new TextView(this); t.setText("اکانت محلی\nبرای Engine مستقل، بدون گرفتن رمز Microsoft."); t.setTextSize(17); root.addView(t);
        EditText name = new EditText(this); name.setSingleLine(); name.setHint("Username"); name.setText(prefs.getString("username","Player")); root.addView(name);
        Button next = new Button(this); next.setText("Next — Versions");
        next.setOnClickListener(v->{prefs.edit().putString("username",name.getText().toString().trim().isEmpty()?"Player":name.getText().toString().trim()).putBoolean("setup_complete",true).apply(); showVersions();});
        root.addView(next);
    }

    private void showVersions() {
        base("Versions");
        status = new TextView(this); status.setText("در حال دریافت فهرست رسمی Minecraft..."); root.addView(status);
        Spinner spinner = new Spinner(this); root.addView(spinner);
        Button install = new Button(this); install.setText("Download & Install"); install.setEnabled(false); root.addView(install);
        Button home = new Button(this); home.setText("Home"); home.setOnClickListener(v->showHome()); root.addView(home);

        worker.execute(()->{
            try {
                String json = MinecraftManifestLoader.loadJson(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"));
                MinecraftManifest m = MinecraftManifestLoader.parse(json);
                int count=Math.min(80,m.versions.size()); String[] ids=new String[count];
                for(int i=0;i<count;i++) ids[i]=m.versions.get(i).id;
                runOnUiThread(()->{
                    spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ids));
                    install.setEnabled(count>0); status.setText("Release: "+m.latestRelease+"  •  "+count+" versions");
                });
                install.setOnClickListener(v->{int pos=spinner.getSelectedItemPosition(); if(pos>=0&&pos<m.versions.size()) installVersion(m.versions.get(pos));});
            } catch(Throwable e){runOnUiThread(()->status.setText("Version list error: "+String.valueOf(e.getMessage())));}
        });
    }

    private void installVersion(MinecraftVersion mv) {
        status.setText("Downloading "+mv.id+" ...");
        worker.execute(()->{
            try {
                String json=MinecraftManifestLoader.loadJson(new URL(mv.url));
                File r=engine.getRoot();
                MinecraftInstallResult result=MinecraftInstaller.install(json,new File(r,"versions").toPath(),new File(r,"libraries").toPath(),new File(r,"assets").toPath());
                prefs.edit().putString("version",result.version.id).apply();
                runOnUiThread(()->{Toast.makeText(this,"Installed "+result.version.id,Toast.LENGTH_LONG).show();showHome();});
            } catch(Throwable e){runOnUiThread(()->status.setText("Install failed: "+String.valueOf(e.getMessage())));}
        });
    }

    private void showHome() {
        base("Simple Launcher");
        TextView t=new TextView(this);
        t.setText("Independent Simple Engine\nVersion: "+prefs.getString("version","Not installed")+"\nRAM: "+prefs.getInt("ram_mb",1024)+" MB");
        t.setTextSize(17); root.addView(t);
        Button launch=new Button(this); launch.setText("Launch Minecraft"); launch.setOnClickListener(v->launchMinecraft()); root.addView(launch);
        Button versions=new Button(this); versions.setText("Versions / Install"); versions.setOnClickListener(v->showVersions()); root.addView(versions);
        Button settings=new Button(this); settings.setText("Settings"); settings.setOnClickListener(v->showSetup()); root.addView(settings);
    }

    private void launchMinecraft() {
        if(prefs.getString("version","").isEmpty()){Toast.makeText(this,"First install a Minecraft version.",Toast.LENGTH_LONG).show();return;}
        Toast.makeText(this,"Launch pipeline is connected, but no Android Java runtime is bundled yet.",Toast.LENGTH_LONG).show();
        setContentView(new SimpleEngineRenderView(this));
    }

    @Override protected void onDestroy(){worker.shutdownNow();if(engine!=null)engine.stop();super.onDestroy();}
}