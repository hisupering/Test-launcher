# Simple Engine

Simple Engine is the native engine layer for Simple Launcher.

## Design goal

Simple Engine is independent of PojavLauncher/Amethyst. It owns:
- Java runtime discovery
- Minecraft instance launch configuration
- game/library/assets paths
- process lifecycle
- renderer/input/audio integration points
- future LWJGL/GL bridge

The engine is being built incrementally. The first milestone is a clean, independently owned launch/runtime core; renderer and native bridges are separate milestones.

## Layout

- `core/` - platform-independent engine logic
- `android/` - Android process/runtime integration
- `runtime/` - Java runtime discovery and validation
- `game/` - Minecraft instance metadata and launch preparation
- `native/` - future JNI/LWJGL/OpenGL/audio/input bridge

No PojavLauncher or Amethyst source is required by this module.
