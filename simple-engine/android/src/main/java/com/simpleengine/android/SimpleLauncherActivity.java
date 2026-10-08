package com.simpleengine.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.simpleengine.game.*;
import java.io.File;
import java.net.URL;
import java.util.*;
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
        TextView t = new TextView(this);
        t.setText(title); t.setTextSize(28); t.setGravity(Gravity.CENTER);
        root.addView(t, new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
    }

    private Button button(String text) {
        Button b = new Button(this); b.setText(text);
        root.addView(b, new LinearLayout.LayoutParams(-1,-2));
        return b;
    }

    private String cleanName(String value) {
        String n=value==null?"":value.trim();
        return n.isEmpty()?"Player":n;
    }

    private void showSetup() {
        base("Simple Launcher");
        TextView info = new TextView(this);
        info.setText("Setup اولیه\nظاهر، RAM و نام بازیکن را انتخاب کن.");
        info.setTextSize(17); root.addView(info);
        TextView themeLabel = new TextView(this); themeLabel.setText("Theme"); root.addView(themeLabel);
        RadioGroup theme = new RadioGroup(this);
        RadioButton dark = new RadioButton(this); dark.setText("Dark"); dark.setId(View.generateViewId());
        RadioButton light = new RadioButton(this); light.setText("Light"); light.setId(View.generateViewId());
        theme.addView(dark); theme.addView(light);
        theme.check(prefs.getBoolean("dark",true)?dark.getId():light.getId()); root.addView(theme);
        TextView ram = new TextView(this); ram.setText("RAM بازی: "+prefs.getInt("ram_mb",1024)+" MB"); root.addView(ram);
        SeekBar bar = new SeekBar(this); bar.setMax(16);
        bar.setProgress(Math.max(0,Math.min(16,(prefs.getInt("ram_mb",1024)-512)/256)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean f){ram.setText("RAM بازی: "+(512+p*256)+" MB");}
            public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}
        });
        root.addView(bar);
        EditText name = new EditText(this); name.setSingleLine();
        name.setHint("Player name"); name.setText(prefs.getString("username","Player")); root.addView(name);
        Button next = button("Next");
        next.setOnClickListener(v->{
            prefs.edit().putBoolean("dark",theme.getCheckedRadioButtonId()==dark.getId())
                .putInt("ram_mb",512+bar.getProgress()*256)
                .putString("username",cleanName(name.getText().toString())).apply();
            showAccount();
        });
    }

    private void showAccount() {
        base("Simple Account");
        TextView t = new TextView(this);
        t.setText("اکانت محلی\nبدون گرفتن رمز Microsoft.");
        t.setTextSize(17); root.addView(t);
        EditText name = new EditText(this); name.setSingleLine();
        name.setHint("Username"); name.setText(prefs.getString("username","Player")); root.addView(name);
        Button next = button("Next — Home");
        next.setOnClickListener(v->{
            prefs.edit().putString("username",cleanName(name.getText().toString()))
                .putBoolean("setup_complete",true).apply();
            showHome();
        });
    }

    private List<String> installedVersions() {
        File dir=new File(engine.getRoot(),"versions");
        File[] files=dir.listFiles((d,n)->n.endsWith(".json"));
        List<String> out=new ArrayList<>();
        if(files!=null) for(File f:files) out.add(f.getName().substring(0,f.getName().length()-5));
        Collections.sort(out,Collections.reverseOrder());
        return out;
    }

    private void showVersions() {
        base("Minecraft Versions");
        status = new TextView(this);
        status.setText("نسخه‌های نصب‌شده و نسخه‌های رسمی داخل همین برنامه.");
        status.setTextSize(16); root.addView(status);

        TextView installedTitle=new TextView(this); installedTitle.setText("Installed");
        installedTitle.setTextSize(20); root.addView(installedTitle);
        ListView installedList=new ListView(this);
        List<String> installed=installedVersions();
        if(installed.isEmpty()) installed.add("هنوز نسخه‌ای نصب نشده");
        installedList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,installed));
        root.addView(installedList,new LinearLayout.LayoutParams(-1,180));

        TextView onlineTitle=new TextView(this); onlineTitle.setText("Official versions");
        onlineTitle.setTextSize(20); root.addView(onlineTitle);
        Spinner spinner = new Spinner(this); root.addView(spinner);
        Button install = button("Download & Install"); install.setEnabled(false);
        Button refresh = button("Refresh versions");
        Button home = button("Home"); home.setOnClickListener(v->showHome());

        final MinecraftManifest[] holder=new MinecraftManifest[1];
        Runnable load=()->worker.execute(()->{
            try {
                runOnUiThread(()->status.setText("در حال دریافت فهرست رسمی..."));
                String json=MinecraftManifestLoader.loadJson(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"));
                MinecraftManifest m=MinecraftManifestLoader.parse(json); holder[0]=m;
                List<MinecraftVersion> versions=m.getVersions();
                int count=Math.min(80,versions.size()); String[] ids=new String[count];
                for(int i=0;i<count;i++) ids[i]=versions.get(i).id;
                runOnUiThread(()->{
                    spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,ids));
                    install.setEnabled(count>0);
                    status.setText("Latest release: "+m.getLatestRelease()+" • "+count+" versions");
                });
            } catch(Throwable e){runOnUiThread(()->status.setText("Version list error: "+String.valueOf(e.getMessage())));}
        });
        load.run();
        refresh.setOnClickListener(v->load.run());
        install.setOnClickListener(v->{
            MinecraftManifest m=holder[0];
            if(m==null) return;
            int pos=spinner.getSelectedItemPosition();
            if(pos>=0 && pos<m.getVersions().size()) installVersion(m.getVersions().get(pos));
        });
    }

    private void installVersion(MinecraftVersion mv) {
        base("Installing "+mv.id);
        status=new TextView(this);
        status.setText("در حال نصب داخل Simple Launcher...\nMy Files باز نمی‌شود.");
        status.setTextSize(17); root.addView(status);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100); progress.setProgress(1); root.addView(progress);
        Button back=button("Back"); back.setOnClickListener(v->showVersions());
        worker.execute(()->{
            try {
                runOnUiThread(()->status.setText("دریافت اطلاعات "+mv.id+"..."));
                String json=MinecraftManifestLoader.loadJson(new URL(mv.url));
                runOnUiThread(()->{progress.setProgress(15);status.setText("دانلود فایل‌های "+mv.id+"...");});
                File r=engine.getRoot();
                MinecraftInstallResult result=MinecraftInstaller.install(json,
                    new File(r,"versions").toPath(),new File(r,"libraries").toPath(),new File(r,"assets").toPath());
                prefs.edit().putString("version",result.version.id).apply();
                runOnUiThread(()->{progress.setProgress(100);showVersions();});
            } catch(Throwable e){runOnUiThread(()->status.setText("Install failed: "+String.valueOf(e.getMessage())));}
        });
    }

    private void showMods() {
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        TextView bar=new TextView(this);
        bar.setText("Simple Mods • Modrinth"); bar.setTextSize(20); bar.setPadding(20,20,20,20);
        page.addView(bar);
        WebView web=new WebView(this);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.loadUrl("https://modrinth.com/mods");
        page.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        Button back=new Button(this); back.setText("Back to Simple Launcher");
        back.setOnClickListener(v->showHome()); page.addView(back);
        setContentView(page);
    }

    private void showHome() {
        base("Simple Launcher");
        List<String> installed=installedVersions();
        String current=prefs.getString("version",installed.isEmpty()?"Not installed":installed.get(0));
        TextView t=new TextView(this);
        t.setText("Independent Simple Engine\nInstalled: "+(installed.isEmpty()?"None":installed.size()+" version(s)")
            +"\nSelected: "+current+"\nRAM: "+prefs.getInt("ram_mb",1024)+" MB");
        t.setTextSize(17); root.addView(t);
        Button launch=button("Launch Minecraft"); launch.setOnClickListener(v->launchMinecraft());
        Button versions=button("Versions • Install / Installed"); versions.setOnClickListener(v->showVersions());
        Button mods=button("Mods • Open inside app"); mods.setOnClickListener(v->showMods());
        Button settings=button("Settings"); settings.setOnClickListener(v->showSetup());
    }

    private void launchMinecraft() {
        String version=prefs.getString("version","");
        if(version.isEmpty()){Toast.makeText(this,"First install a Minecraft version.",Toast.LENGTH_LONG).show();return;}
        Toast.makeText(this,"Simple Engine: preparing Minecraft "+version,Toast.LENGTH_LONG).show();
        setContentView(new SimpleEngineRenderView(this));
    }

    @Override protected void onDestroy(){worker.shutdownNow();if(engine!=null)engine.stop();super.onDestroy();}
}