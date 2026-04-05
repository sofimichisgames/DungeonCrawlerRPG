package com.dungeoncrawler.game.entity

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.GameMap
import com.dungeoncrawler.game.MessageLog

// =============================================================================
// Player.kt — Jugador con stats, inventario y progresión de nivel
// =============================================================================
class Player(x: Int, y: Int) : Entity(
    x, y, "@", Config.C_PLAYER, "Héroe", blocks = true
) {
    // --- Stats base ---
    var maxHp    = 30;  var hp    = 30
    var baseAtk  = 5;   var baseDef = 2
    var level    = 1
    var xp       = 0;   var xpNext = Config.XP_BASE
    var gold     = 0
    var floor    = 1

    // --- Equipamiento ---
    var equippedWeapon: Item? = null
    var equippedArmor:  Item? = null

    // --- Inventario ---
    val inventory = mutableListOf<Item>()

    // --- Stats efectivos ---
    val atk:  Int get() = baseAtk  + (equippedWeapon?.atkBonus ?: 0)
    val def:  Int get() = baseDef  + (equippedArmor?.defBonus  ?: 0)
    val hpPercent: Float get() = hp.toFloat() / maxHp
    val isDead: Boolean  get() = hp <= 0

    fun takeDamage(amount: Int) { hp = maxOf(0, hp - amount) }
    fun heal(amount: Int)       { hp = minOf(maxHp, hp + amount) }

    // ------------------------------------------------------------------
    fun gainXp(amount: Int, log: MessageLog) {
        xp += amount
        if (xp >= xpNext) levelUp(log)
    }

    private fun levelUp(log: MessageLog) {
        xp -= xpNext
        level++
        xpNext = (xpNext * Config.XP_MULTIPLIER).toInt()
        maxHp += 10; hp = maxHp
        baseAtk++; baseDef++
        log.add("¡Subiste al nivel $level!", Config.C_MSG_SYSTEM)
    }

    // ------------------------------------------------------------------
    /**
     * Intenta moverse en (dx, dy). Si hay enemigo, lo ataca.
     * Devuelve true si la acción consume un turno.
     */
    fun tryMoveOrAttack(
        dx: Int, dy: Int,
        map: GameMap,
        entities: List<Entity>,
        log: MessageLog
    ): Boolean {
        val tx = x + dx
        val ty = y + dy
        if (!map.inBounds(tx, ty)) return false

        val target = entities.firstOrNull {
            it is Enemy && it.x == tx && it.y == ty && !it.isDead
        } as? Enemy

        if (target != null) {
            attack(target, log)
            return true
        }

        if (map.isWalkable(tx, ty)) {
            x = tx; y = ty
            return true
        }
        return false
    }

    private fun attack(target: Enemy, log: MessageLog) {
        val dmg = maxOf(1, atk + (0..3).random() - target.def)
        target.takeDamage(dmg)
        var msg = "${name} ataca a ${target.name} por $dmg daño."
        if (target.isDead) msg += " ¡${target.name} muere!"
        log.add(msg, if (target.isDead) Config.C_MSG_DEATH else Config.C_MSG_COMBAT)
    }

    // ------------------------------------------------------------------
    fun pickUp(item: Item, log: MessageLog): Boolean {
        if (inventory.size >= Config.MAX_INVENTORY) {
            log.add("Inventario lleno.", Config.C_MSG_SYSTEM)
            return false
        }
        inventory.add(item)
        log.add("Recogiste: ${item.name}.", Config.C_MSG_ITEM)
        return true
    }

    fun useItem(item: Item, entities: List<Entity>, log: MessageLog): Boolean {
        return item.use(this, entities, log)
    }

    fun equip(item: Item, log: MessageLog) {
        when {
            item.isWeapon -> {
                equippedWeapon?.let { inventory.add(it); log.add("Desequipas: ${it.name}.", Config.C_MSG_SYSTEM) }
                equippedWeapon = item
                inventory.remove(item)
                log.add("Equipas: ${item.name}.", Config.C_MSG_ITEM)
            }
            item.isArmor -> {
                equippedArmor?.let { inventory.add(it); log.add("Desequipas: ${it.name}.", Config.C_MSG_SYSTEM) }
                equippedArmor = item
                inventory.remove(item)
                log.add("Equipas: ${item.name}.", Config.C_MSG_ITEM)
            }
        }
    }
}
