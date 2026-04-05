package com.dungeoncrawler.game

import com.dungeoncrawler.Config
import kotlin.math.abs
import kotlin.math.sqrt

// =============================================================================
// FovSystem.kt — Campo de visión con Line-of-Sight por Bresenham
// =============================================================================
object FovSystem {

    fun compute(map: GameMap, ox: Int, oy: Int, radius: Int = Config.FOV_RADIUS) {
        map.clearVisibility()
        // La celda del jugador siempre es visible
        setVisible(map, ox, oy)

        // Revisar todas las celdas dentro del radio
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                if (dx * dx + dy * dy > radius * radius) continue
                val tx = ox + dx
                val ty = oy + dy
                if (!map.inBounds(tx, ty)) continue
                if (hasLineOfSight(map, ox, oy, tx, ty)) {
                    setVisible(map, tx, ty)
                }
            }
        }
    }

    private fun setVisible(map: GameMap, x: Int, y: Int) {
        map.visible[x][y]  = true
        map.explored[x][y] = true
    }

    /**
     * Bresenham's line — devuelve true si no hay paredes entre los dos puntos
     * (se permiten las paredes en el punto de destino, para ver la pared).
     */
    private fun hasLineOfSight(map: GameMap, x0: Int, y0: Int, x1: Int, y1: Int): Boolean {
        var cx = x0; var cy = y0
        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx - dy

        while (true) {
            if (cx == x1 && cy == y1) return true
            if (!map.inBounds(cx, cy)) return false
            if (map.tiles[cx][cy] == Config.TILE_WALL) return false   // Pared bloquea

            val e2 = 2 * err
            if (e2 > -dy) { err -= dy; cx += sx }
            if (e2 <  dx) { err += dx; cy += sy }
        }
    }
}
