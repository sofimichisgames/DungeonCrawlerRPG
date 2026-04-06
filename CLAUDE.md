# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Dungeon Crawler RPG** is an Android roguelike game written in Kotlin. The game features procedural dungeon generation, turn-based combat, inventory management, and progressive floor difficulty. Development uses local Gradle builds and direct ADB installation on connected devices.

---

## Building & Running

### Deploy Scripts (Recommended)

**For development iteration (most common):**
```bash
bash build_and_deploy.sh
```
- Builds APK with Java 21
- Installs to device
- Launches app
- Takes 1-2 minutes

**For quick redeploy (after code changes, if APK is fresh):**
```bash
bash deploy.sh
```
- Skips build, uses existing APK
- Fast reinstall (10-15 seconds)
- Perfect for testing UI/logic changes quickly

### Manual Commands
- **Build APK only:** `./gradlew assembleDebug`
- **View logs:** `/c/Android/platform-tools/platform-tools/adb logcat | findstr dungeoncrawler`
- **Lint check:** `./gradlew lint`
- **Release APK:** `./gradlew assembleRelease`

### Project Structure
- **Build system:** Gradle 9.0 with AGP 8.7.0, Kotlin 2.0.0
- **Java version:** **Java 21 LTS** (required - Java 26 has compatibility issues)
- **Target SDK:** Android 34 (API level 34)
- **Min SDK:** 24
- **APK output:** `app/build/outputs/apk/debug/app-debug.apk`

---

## Architecture

### High-Level Flow
```
MainActivity
  └─ GameView (SurfaceView + Game Loop Thread)
      ├─ GameEngine (core game state & logic)
      │   ├─ GameMap (tile grid + visibility)
      │   ├─ DungeonGenerator (procedural generation)
      │   ├─ FovSystem (field of view calculation)
      │   ├─ MessageLog (event history)
      │   └─ entities: Player, Enemy, Item
      ├─ Renderer (draws everything on Canvas)
      └─ VirtualControls (touch input handling)
```

### Core Systems

#### GameEngine (`game/GameEngine.kt`)
- **Central state machine** with states: `STATE_MENU`, `STATE_PLAYING`, `STATE_INVENTORY`, `STATE_GAME_OVER`, `STATE_VICTORY`
- **Turn-based logic:** `playerMove()`, `playerWait()`, `playerPickUp()`, `playerDescend()` trigger `afterPlayerTurn()` for enemy AI
- **Floor progression:** `loadFloor()` generates dungeons and places player/enemies/items
- **Coordinates:** `player` position + `entities` list (Player, Enemy, Item objects)
- Redraw is controlled by `needsRedraw` flag to optimize rendering

#### GameMap (`game/GameMap.kt`)
- **60×40 tile grid** (constants in `Config.kt`)
- Tile types: `TILE_WALL` (0), `TILE_FLOOR` (1), `TILE_STAIRS` (2)
- **Visibility tracking:** `visible[][]` (computed each turn by FovSystem), `explored[][]` (permanently marked)
- Stairs position: `stairsX`, `stairsY`

#### DungeonGenerator (`game/DungeonGenerator.kt`)
- **BSP algorithm** for room generation (20 rooms max, 5–12 tiles each)
- Returns: player start position, enemies, items
- Enemy/item density increases with `currentFloor`

#### FovSystem (`game/FovSystem.kt`)
- **Shadowcasting** algorithm with radius 8 (configurable in Config)
- Updates `gameMap.visible[][]` each turn from player position

#### Entity System
- **Base class:** `Entity` (x, y, char, color, renderOrder, hp, maxHp)
- **Player** (`entity/Player.kt`): stats (atk/def/xp/level), inventory, equipment
- **Enemy** (`entity/Enemy.kt`): 6 types with AI (chase player if visible, melee combat)
- **Item** (`entity/Item.kt`): 12 types (potions, scrolls, weapons, armor, gold)

#### Renderer & UI (`ui/Renderer.kt`, `ui/VirtualControls.kt`)
- **Canvas-based 2D rendering:** HUD bar (HP/XP/level), map tiles (rectangles with borders), entities (colored circles)
- **Touch controls:** D-pad (movement), center button (wait), right side buttons (inv, actions)
- **Inventory screen:** modal overlay, selectable items by key a–z

### Game Configuration (`Config.kt`)
All game constants live here: map dimensions, tile types, states, item types, colors (ARGB).

---

## Key Patterns & Conventions

### Turn-Based Combat
1. Player action (`playerMove`, etc.) calls `afterPlayerTurn()`
2. `afterPlayerTurn()` computes FOV, runs enemy AI, checks win/lose conditions
3. Sets `needsRedraw = true` to trigger next frame render

### Item & Equipment System
- Items have `char`, `color`, `name`, `type` (potion/scroll/weapon/armor/gold)
- Player can equip weapons/armor; effects applied in combat calculation
- Consumables (potions/scrolles) removed from inventory after use

### Message Log
- Centralized event logging: `messageLog.add(text, color)`
- Only 3 most recent messages rendered on screen
- Different colors for combat, items, system, death

### Rendering & Redraw Optimization
- Draw only when `engine.needsRedraw = true`
- `GameThread` runs at 60 FPS but skips frames if no redraw needed
- Renderer calculates cell size dynamically based on screen dimensions and map grid

---

## Important Files & Quick Navigation

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Android app entry point |
| `GameView.kt` | SurfaceView, game loop thread, input routing |
| `game/GameEngine.kt` | Core game state machine & turn logic |
| `game/GameMap.kt` | Tile grid, visibility, stairs |
| `game/DungeonGenerator.kt` | Procedural dungeon BSP generation |
| `game/FovSystem.kt` | Shadowcasting field-of-view |
| `entity/Player.kt` | Player stats, inventory, combat |
| `entity/Enemy.kt` | Enemy types & AI |
| `entity/Item.kt` | Item definitions & effects |
| `ui/Renderer.kt` | Canvas drawing: HUD, map, entities, UI |
| `ui/VirtualControls.kt` | Touch input handling |
| `Config.kt` | Global constants & color palette |

---

## Common Development Tasks

### Adding a new enemy type
1. Add type constant to `Config.kt` (e.g., `ENEMY_ZOMBIE`)
2. Create subclass or extend `Enemy` logic in `entity/Enemy.kt` with new stats/ai
3. Update `DungeonGenerator.kt` to spawn it on certain floors

### Adding a new item or effect
1. Define item type in `Config.kt` (e.g., `ITEM_SCROLL_HEAL`)
2. Add item class/logic to `entity/Item.kt`
3. Update `DungeonGenerator.kt` item pool
4. Handle in Player's `useItem()` method

### Adjusting difficulty or balance
- Tweak `Config.kt`: `MAX_ENEMIES`, `MAX_ITEMS`, `FOV_RADIUS`, `XP_MULTIPLIER`
- Or tune `DungeonGenerator.kt` spawn logic

### Debugging rendering issues
- Check `Renderer.drawMap()` and `drawEntities()` for canvas calls
- Verify tile/entity positions in `GameMap` and entity coordinates
- Adjust `VirtualControls` if touch input seems offset

---

## Local Development Workflow

### Prerequisites & One-Time Setup

1. **Java 21 LTS:** Download and install from [Eclipse Temurin](https://adoptium.net/) or [OpenJDK](https://jdk.java.net/21/)
   - After installation, set `JAVA_HOME` environment variable
   - Verify: `java -version` (should show 21.x.x)
   - **⚠️ Important:** Java 26 has compatibility issues with the current AGP/Gradle version

2. **Android SDK:** Ensure you have an Android SDK with API 34 installed at `C:\Android` (or set `ANDROID_HOME` env var)

3. **ADB (Android Debug Bridge):** Installed with Android SDK at `/c/Android/platform-tools/platform-tools/adb`
   - To add to PATH for easy access: `setx PATH "%PATH%;C:\Android\platform-tools\platform-tools"`

4. **Accept SDK licenses:**
   ```bash
   /c/Android/platform-tools/platform-tools/adb shell echo "Licenses accepted"
   ```
   (or use `sdkmanager --licenses` from Android SDK cmdline-tools)

5. **Create `local.properties`** in project root with:
   ```
   sdk.dir=C:\\Android
   ```

6. **Gradle version:** Already locked in `gradle/wrapper/gradle-wrapper.properties` to Gradle 9.0 (compatible with AGP 8.7.0)

### Detailed Build & Test Steps

**1. Build APK**
```bash
./gradlew clean assembleDebug
```
- Output: `app/build/outputs/apk/debug/app-debug.apk`
- Typical time: 2–3 minutes (faster for incremental builds)

**2. Verify device connection**
```bash
/c/Android/platform-tools/platform-tools/adb devices
```
- Should show device as "device" (not "offline" or "unauthorized")

**3. Install APK**
```bash
/c/Android/platform-tools/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```
- `-r` flag reinstalls if already present
- Look for "Success" in output

**4. Launch app**
```bash
/c/Android/platform-tools/platform-tools/adb shell am start -n com.dungeoncrawler/.MainActivity
```

**5. Monitor logs**
```bash
/c/Android/platform-tools/platform-tools/adb logcat | findstr "dungeoncrawler"
```

**Lint check (before committing)**
```bash
./gradlew lint
```

### Typical Iteration Loop
1. Make code changes in Kotlin files
2. **Build & deploy:** `bash build_and_deploy.sh` (1-2 min) or just `bash deploy.sh` if APK is fresh (10 sec)
3. Verify changes on device
4. For quick iterations: repeat steps 1-2 with `deploy.sh`
5. Check logs if needed: `/c/Android/platform-tools/platform-tools/adb logcat | findstr dungeoncrawler`
6. Commit when satisfied: `git add -A && git commit -m "..."`

### Deploy Scripts Summary

**`build_and_deploy.sh`** — Full build + install + launch
- Sets JAVA_HOME to Java 21
- Stops Gradle daemon (avoids Java 26 issues)
- Builds APK from scratch
- Installs and launches
- **Use when:** Making code changes

**`deploy.sh`** — Fast redeploy (no rebuild)
- Skips build, uses existing APK
- Removes old version and installs new
- Launches app
- ~10-15 seconds
- **Use when:** Testing quick changes after recent build

Both scripts require:
- Device connected via USB
- USB debugging enabled
- `/c/Android/platform-tools/platform-tools/adb` accessible

### Troubleshooting

- **"JAVA_HOME is not set"** → Install Java 21 and set `JAVA_HOME=C:\Program Files\Java\jdk-21.0.10`
- **"Java 26 incompatibility"** → Use Java 21 LTS instead. Java 26 causes "Unsupported class file major version 70" errors
- **"adb: command not found"** → Use full path: `/c/Android/platform-tools/platform-tools/adb` or add to PATH
- **"Device offline"** → Reconnect USB, enable USB debugging on device, run `adb kill-server && adb devices`
- **"Activity does not exist"** → Ensure app package is `com.dungeoncrawler` and MainActivity is in correct location
- **App crashes on device** → Check logs: `/c/Android/platform-tools/platform-tools/adb logcat | findstr "dungeoncrawler"`
