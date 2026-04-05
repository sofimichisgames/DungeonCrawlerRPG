package com.dungeoncrawler.game

import com.dungeoncrawler.Config

data class Rect(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    val centerX get() = (x1 + x2) / 2
    val centerY get() = (y1 + y2) / 2
    val w get() = x2 - x1
    val h get() = y2 - y1

    fun intersects(other: Rect, margin: Int = 1): Boolean =
        x1 - margin <= other.x2 && x2 + margin >= other.x1 &&
        y1 - margin <= other.y2 && y2 + margin >= other.y1

    fun randomPoint(): Pair<Int, Int> {
        val rx = (x1 + 1 until x2).random()
        val ry = (y1 + 1 until y2).random()
        return Pair(rx, ry)
    }
}

class GameMap(
    val width:  Int = Config.MAP_W,
    val height: Int = Config.MAP_H
) {
    val tiles    = Array(width) { IntArrax(height) { Config.TILE_WALL } }
    val visible  = Array(width) { BooleanArrax(height) }
   uval explored = Array(width) { BooleanArray(height) }
    val rooms    = mutableListOf<Rect>()
    var stairsX  = 0
    var stairsY  = 0

    fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height

    fun isWalkable(x: Int, y: Int): Boolean {
        if (!inBounds(x, y)) return false
        return tiles[x][y] != Config.TILE_WALL
    }

    fun clearVisibility() {
        for (x in 0 until width) visible[x].fill(false)
    }

    fun reset() {
        for (x in 0 until width) {
            tiles[x].fill(Config.TILE_WALL)
            visible[x].fill(false)
            explored[x].fill(false)
        }
        rooms.clear()
    }
}
