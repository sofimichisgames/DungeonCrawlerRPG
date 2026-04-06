# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Dungeon Crawler RPG** is an Android roguelike game written in Kotlin. The game features procedural dungeon generation, turn-based combat, inventory management, and progressive floor difficulty. After finishing work, **always push changes to GitHub** — this triggers a GitHub Actions pipeline that builds the APK.

---

## Building & Running

### Build Commands
- **Assemble debug APK:** `./gradlew assembleDebug`
- **Assemble release APK:** `./gradlew assembleRelease`
- **Run on connected device:** `./gradlew installDebug` (requires device connected via USB with debugging enabled)
- **Lint check:** `./gradlew lint`

### Project Structure
- **Build system:** Gradle 8.1.0 with Kotlin 1.9.0
- **Target SDK:** Android 34 (API level 34)
- **Min SDK:** 24
- **Java/JVM target:** 17
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

## CI/CD & Deployment

After making changes, **push to GitHub** with:
```bash
git add -A
git commit -m "Description of changes"
git push origin main
```

This triggers a GitHub Actions pipeline that:
1. Builds the APK (debug & release)
2. Stores output in artifacts

The pipeline is configured in `.github/workflows/` (not yet created, but expected).
