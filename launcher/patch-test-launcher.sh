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
s = s.replace('resValue "string", "app_name", "Amethyst (Debug)"', 'resValue "string", "app_name", "Test Launcher (Debug)"')
s = s.replace('resValue "string", "app_short_name", "Amethyst (Debug)"', 'resValue "string", "app_short_name", "Test Launcher (Debug)"')
s = s.replace('resValue "string", "app_name", "Amethyst"', 'resValue "string", "app_name", "Test Launcher"')
s = s.replace('resValue "string", "app_short_name", "Amethyst"', 'resValue "string", "app_short_name", "Test Launcher"')
s = s.replace("'org.angelauramc.amethyst.scoped.gamefolder.debug'", "'com.testlauncher.minecraft.scoped.gamefolder.debug'")
s = s.replace("'org.angelauramc.amethyst.scoped.controlfolder.debug'", "'com.testlauncher.minecraft.scoped.controlfolder.debug'")
s = s.replace("'org.angelauramc.amethyst.scoped.gamefolder'", "'com.testlauncher.minecraft.scoped.gamefolder'")
s = s.replace("'org.angelauramc.amethyst.scoped.controlfolder'", "'com.testlauncher.minecraft.scoped.controlfolder'")
s = s.replace("'org.angelauramc.amethyst.debug'", "'com.testlauncher.minecraft.debug'")
s = s.replace("'org.angelauramc.amethyst'", "'com.testlauncher.minecraft'")
p.write_text(s)
PY

mkdir -p "$PKG/launcher" "$RES/layout"
cp "$ROOT_DIR/launcher/TestLauncherHomeFragment.java" "$PKG/launcher/TestLauncherHomeFragment.java"
cp "$ROOT_DIR/launcher/fragment_test_launcher_home.xml" "$RES/layout/fragment_test_launcher_home.xml"

python3 - "$PKG/LauncherActivity.java" <<'PY'
from pathlib import Path
p = Path(__import__("sys").argv[1])
s = p.read_text()
if 'net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment' not in s:
    s = s.replace('import net.kdt.pojavlaunch.fragments.MainMenuFragment;',
                  'import net.kdt.pojavlaunch.fragments.MainMenuFragment;\nimport net.kdt.pojavlaunch.launcher.TestLauncherHomeFragment;')
s = s.replace('if(!(fragment instanceof MainMenuFragment)) return false;',
              'if(!(fragment instanceof MainMenuFragment) && !(fragment instanceof TestLauncherHomeFragment)) return false;')
s = s.replace('.add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT").commit();',
              '.add(R.id.container_fragment, TestLauncherHomeFragment.class, null, "ROOT").commit();')
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

echo "Test Launcher overlay applied."
