# Test Launcher — Feature Contract

This repository builds **Test Launcher**, a real Android launcher for Minecraft Java Edition.

## Core requirements

- Real Minecraft Java runtime/engine on Android; no mock game process and no padded/fake APK.
- One-tap Minecraft launch after an instance/account/version is configured.
- Offline/local account: username only, no password.
- Microsoft account login.
- Official Minecraft Java versions, excluding Alpha/Beta from the normal version picker.
- Loader choices: Vanilla, Fabric, Forge, OptiFine where the selected Minecraft version supports them.
- Real downloads of Minecraft client metadata, libraries, assets, loader files and required Java runtimes.
- Installed versions/instances page with Launch and Delete.
- Mods page with Modrinth, CurseForge and SpigotMC discovery/integration targets.
- RAM manager.
- Renderer selection/settings.
- Touch controls configuration.
- Worlds management.
- Resource-pack management.
- Professional Test Launcher home UI with cards, navigation and animations.

## Product-specific requirements

These are part of the Test Launcher product contract and must not be dropped during implementation:

1. **Offline account** — simple name-based local account.
2. **Microsoft login** — OAuth/device/browser based login; never store a Microsoft password.
3. **Version picker** — release versions in the normal list; Alpha/Beta hidden from normal user flow.
4. **Loader picker** — Vanilla/Fabric/Forge/OptiFine.
5. **Downloads / Instances** — installed versions are visible, launchable and deletable.
6. **Mods** — Modrinth, CurseForge and SpigotMC entry points.
7. **RAM** — user-selectable memory limit with safe device-aware bounds.
8. **Renderer** — renderer options exposed in launcher settings.
9. **Controls** — touch-control editor/configuration.
10. **Worlds** — browse/manage worlds for the selected instance.
11. **Resource packs** — browse/manage packs for the selected instance.
12. **One-click Play** — home screen play action uses the real Minecraft launch pipeline.

## Build strategy

The GitHub Actions build pulls the pinned upstream Android Java launcher engine at build time and applies the Test Launcher overlay. This keeps large upstream/native/JRE artifacts out of this repository while producing a real APK.

The upstream engine is used as an implementation component; Test Launcher owns the product UI, feature contract, branding and integration layer.
