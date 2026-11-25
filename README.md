# Prism Client

A client-side Fabric mod for **Minecraft 1.21.11** and **26.2** with a redesigned in-game menu.
Cosmetic and quality-of-life only (HUD widgets and zoom). No combat or movement advantages.

**Open the menu with `Right Shift`** (rebind under Options > Controls > Prism Client).
Zoom is `C` (hold). Both keys are rebindable.

## Menu

- Animated sidebar with category tabs and live enabled counts
- Cards with smooth toggle switches, expandable settings (switches, sliders, mode pickers)
- Type anywhere to search; Esc clears the search, Esc again closes
- Left-click a card to expand it, right-click it to toggle. Click the dimmed area to close
- Accent color, background dim and animations are configurable under Interface
- Settings persist to `config/prism-client.properties`

## Features

HUD: FPS, Coordinates (+direction), CPS, Keystrokes, Clock. Visual: Zoom (FOV based), Swing Speed and Hand Glow (experimental), both via small mixins.

## Layout

```
common/      shared code: menu UI, HUD, config. Pure Java, no Minecraft imports
mc-1.21.11/  Fabric project (Java 21, Mojang mappings, remapping Loom)
mc-26.2/     Fabric project (Java 25, unobfuscated, non-remapping Loom)
```

Each version only contains small adapters (`Gfx`, `Game`, `MenuScreen`, `PrismClient`);
everything visual is in `common/`, so the menu looks identical on both.

## Building

1. Install JDK 21 (for 1.21.11) and JDK 25 (for 26.2).
2. Each `mc-*` folder is its own Gradle project. Copy `gradlew`, `gradlew.bat` and the
   `gradle/wrapper/` folder into it from the matching branch of
   https://github.com/FabricMC/fabric-example-mod (branch `1.21.11` and branch `26.2`).
3. In the folder, run `./gradlew build`. The jar is in `build/libs/`.
4. If a dependency version fails to resolve, take the current values from https://fabricmc.net/develop
   and update `gradle.properties`.

## If something does not compile

I could compile and test the shared code (`common/`) but not the Minecraft adapters, since they need
the game jars. These are the lines most likely to need a tweak:

- `PrismClient`: Fabric API helper names/packages (`KeyBindingHelper` on 1.21.11, `KeyMappingHelper` on 26.x)
- `MenuScreen`: input method signatures (`MouseButtonEvent`, `KeyEvent`, `CharacterEvent`)
- 26.2 `Compat.java`: `setScreenAndShow` vs `gui.setScreen`, `gui.screen()`
- `Gfx`: text/scissor method names
- `Game`: `getFps()` and the `options.keyUp` style fields

## Adding a feature

Register it in `Features.java`, then draw it in `HudRenderer.java` (HUD) or add a tick hook in `PrismClient`.
The menu picks it up automatically.

## Building without installing anything (GitHub Actions)

1. Create a new GitHub repository and upload the contents of this folder (including `.github/`).
2. Open the repo's **Actions** tab. The "Build jars" workflow runs on every push (or press "Run workflow").
3. When it finishes, open the run and download the artifacts `prism-client-mc-1.21.11` and `prism-client-mc-26.2`.
   Each contains the `.jar` for that version.
