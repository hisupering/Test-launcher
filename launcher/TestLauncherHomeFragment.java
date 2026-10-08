package net.kdt.pojavlaunch.launcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.SelectAuthFragment;
import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.value.MinecraftAccount;

public class TestLauncherHomeFragment extends Fragment {
    public static final String TAG = "TestLauncherHomeFragment";

    public TestLauncherHomeFragment() {
        super(R.layout.fragment_test_launcher_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (PojavProfile.getCurrentProfileContent(requireContext(), null) == null) {
            Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null);
            return;
        }

        MinecraftAccount account = PojavProfile.getCurrentProfileContent(requireContext(), null);
        TextView accountState = view.findViewById(R.id.test_account_state);
        if (accountState != null && account != null) accountState.setText(account.username);

        bind(view);
        animateIn(view);
    }

    private void bind(View view) {
        View play = view.findViewById(R.id.test_play);
        View account = view.findViewById(R.id.test_account_card);
        View versions = view.findViewById(R.id.test_versions);
        View downloads = view.findViewById(R.id.test_downloads);
        View mods = view.findViewById(R.id.test_mods);
        View settings = view.findViewById(R.id.test_settings);
        View controls = view.findViewById(R.id.test_controls);
        View worlds = view.findViewById(R.id.test_worlds);

        play.setOnClickListener(v -> {
            if (!Tools.hasOnlineProfile()) {
                Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null);
                return;
            }
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
        });

        account.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null));

        versions.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null));

        downloads.setOnClickListener(v -> openFolder());
        worlds.setOnClickListener(v -> openFolder("saves"));
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

    private void animateIn(View root) {
        Animation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(220);
        root.startAnimation(fade);
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