package com.dungeoncrawler.game

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.entity.Enemy
import com.dungeoncrawler.game.entity.Item
import kotlin.random.Random

class DungeonGenerator {
    data class GenerationResult(val playerStart: Pair<Int,Int>, val enemies: List<Enemy>, val items: List<Item>)

    fun generate(map: GameMap, floor: Int): GenerationResult {
        map.reset()
        val enemies = mutableListOf<Enemy>()
        val items = mutableListOf<Item>()
        repeat(Config.MAX_ROOMS) {
            val w = Random.nextInt(Config.MIN_ROOM_SIZE, Config.MAX_ROOM_SIZE+1)
            val h = Random.nextInt(Config.MIN_ROOM_SIZE, Config.MAX_ROOM_SIZE+1)
            val x = Random.nextInt(1, map.width-w-1)
            val y = Random.nextInt(1, map.height-h-1)
            val room = Rect(x,y, x+w, y+h)
            if (map.rooms.any { it.intersects(room) }) return@repeat
            for (rx in room.x1+1 until room.x2) for (ry in room.y1+1 until room.y2) map.tiles[rx][ry] = Config.TILE_FLOOR
            if (map.rooms.isNotEmpty()) {
                val prev = map.rooms.last()
                if (Random.nextBoolean()) { for (cx in minOf(prev.centerX,room.centerX)..maxOf(prev.centerX,room.centerX)) if (map.inBounds(cx,prev.centerY)) map.tiles[cx][prev.centerY] = Config.TILE_FLOOR; for (cy in minOf(prev.centerY,room.centerY)..maxOf(prev.centerY,room.centerY)) if (map.inBounds(room.centerX,cy)) map.tiles[room.centerX][cy] = Config.TILE_FLOOR } else { for (cy in minOf(prev.centerY,room.centerY)..maxOf(prev.centerY,room.centerY)) if (map.inBounds(prev.centerX,cy)) map.tiles[prev.centerX][cy] = Config.TILE_FLOOR; for (cx in minOf(prev.centerX,room.centerX)..maxOf(prev.centerX,room.centerX)) if (map.inBounds(cx,room.centerY)) map.tiles[cx][room.centerY] = Config.TILE_FLOOR }
                val occ = mutableSetOf<Pair<Int,Int>>()
                repeat(Random.nextInt(0,minOf(Config.MAX_ENEMIES,1+floor/2)+1)) { val p=room.randomPoint(); if(p !in occ) { occ.add(p); enemies.add(Enemy.create(p.first,p.second,pickEnemy(floor),floor)) } }
                repeat(Random.nextInt(0,Config.MAX_ITEMS+1)) { val p=room.randomPoint(); if(p !in occ) { occ.add(p); items.add(Item.create(p.first,p.second,pickItem(floor),floor)) } }
            }
            map.rooms.add(room)
        }
        if (map.rooms.size>1) { val l=map.rooms.last(); map.stairsX=l.centerX; map.stairsY=l.centerY; map.tiles[map.stairsX][map.stairsY]=Config.TILE_STAIRS }
        val start=if(map.rooms.isNotEmpty()) Pair(map.rooms.first().centerX,map.rooms.first().centerY) else Pair(5,5)
        return GenerationResult(start,enemies,items)
    }

    private fun pickEnemy(f: Int) = weightedPick(when { f>=8 -> listOf("orc" to 15,"troll" to 20,"demon" to 35,"dragon" to 30); f>=5 -> listOf("goblin" to 20,"skeleton" to 25,"orc" to 25,"troll" to 20,"demon" to 10); f>=3 -> listOf("goblin" to 40,"skeleton" to 30,"orc" to 20,"troll" to 10); else -> listOf("goblin" to 70,"skeleton" to 20,"orc" to 10) })
    private fun pickItem(f: Int) = weightedPick(when { f>=8 -> listOf(Config.ITEM_POTION_HP to 20,Config.ITEM_POTION_STR to 15,Config.ITEM_SCROLL_FIREBALL to 15,Config.ITEM_SCROLL_CONFUSE to 10,Config.ITEM_WEAPON_AXE to 15,Config.ITEM_ARMOR_PLATE to 15,Config.ITEM_GOLD to 10); f>=5 -> listOf(Config.ITEM_POTION_HP to 25,Config.ITEM_SCROLL_FIREBALL to 15,Config.ITEM_SCROLL_LIGHTNING to 10,Config.ITEM_WEAPON_SWORD to 15,Config.ITEM_WEAPON_AXE to 10,Config.ITEM_ARMOR_CHAIN to 10,Config.ITEM_GOLD to 15); f>=3 -> listOf(Config.ITEM_POTION_HP to 35,Config.ITEM_SCROLL_LIGHTNING to 15,Config.ITEM_WEAPON_SWORD to 15,Config.ITEM_ARMOR_LEATHER to 10,Config.ITEM_ARMOR_CHAIN to 5,Config.ITEM_GOLD to 20); else -> listOf(Config.ITEM_POTION_HP to 50,Config.ITEM_WEAPON_DAGGER to 10,Config.ITEM_ARMOR_LEATHER to 10,Config.ITEM_GOLD to 30) })
    private fun <T> weightedPick(t: List<Pair<T,Int>>): T { var a=0; val r=Random.nextInt(1,101); for((v,w) in t) { a+=w; if(r<=a) return v }; return t.last().first }
}
