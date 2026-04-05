package com.dungeoncrawler.game.entity

import kotlin.math.abs
import kotlin.math.sqrt

// =============================================================================
// Entity.kt — Clase base para todo objeto en el mapa
// =============================================================================
abstract class Entity(
    var x: Int,
    var y: Int,
    val char:     String,
    val color:    Int,
    open val name: String,
    val blocks:   Boolean = false
) {
    var renderOrder: Int = 1

    fun distanceTo(other: Entity): Float =
        sqrt(((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toFloat())
}
