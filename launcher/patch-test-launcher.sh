#!/usr/bin/env bash
set -euo pipefail

ENGINE_DIR="${1:?engine directory required}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

APP="$ENGINE_DIR/app_pojavlauncher"
PKG="$APP/src/main/java/net/kdt/pojavlaunch"
RES="$APP/src/main/res"

python3 - "$APP/build.gradle" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
s = s.replace('applicationId "org.angelauramc.amethyst"', 'applicationId "com.testlauncher.minecraft"')
s = s.replace('resValue "string", "app_name", "Amethyst (Debug)"', 'resValue "string", "app_name", "Simple Launcher (Debug)"')
s = s.replace('resValue "string", "app_short_name", "Amethyst (Debug)"', 'resValue "string", "app_short_name", "Simple Launcher (Debug)"')
s = s.replace('resValue "string", "app_name", "Amethyst"', 'resValue "string", "app_name", "Simple Launcher"')
s = s.replace('resValue "string", "app_short_name", "Amethyst"', 'resValue "string", "app_short_name", "Simple Launcher"')
s = s.replace("'org.angelauramc.amethyst.scoped.gamefolder.debug'", "'com.testlauncher.minecraft.scoped.gamefolder.debug'")
s = s.replace("'org.angelauramc.amethyst.scoped.controlfolder.debug'", "'com.testlauncher.minecraft.scoped.controlfolder.debug'")
s = s.replace("'org.angelauramc.amethyst.scoped.gamefolder'", "'com.testlauncher.minecraft.scoped.gamefolder'")
s = s.replace("'org.angelauramc.amethyst.scoped.controlfolder'", "'com.testlauncher.minecraft.scoped.controlfolder'")
s = s.replace("'org.angelauramc.amethyst.debug'", "'com.testlauncher.minecraft.debug'")
s = s.replace("'org.angelauramc.amethyst'", "'com.testlauncher.minecraft'")
p.write_text(s)
PY

mkdir -p "$PKG/launcher" "$RES/layout" "$RES/drawable"
cp "$ROOT_DIR/launcher/TestLauncherHomeFragment.java" "$PKG/launcher/TestLauncherHomeFragment.java"
cp "$ROOT_DIR/launcher/TestSelectAuthFragment.java" "$PKG/launcher/TestSelectAuthFragment.java"
cp "$ROOT_DIR/launcher/TestLocalLoginFragment.java" "$PKG/launcher/TestLocalLoginFragment.java"
cp "$ROOT_DIR/launcher/fragment_test_launcher_home.xml" "$RES/layout/fragment_test_launcher_home.xml"
cp "$ROOT_DIR/launcher/fragment_test_auth.xml" "$RES/layout/fragment_test_auth.xml"
cp "$ROOT_DIR/launcher/fragment_test_local_login.xml" "$RES/layout/fragment_test_local_login.xml"
cp "$ROOT_DIR/launcher/simple_launcher_logo.xml" "$RES/drawable/simple_launcher_logo.xml"

python3 - "$PKG/LauncherActivity.java" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()

if 'net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment' not in s:
    s = s.replace(
        'import net.kdt.pojavlaunch.fragments.MainMenuFragment;',
        'import net.kdt.pojavlaunch.fragments.MainMenuFragment;\n'
        'import net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment;\n'
        'import net.kdt.pojavlaunch.launcher.TestSelectAuthFragment;\n'
        'import net.kdt.pojavlaunch.PojavProfile;'
    )

s = s.replace(
    'if(!(fragment instanceof MainMenuFragment)) return false;',
    'if(!(fragment instanceof MainMenuFragment) && !(fragment instanceof TestLauncherHomeFragment) && !(fragment instanceof TestSelectAuthFragment)) return false;'
)

s = s.replace(
    'Tools.swapFragment(this, SelectAuthFragment.class, SelectAuthFragment.TAG, null);',
    'Tools.swapFragment(this, TestSelectAuthFragment.class, TestSelectAuthFragment.TAG, null);'
)

old_root = '.add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT").commit();'
new_root = '''.add(R.id.container_fragment,
                        PojavProfile.getCurrentProfileContent(this, null) != null
                                ? TestLauncherHomeFragment.class
                                : TestSelectAuthFragment.class,
                        null, "ROOT").commit();'''
s = s.replace(old_root, new_root)
p.write_text(s)
PY
python3 - "$PKG/Tools.java" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
if 'net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment' not in s and 'MainMenuFragment' in s:
    s = s.replace('import net.kdt.pojavlaunch.fragments.MainMenuFragment;',
                  'import net.kdt.pojavlaunch.fragments.MainMenuFragment;\nimport net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment;')
s = s.replace('swapFragment(activity, MainMenuFragment.class, MainMenuFragment.TAG, null);',
              'swapFragment(activity, TestLauncherHomeFragment.class, TestLauncherHomeFragment.TAG, null);')
p.write_text(s)
PY

mkdir -p "$APP/src/main/assets/test_launcher"
cp "$ROOT_DIR/TEST_LAUNCHER_FEATURES.md" "$APP/src/main/assets/test_launcher/FEATURES.md"

python3 - "$RES/values/colors.xml" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
s = s.replace('<color name="minebutton_color">#9649b8</color>', '<color name="minebutton_color">#22C55E</color>')
s = s.replace('<color name="background_app">#181818</color>', '<color name="background_app">#0B1710</color>')
s = s.replace('<color name="background_overlay">#464646</color>', '<color name="background_overlay">#183321</color>')
s = s.replace('<color name="background_status_bar">#242424</color>', '<color name="background_status_bar">#102016</color>')
s = s.replace('<color name="background_bottom_bar">#232323</color>', '<color name="background_bottom_bar">#0F1D14</color>')
p.write_text(s)
PY

python3 - "$APP/src/main/AndroidManifest.xml" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
s = s.replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/simple_launcher_logo"')
s = s.replace('android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@drawable/simple_launcher_logo"')
p.write_text(s)
PY

python3 - "$PKG/LauncherActivity.java" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
if 'net.kdt.pojavlaunch.value.MinecraftAccount;' not in s:
    s = s.replace(
        'import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;',
        'import net.kdt.pojavlaunch.value.MinecraftAccount;\n'
        'import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;'
    )

old = '''        // Simple Launcher can create a local account after the account spinner
        // has already been initialized. Prefer the persisted current profile when
        // the spinner has a stale null selection.
        MinecraftAccount selectedAccount = mAccountSpinner.getSelectedAccount();
        if (selectedAccount == null) {
            selectedAccount = PojavProfile.getCurrentProfileContent(this, null);
        }
        if (selectedAccount == null) {
            Toast.makeText(this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show();
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return false;
        }

        // Override whatever version is in use and replace it with lwjgl3ify if needed'''
new = '''        // The custom Simple Launcher account screen can create a Local Account
        // after the Account Spinner has already initialized. In that case the spinner
        // can still hold a stale null selection. Use the persisted current profile
        // as the authoritative fallback.
        MinecraftAccount selectedAccount = mAccountSpinner.getSelectedAccount();
        if (selectedAccount == null) {
            selectedAccount = PojavProfile.getCurrentProfileContent(this, null);
        }
        if (selectedAccount == null) {
            Toast.makeText(this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show();
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return false;
        }

        // Override whatever version is in use and replace it with lwjgl3ify if needed'''
s=s.replace(old,new)
s=s.replace('selectedAccount.isLocal()', 'selectedAccount.isLocal()')
s=s.replace('selectedAccount.isDemo()', 'selectedAccount.isDemo()')
p.write_text(s)
PY

python3 - "$PKG/Tools.java" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
s = s.replace('public static String APP_NAME = "Amethyst";', 'public static String APP_NAME = "Simple Launcher";')
p.write_text(s)
PY

echo "Simple Launcher overlay applied."
