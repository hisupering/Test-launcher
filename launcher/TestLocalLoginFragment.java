package net.kdt.pojavlaunch.launcher;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.util.regex.Pattern;

public class TestLocalLoginFragment extends Fragment {
    public static final String TAG = "TEST_LOCAL_LOGIN_FRAGMENT";
    private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
    private EditText username;

    public TestLocalLoginFragment() {
        super(R.layout.fragment_test_local_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        username = view.findViewById(R.id.login_edit_email);
        view.findViewById(R.id.login_button).setOnClickListener(v -> create());
        username.setOnEditorActionListener((v, actionId, event) -> {
            create();
            return true;
        });
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(220);
        view.startAnimation(a);
    }

    private void create() {
        Context context = requireContext();
        String name = username.getText().toString().trim();

        if (name.length() < 3 || name.length() > 16 || !pattern.matcher(name).matches()) {
            Tools.dialog(context, "Invalid player name", "Use 3–16 characters: A-Z, 0-9 or _.");
            return;
        }

        File accountFile = new File(Tools.DIR_ACCOUNT_NEW, name + ".json");
        if (accountFile.exists()) {
            Tools.dialog(context, "Account already exists", "This local account is already saved. Choose another name.");
            return;
        }

        try {
            // Use the engine's native offline-account defaults.
            MinecraftAccount account = new MinecraftAccount();
            account.username = name;
            account.save();
            PojavProfile.setCurrentProfile(context, name);

            if (PojavProfile.getCurrentProfileContent(context, null) == null) {
                throw new IllegalStateException("Local account was not persisted");
            }
        } catch (Exception e) {
            Tools.dialog(context, "Account error", "Could not save the local account. Please try again.");
            return;
        }

        username.postDelayed(() -> Tools.swapFragment(requireActivity(),
                TestLauncherHomeFragment.class, TestLauncherHomeFragment.TAG, null), 220);
    }
}
