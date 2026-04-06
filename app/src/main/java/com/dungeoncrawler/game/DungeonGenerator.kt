package com.dungeoncrawler.game

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.entity.Enemy
import com.dungeoncrawler.game.entity.Item
import kotlin.random.Random

// =============================================================================
// DungeonGenerator.kt — Generación procedural: salas + corredores
// =============================================================================
class DungeonGenerator {

    data class GenerationResult(
        val playerStart: Pair<Int, Int>,
        val enemies: List<Enemy>,
        val items:   List<Item>
    )

    fun generate(map: GameMap, floor: Int): GenerationResult {
        map.reset()

        val enemies = mutableListOf<Enemy>()
        val items   = mutableListOf<Item>()

        repeat(Config.MAX_ROOMS) {
            val w  = Random.nextInt(Config.MIN_ROOM_SIZE, Config.MAX_ROOM_SIZE + 1)
            val h  = Random.nextInt(Config.MIN_ROOM_SIZE, Config.MAX_ROOM_SIZE + 1)
            val x  = Random.nextInt(1, map.width  - w - 1)
            val y  = Random.nextInt(1, map.height - h - 1)
            val room = Rect(x, y, x + w, y + h)

            if (map.rooms.any { it.intersects(room) }) return@repeat

            carveRoom(map, room)

            if (map.rooms.isNotEmpty()) {
                connectRooms(map, room, map.rooms.last())
                populateRoom(map, room, floor, enemies, items)
            }
            map.rooms.add(room)
        }

        // Escaleras en la última sala
        if (map.rooms.size > 1) {
            val last = map.rooms.last()
            map.stairsX = last.centerX
            map.stairsY = last.centerY
            map.tiles[map.stairsX][map.stairsY] = Config.TILE_STAIRS
        }

        val start = if (map.rooms.isNotEmpty())
            Pair(map.rooms.first().centerX, map.rooms.first().centerY)
        else Pair(5, 5)

        return GenerationResult(start, enemies, items)
    }

    // ------------------------------------------------------------------
    private fun carveRoom(map: GameMap, r: Rect) {
        for (x in r.x1 + 1 until r.x2)
            for (y in r.y1 + 1 until r.y2)
                map.tiles[x][y] = Config.TILE_FLOOR
    }

    private fun connectRooms(map: GameMap, a: Rect, b: Rect) {
        if (Random.nextBoolean()) {
            carveHorizTunnel(map, a.centerX, b.centerX, a.centerY)
            carveVertTunnel(map, a.centerY, b.centerY, b.centerX)
        } else {
            carveVertTunnel(map, a.centerY, b.centerY, a.centerX)
            carveHorizTunnel(map, a.centerX, b.centerX, b.centerY)
        }
    }

    private fun carveHorizTunnel(map: GameMap, x1: Int, x2: Int, y: Int) {
        for (x in minOf(x1, x2)..maxOf(x1, x2))
            if (map.inBounds(x, y)) map.tiles[x][y] = Config.TILE_FLOOR
    }

    private fun carveVertTunnel(map: GameMap, y1: Int, y2: Int, x: Int) {
        for (y in minOf(y1, y2)..maxOf(y1, y2))
            if (map.inBounds(x, y)) map.tiles[x][y] = Config.TILE_FLOOR
    }

    // ------------------------------------------------------------------
    private fun populateRoom(
        map: GameMap, room: Rect, floor: Int,
        enemies: MutableList<Enemy>, items: MutableList<Item>
    ) {
        val occupied = mutableSetOf<Pair<Int, Int>>()

        val numEnemies = Random.nextInt(0, minOf(Config.MAX_ENEMIES, 1 + floor / 2) + 1)
        repeat(numEnemies) {
            val pos = room.randomPoint()
            if (pos !in occupied) {
                occupied.add(pos)
                enemies.add(Enemy.create(pos.first, pos.second, pickEnemy(floor), floor))
            }
        }

        val numItems = Random.nextInt(0, Config.MAX_ITEMS + 1)
        repeat(numItems) {
            val pos = room.randomPoint()
            if (pos !in occupied) {
                occupied.add(pos)
                items.add(Item.create(pos.first, pos.second, pickItem(floor), floor))
            }
        }
    }

    // ------------------------------------------------------------------
    // Tablas de probabilidades
    // ------------------------------------------------------------------
    private fun pickEnemy(floor: Int): String {
        val table = when {
            floor >= 8 -> listOf("orc" to 15, "troll" to 20, "demon" to 35, "dragon" to 30)
            floor >= 5 -> listOf("goblin" to 20, "skeleton" to 25, "orc" to 25, "troll" to 20, "demon" to 10)
            floor >= 3 -> listOf("goblin" to 40, "skeleton" to 30, "orc" to 20, "troll" to 10)
            else       -> listOf("goblin" to 70, "skeleton" to 20, "orc" to 10)
        }
        return weightedPick(table)
    }

    private fun pickItem(floor: Int): String {
        val table = when {
            floor >= 45 -> listOf(
                Config.ITEM_POTION_HP to 10, Config.ITEM_POTION_STR to 15,
                Config.ITEM_SCROLL_FIREBALL to 8, Config.ITEM_SCROLL_CONFUSE to 8,
                Config.ITEM_WEAPON_GREATSWORD to 25, Config.ITEM_WEAPON_WARHAMMER to 15,
                Config.ITEM_ARMOR_CRYSTAL to 8, Config.ITEM_ARMOR_DRAGON to 8, Config.ITEM_ARMOR_MITHRIL to 10, Config.ITEM_GOLD to 9)
            floor >= 42 -> listOf(
                Config.ITEM_POTION_HP to 12, Config.ITEM_POTION_STR to 14,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_CONFUSE to 8,
                Config.ITEM_WEAPON_GREATSWORD to 22, Config.ITEM_WEAPON_WARHAMMER to 15,
                Config.ITEM_WEAPON_HALBERD to 8, Config.ITEM_ARMOR_DRAGON to 7, Config.ITEM_ARMOR_MITHRIL to 8, Config.ITEM_ARMOR_FULL_PLATE to 8, Config.ITEM_GOLD to 11)
            floor >= 38 -> listOf(
                Config.ITEM_POTION_HP to 14, Config.ITEM_POTION_STR to 12,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_CONFUSE to 8,
                Config.ITEM_WEAPON_GREATSWORD to 18, Config.ITEM_WEAPON_WARHAMMER to 16,
                Config.ITEM_WEAPON_HALBERD to 10, Config.ITEM_ARMOR_MITHRIL to 8, Config.ITEM_ARMOR_FULL_PLATE to 10, Config.ITEM_GOLD to 10)
            floor >= 35 -> listOf(
                Config.ITEM_POTION_HP to 15, Config.ITEM_POTION_STR to 12,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_CONFUSE to 8,
                Config.ITEM_WEAPON_GREATSWORD to 14, Config.ITEM_WEAPON_WARHAMMER to 18,
                Config.ITEM_WEAPON_HALBERD to 12, Config.ITEM_ARMOR_FULL_PLATE to 10, Config.ITEM_ARMOR_SPLINT to 8, Config.ITEM_GOLD to 9)
            floor >= 32 -> listOf(
                Config.ITEM_POTION_HP to 16, Config.ITEM_POTION_STR to 11,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_CONFUSE to 7,
                Config.ITEM_WEAPON_WARHAMMER to 16, Config.ITEM_WEAPON_HALBERD to 14,
                Config.ITEM_WEAPON_MACE to 10, Config.ITEM_ARMOR_FULL_PLATE to 10, Config.ITEM_ARMOR_SPLINT to 10, Config.ITEM_GOLD to 8)
            floor >= 28 -> listOf(
                Config.ITEM_POTION_HP to 17, Config.ITEM_POTION_STR to 10,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_LIGHTNING to 8,
                Config.ITEM_WEAPON_HALBERD to 16, Config.ITEM_WEAPON_WARHAMMER to 14,
                Config.ITEM_WEAPON_MACE to 12, Config.ITEM_ARMOR_SPLINT to 10, Config.ITEM_ARMOR_SCALE to 8, Config.ITEM_GOLD to 9)
            floor >= 25 -> listOf(
                Config.ITEM_POTION_HP to 18, Config.ITEM_POTION_STR to 10,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_LIGHTNING to 8,
                Config.ITEM_WEAPON_HALBERD to 14, Config.ITEM_WEAPON_MACE to 14,
                Config.ITEM_WEAPON_AXE to 10, Config.ITEM_ARMOR_SPLINT to 8, Config.ITEM_ARMOR_SCALE to 10, Config.ITEM_GOLD to 8)
            floor >= 22 -> listOf(
                Config.ITEM_POTION_HP to 19, Config.ITEM_POTION_STR to 9,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_LIGHTNING to 8,
                Config.ITEM_WEAPON_MACE to 14, Config.ITEM_WEAPON_HALBERD to 12,
                Config.ITEM_WEAPON_SCIMITAR to 10, Config.ITEM_WEAPON_AXE to 8, Config.ITEM_ARMOR_SPLINT to 10, Config.ITEM_ARMOR_SCALE to 10, Config.ITEM_GOLD to 8)
            floor >= 20 -> listOf(
                Config.ITEM_POTION_HP to 20, Config.ITEM_POTION_STR to 9,
                Config.ITEM_SCROLL_FIREBALL to 10, Config.ITEM_SCROLL_LIGHTNING to 8,
                Config.ITEM_WEAPON_MACE to 16, Config.ITEM_WEAPON_SCIMITAR to 10,
                Config.ITEM_WEAPON_AXE to 8, Config.ITEM_ARMOR_SCALE to 12, Config.ITEM_ARMOR_SPLINT to 8, Config.ITEM_GOLD to 7)
            floor >= 17 -> listOf(
                Config.ITEM_POTION_HP to 21, Config.ITEM_SCROLL_FIREBALL to 10,
                Config.ITEM_SCROLL_LIGHTNING to 8, Config.ITEM_WEAPON_AXE to 14,
                Config.ITEM_WEAPON_SCIMITAR to 12, Config.ITEM_WEAPON_MACE to 12,
                Config.ITEM_WEAPON_SPEAR to 10, Config.ITEM_ARMOR_SCALE to 10, Config.ITEM_ARMOR_HIDE to 8, Config.ITEM_GOLD to 7)
            floor >= 14 -> listOf(
                Config.ITEM_POTION_HP to 22, Config.ITEM_SCROLL_LIGHTNING to 10,
                Config.ITEM_SCROLL_FIREBALL to 8, Config.ITEM_WEAPON_SCIMITAR to 14,
                Config.ITEM_WEAPON_AXE to 12, Config.ITEM_WEAPON_SPEAR to 10,
                Config.ITEM_ARMOR_SCALE to 10, Config.ITEM_ARMOR_HIDE to 12, Config.ITEM_GOLD to 4)
            floor >= 12 -> listOf(
                Config.ITEM_POTION_HP to 24, Config.ITEM_SCROLL_LIGHTNING to 10,
                Config.ITEM_WEAPON_AXE to 14, Config.ITEM_WEAPON_SWORD to 12,
                Config.ITEM_WEAPON_SPEAR to 10, Config.ITEM_ARMOR_SCALE to 12,
                Config.ITEM_ARMOR_HIDE to 12, Config.ITEM_GOLD to 8)
            floor >= 10 -> listOf(
                Config.ITEM_POTION_HP to 26, Config.ITEM_SCROLL_LIGHTNING to 10,
                Config.ITEM_WEAPON_SWORD to 14, Config.ITEM_WEAPON_AXE to 12,
                Config.ITEM_WEAPON_SPEAR to 10, Config.ITEM_ARMOR_SCALE to 10,
                Config.ITEM_ARMOR_HIDE to 10, Config.ITEM_GOLD to 8)
            floor >= 8 -> listOf(
                Config.ITEM_POTION_HP to 28, Config.ITEM_SCROLL_LIGHTNING to 10,
                Config.ITEM_WEAPON_SWORD to 14, Config.ITEM_WEAPON_BOW to 10,
                Config.ITEM_WEAPON_SPEAR to 8, Config.ITEM_ARMOR_SCALE to 8,
                Config.ITEM_ARMOR_HIDE to 14, Config.ITEM_GOLD to 8)
            floor >= 6 -> listOf(
                Config.ITEM_POTION_HP to 30, Config.ITEM_SCROLL_LIGHTNING to 10,
                Config.ITEM_WEAPON_SWORD to 14, Config.ITEM_WEAPON_BOW to 10,
                Config.ITEM_ARMOR_HIDE to 14, Config.ITEM_ARMOR_SCALE to 8,
                Config.ITEM_GOLD to 14)
            floor >= 4 -> listOf(
                Config.ITEM_POTION_HP to 34, Config.ITEM_SCROLL_LIGHTNING to 8,
                Config.ITEM_WEAPON_SWORD to 12, Config.ITEM_WEAPON_BOW to 8,
                Config.ITEM_ARMOR_HIDE to 16, Config.ITEM_ARMOR_LEATHER to 8, Config.ITEM_GOLD to 22)
            floor >= 2 -> listOf(
                Config.ITEM_POTION_HP to 40, Config.ITEM_WEAPON_DAGGER to 10,
                Config.ITEM_ARMOR_HIDE to 12, Config.ITEM_ARMOR_LEATHER to 8, Config.ITEM_GOLD to 36)
            else       -> listOf(
                Config.ITEM_POTION_HP to 45, Config.ITEM_WEAPON_DAGGER to 12,
                Config.ITEM_ARMOR_HIDE to 10, Config.ITEM_ARMOR_LEATHER to 12, Config.ITEM_GOLD to 30)
        }
        return weightedPick(table)
    }

    private fun <T> weightedPick(table: List<Pair<T, Int>>): T {
        val roll = Random.nextInt(1, 101)
        var acc  = 0
        for ((value, weight) in table) {
            acc += weight
            if (roll <= acc) return value
        }
        return table.last().first
    }
}
