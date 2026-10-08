package net.kdt.pojavlaunch.launcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.SelectAuthFragment;

public class TestLauncherHomeFragment extends Fragment {
    public static final String TAG = "TestLauncherHomeFragment";

    public TestLauncherHomeFragment() {
        super(R.layout.fragment_test_launcher_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button play = view.findViewById(R.id.test_play);
        Button account = view.findViewById(R.id.test_account);
        Button versions = view.findViewById(R.id.test_versions);
        Button downloads = view.findViewById(R.id.test_downloads);
        Button mods = view.findViewById(R.id.test_mods);
        Button settings = view.findViewById(R.id.test_settings);
        Button controls = view.findViewById(R.id.test_controls);
        Button worlds = view.findViewById(R.id.test_worlds);
        Button packs = view.findViewById(R.id.test_resourcepacks);

        play.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        account.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null));

        versions.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null));

        downloads.setOnClickListener(v -> openFolder());
        worlds.setOnClickListener(v -> openFolder("saves"));
        packs.setOnClickListener(v -> openFolder("resourcepacks"));

        mods.setOnClickListener(v -> openUrl("https://modrinth.com/mods"));
        settings.setOnClickListener(v -> Tools.swapFragment(
                requireActivity(),
                net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment.class,
                "SETTINGS_FRAGMENT",
                null
        ));
        controls.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), net.kdt.pojavlaunch.CustomControlsActivity.class)));
    }

    private void openFolder() {
        Tools.openPath(requireContext(), new java.io.File(Tools.DIR_GAME_NEW), false);
    }

    private void openFolder(String child) {
        java.io.File f = new java.io.File(Tools.DIR_GAME_NEW, child);
        if (!f.exists()) f.mkdirs();
        Tools.openPath(requireContext(), f, false);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
