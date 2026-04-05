package com.dungeoncrawler.game.entity

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.GameMap
import com.dungeoncrawler.game.MessageLog
import kotlin.math.sign
import kotlin.random.Random

// =============================================================================
// Enemy.kt — Enemigos con IA básica
// =============================================================================
class Enemy private constructor(
    x: Int, y: Int,
    char: String, color: Int,
    override val name: String,
    var hp: Int, var maxHp: Int,
    val atk: Int, val def: Int,
    val xpReward: Int
) : Entity(x, y, char, color, name, blocks = true) {

    val isDead: Boolean get() = hp <= 0
    val hpPercent: Float get() = hp.toFloat() / maxHp
    var confusedTurns = 0

    init { renderOrder = 2 }

    fun takeDamage(amount: Int) { hp = maxOf(0, hp - amount) }

    // ------------------------------------------------------------------
    fun takeTurn(player: Player, map: GameMap, entities: List<Entity>, log: MessageLog) {
        if (isDead) return
        if (!map.visible[x][y]) return   // Solo actúan si son visibles

        if (confusedTurns > 0) {
            confusedTurns--
            val dx = Random.nextInt(-1, 2)
            val dy = Random.nextInt(-1, 2)
            val nx = x + dx; val ny = y + dy
            if (map.isWalkable(nx, ny) && !entities.any { it.blocks && it.x == nx && it.y == ny && it !== this })
                x = nx; y = ny
            return
        }

        val dist = distanceTo(player)
        if (dist <= 1.5f) {
            attackPlayer(player, log)
        } else {
            moveToward(player, map, entities)
        }
    }

    private fun attackPlayer(player: Player, log: MessageLog) {
        val dmg = maxOf(1, atk + Random.nextInt(4) - player.def)
        player.takeDamage(dmg)
        var msg = "$name ataca a ${player.name} por $dmg daño."
        if (player.isDead) msg += " ¡Has muerto!"
        log.add(msg, if (player.isDead) Config.C_MSG_DEATH else Config.C_MSG_COMBAT)
    }

    private fun moveToward(player: Player, map: GameMap, entities: List<Entity>) {
        val dx = (player.x - x).sign
        val dy = (player.y - y).sign

        val candidates = listOf(
            Pair(dx, dy), Pair(dx, 0), Pair(0, dy)
        )
        for ((mx, my) in candidates) {
            if (mx == 0 && my == 0) continue
            val nx = x + mx; val ny = y + my
            if (map.isWalkable(nx, ny) &&
                !entities.any { it.blocks && it.x == nx && it.y == ny && it !== this }) {
                x = nx; y = ny
                return
            }
        }
    }

    // ------------------------------------------------------------------
    companion object {
        private data class EnemyDef(
            val char: String, val color: Int, val name: String,
            val hp: Int, val atk: Int, val def: Int, val xp: Int
        )

        private val DEFS = mapOf(
            "goblin"   to EnemyDef("g", Config.C_GOBLIN,   "Goblin",    10, 3, 0,  10),
            "skeleton" to EnemyDef("s", Config.C_SKELETON, "Esqueleto", 16, 4, 1,  20),
            "orc"      to EnemyDef("o", Config.C_ORC,      "Orco",      24, 6, 2,  35),
            "troll"    to EnemyDef("T", Config.C_TROLL,    "Troll",     40, 7, 3,  60),
            "demon"    to EnemyDef("D", Config.C_DEMON,    "Demonio",   30,10, 3,  80),
            "dragon"   to EnemyDef("d", Config.C_DRAGON,   "Dragón",    60,14, 5, 150)
        )

        fun create(x: Int, y: Int, type: String, floor: Int): Enemy {
            val d = DEFS[type] ?: DEFS["goblin"]!!
            val scale = 1f + (floor - 1) * 0.15f
            val hp  = (d.hp  * scale).toInt()
            val atk = (d.atk * scale).toInt()
            val def = (d.def * scale).toInt()
            return Enemy(x, y, d.char, d.color, d.name, hp, hp, atk, def, d.xp)
        }
    }
}
