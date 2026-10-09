package com.simpleengine.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.provider.OpenableColumns;
import android.widget.*;
import com.simpleengine.game.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SimpleLauncherActivity extends Activity {
    private static final int PICK_CONTENT_FILE = 701;
    private static final String PREF_CONTENT_CATEGORY = "content_category";
    private SimpleEngineBridge engine;
    private android.content.SharedPreferences prefs;
    private LinearLayout root;
    private TextView status;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private MinecraftLaunchController launchController;
    private String pendingContentCategory = "mods";

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
        File dir = new File(engine.getRoot(), "versions");
        List<String> out = new ArrayList<>();
        File[] dirs = dir.listFiles(File::isDirectory);
        if (dirs != null) {
            for (File versionDir : dirs) {
                File metadata = new File(versionDir, versionDir.getName() + ".json");
                if (metadata.isFile()) out.add(versionDir.getName());
            }
        }
        File[] flat = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (flat != null) {
            for (File f : flat) {
                String id = f.getName().substring(0, f.getName().length() - 5);
                if (!out.contains(id)) out.add(id);
            }
        }
        Collections.sort(out, Collections.reverseOrder());
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

    private File gameDirectory() {
        File dir = new File(engine.getRoot(), "game");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File contentDirectory(String category) {
        String folder;
        if ("resourcepacks".equals(category)) folder = "resourcepacks";
        else if ("shaderpacks".equals(category)) folder = "shaderpacks";
        else folder = "mods";
        File dir = new File(gameDirectory(), folder);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private String contentTitle(String category) {
        if ("resourcepacks".equals(category)) return "Resource Packs";
        if ("shaderpacks".equals(category)) return "Shader Packs";
        return "Mods";
    }

    private String allowedExtensions(String category) {
        if ("mods".equals(category)) return ".jar";
        return ".zip";
    }

    private void showContentManager(String category) {
        pendingContentCategory = category;
        File dir = contentDirectory(category);
        base(contentTitle(category));
        TextView info = new TextView(this);
        String hint = "mods".equals(category)
            ? "فایل مود .jar را وارد کن. برای اجرا باید نسخهٔ سازگار Fabric/Forge نصب باشد."
            : ("resourcepacks".equals(category)
                ? "فایل ریسورس‌پک .zip را وارد کن؛ سپس در تنظیمات Minecraft فعالش کن."
                : "فایل شیدرپک .zip را وارد کن؛ برای اجرا به Iris یا OptiFine سازگار نیاز است.");
        info.setText(hint + "\nفایل‌ها در پوشهٔ داخلی بازی ذخیره می‌شوند: " + dir.getName());
        info.setTextSize(15); root.addView(info);

        Button add = button("＋ Import " + contentTitle(category) + " from device");
        add.setOnClickListener(v -> openContentPicker(category));

        ListView list = new ListView(this);
        List<File> files = listContentFiles(dir, category);
        List<String> labels = new ArrayList<>();
        for (File f : files) labels.add(f.getName() + "  •  " + readableSize(f.length()));
        if (labels.isEmpty()) labels.add("هنوز فایلی وارد نشده");
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels));
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position >= files.size()) return true;
            File target = files.get(position);
            new android.app.AlertDialog.Builder(this)
                .setTitle("حذف فایل")
                .setMessage("فایل " + target.getName() + " از لانچر حذف شود؟")
                .setNegativeButton("Cancel", (d,w) -> {})
                .setPositiveButton("Delete", (d,w) -> {
                    if (target.delete()) Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    showContentManager(category);
                }).show();
            return true;
        });

        Button refresh = button("Refresh list");
        refresh.setOnClickListener(v -> showContentManager(category));
        Button back = button("Back to Home"); back.setOnClickListener(v -> showHome());
    }

    private List<File> listContentFiles(File dir, String category) {
        String ext = allowedExtensions(category);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(ext));
        List<File> result = new ArrayList<>();
        if (files != null) result.addAll(Arrays.asList(files));
        result.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private String readableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void openContentPicker(String category) {
        pendingContentCategory = category;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, PICK_CONTENT_FILE);
        } catch (Throwable e) {
            Toast.makeText(this, "نمی‌توان فایل‌انتخاب‌کن را باز کرد: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_CONTENT_FILE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String category = pendingContentCategory;
        String name = queryDisplayName(uri);
        String lower = name.toLowerCase(Locale.ROOT);
        String required = allowedExtensions(category);
        if (!lower.endsWith(required)) {
            Toast.makeText(this, "این بخش فقط فایل " + required + " می‌پذیرد.", Toast.LENGTH_LONG).show();
            return;
        }
        int grantedFlags = data.getFlags();
        if ((grantedFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Throwable ignored) { }
        }
        File destination = contentDirectory(category);
        String safeName = name.replaceAll("[^a-zA-Z0-9._ -]", "_").trim();
        if (safeName.isEmpty()) safeName = "imported" + required;
        File target = new File(destination, safeName);
        if (target.exists()) target = new File(destination, System.currentTimeMillis() + "-" + safeName);
        final File finalTarget = target;
        base("Importing " + contentTitle(category));
        status = new TextView(this);
        status.setText("در حال کپی‌کردن فایل به پوشهٔ داخلی Simple Launcher...");
        root.addView(status);
        worker.execute(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new java.io.FileOutputStream(finalTarget)) {
                if (in == null) throw new IllegalStateException("فایل قابل خواندن نیست");
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                runOnUiThread(() -> {
                    Toast.makeText(this, "وارد شد: " + finalTarget.getName(), Toast.LENGTH_LONG).show();
                    showContentManager(category);
                });
            } catch (Throwable e) {
                runOnUiThread(() -> {
                    status.setText("Import failed: " + e.getMessage());
                    Button back = button("Back");
                    back.setOnClickListener(v -> showContentManager(category));
                });
            }
        });
    }

    private String queryDisplayName(Uri uri) {
        String name = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) name = cursor.getString(index);
            }
        } catch (Throwable ignored) { }
        if (name == null || name.trim().isEmpty()) name = uri.getLastPathSegment();
        return name == null ? "imported-file" : name;
    }

    private void showModsWebsite() {
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        TextView bar=new TextView(this);
        bar.setText("Simple Mods • Modrinth"); bar.setTextSize(20); bar.setPadding(20,20,20,20);
        page.addView(bar);
        WebView web=new WebView(this);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Toast.makeText(this, "برای نصب مستقیم، فایل را از بخش Import در Simple Launcher انتخاب کن.", Toast.LENGTH_LONG).show();
        });
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
        Button mods=button("Mods • Manage .jar files"); mods.setOnClickListener(v->showContentManager("mods"));
        Button packs=button("Resource Packs • Manage .zip"); packs.setOnClickListener(v->showContentManager("resourcepacks"));
        Button shaders=button("Shader Packs • Manage .zip"); shaders.setOnClickListener(v->showContentManager("shaderpacks"));
        Button browse=button("Browse Mods on Modrinth"); browse.setOnClickListener(v->showModsWebsite());
        Button settings=button("Settings"); settings.setOnClickListener(v->showSetup());
    }

    private void launchMinecraft() {
        String version=prefs.getString("version","");
        if(version.isEmpty()){
            Toast.makeText(this,"First install a Minecraft version.",Toast.LENGTH_LONG).show();
            return;
        }

        base("Launching Minecraft " + version);
        status = new TextView(this);
        status.setText("Preparing Java runtime and Minecraft files...\nEverything stays inside Simple Launcher.");
        status.setTextSize(16);
        root.addView(status);

        ProgressBar progress = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100); progress.setProgress(5); root.addView(progress);

        Button back = button("Back");
        back.setOnClickListener(v -> {
            if (launchController != null) launchController.stop();
            showHome();
        });

        worker.execute(() -> {
            try {
                runOnUiThread(() -> status.setText("Installing Android Java runtime..."));
                File javaExecutable = engine.ensureJavaRuntime();
                runOnUiThread(() -> { progress.setProgress(25); status.setText("Preparing Minecraft " + version + "..."); });

                File versionJson = new File(engine.getRoot(),"versions/" + version + "/" + version + ".json");
                if (!versionJson.isFile()) throw new IllegalStateException("Installed version metadata is missing");
                String json = new String(java.nio.file.Files.readAllBytes(versionJson.toPath()), java.nio.charset.StandardCharsets.UTF_8);

                File gameDir = gameDirectory();
                File assetsDir = new File(engine.getRoot(),"assets");
                File nativesDir = new File(engine.getRoot(),"natives/" + version);
                nativesDir.mkdirs();
                runOnUiThread(() -> status.setText("Preparing Android LWJGL bridge..."));
                AndroidLwjglInstaller.Bundle lwjgl = AndroidLwjglInstaller.ensure(this, engine.getRoot());
                for (File nativeFile : Objects.requireNonNull(lwjgl.natives.listFiles((d,n)->n.endsWith(".so")))) {
                    File target = new File(nativesDir, nativeFile.getName());
                    java.nio.file.Files.copy(nativeFile.toPath(), target.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    target.setExecutable(true, false);
                }

                MinecraftInstallResult install = MinecraftInstaller.install(
                    json,
                    new File(engine.getRoot(),"versions").toPath(),
                    new File(engine.getRoot(),"libraries").toPath(),
                    assetsDir.toPath());

                runOnUiThread(() -> { progress.setProgress(70); status.setText("Starting Simple Engine JVM..."); });

                Map<String,Boolean> features = new HashMap<>();
                features.put("has_custom_resolution", false);
                features.put("is_demo_user", false);
                features.put("has_quick_plays_support", false);
                features.put("is_quick_play_singleplayer", false);
                features.put("is_quick_play_multiplayer", false);
                features.put("is_quick_play_realms", false);

                MinecraftLaunchContext ctx = new MinecraftLaunchContext(
                    cleanName(prefs.getString("username","Player")),
                    "00000000-0000-0000-0000-000000000001",
                    "0",
                    "legacy",
                    install.version.type == null ? "release" : install.version.type,
                    install.version.id,
                    gameDir,
                    assetsDir,
                    install.version.assetIndexId,
                    nativesDir,
                    "Simple Launcher",
                    "0.1.0",
                    features);

                launchController = new MinecraftLaunchController(new com.simpleengine.core.EngineListener() {
                    public void onStateChanged(com.simpleengine.core.EngineState state) {
                        runOnUiThread(() -> status.setText("Minecraft JVM: " + state));
                    }
                    public void onLog(String line) {
                        runOnUiThread(() -> status.setText("Minecraft: " + line));
                    }
                    public void onError(Throwable error) {
                        runOnUiThread(() -> status.setText("Minecraft error: " + error));
                    }
                });

                launchController.launch(javaExecutable, install, gameDir, ctx, prefs.getInt("ram_mb",1024), lwjgl.jars);
                runOnUiThread(() -> { progress.setProgress(100); status.setText("Minecraft JVM started.\nIf the game closes, open the log/error shown here."); });
            } catch(Throwable e) {
                runOnUiThread(() -> status.setText("Launch failed: " + e));
            }
        });
    }

    @Override protected void onDestroy(){worker.shutdownNow();if(engine!=null)engine.stop();super.onDestroy();}
}
