package net.kdt.pojavlaunch.launcher;

import static net.kdt.pojavlaunch.Tools.hasNoOnlineProfileDialog;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.launcher.TestLocalLoginFragment;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;

public class TestSelectAuthFragment extends Fragment {
    public static final String TAG = "TEST_AUTH_SELECT_FRAGMENT";

    public TestSelectAuthFragment() {
        super(R.layout.fragment_test_auth);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View microsoft = view.findViewById(R.id.button_microsoft_authentication);
        View local = view.findViewById(R.id.button_local_authentication);

        microsoft.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null));

        local.setOnClickListener(v ->
                Tools.swapFragment(requireActivity(), TestLocalLoginFragment.class, TestLocalLoginFragment.TAG, null));

        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(220);
        view.startAnimation(a);
    }
}