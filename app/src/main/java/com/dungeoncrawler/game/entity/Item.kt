package com.dungeoncrawler.game.entity

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.MessageLog
import kotlin.random.Random

class Item(
    x: Int, y: Int,
    char:  String,
    color: Int,
    override val name: String,
    val type:     String,
    val atkBonus: Int = 0,
    val defBonus: Int = 0,
    val hpBonus:  Int = 0,
    val goldValue:Int = 0
) : Entity(x, y, char, color, name, blocks = false) {

    val isWeapon: Boolean get() = type in listOf(
        Config.ITEM_WEAPON_DAGGER, Config.ITEM_WEAPON_SWORD, Config.ITEM_WEAPON_AXE
    )
    val isArmor: Boolean get() = type in listOf(
        Config.ITEM_ARMOR_LEATHER, Config.ITEM_ARMOR_CHAIN, Config.ITEM_ARMOR_PLATE
    )
    val isPotion: Boolean get() = type in listOf(Config.ITEM_POTION_HP, Config.ITEM_POTION_STR)
    val isScroll: Boolean get() = type in listOf(
        Config.ITEM_SCROLL_LIGHTNING, Config.ITEM_SCROLL_FIREBALL, Config.ITEM_SCROLL_CONFUSE
    )

    fun use(player: Player, entities: List<Entity>, log: MessageLog): Boolean {
        return when (type) {
            Config.ITEM_POTION_HP -> {
                val healed = minOf(player.maxHp - player.hp, hpBonus)
                player.heal(hpBonus)
                log.add("Bebes la $name. Recuperas $healed PV.", Config.C_MSG_ITEM)
                true
            }
            Config.ITEM_POTION_STR -> {
                player.baseAtk += 2
                log.add("Bebes la $name. Tu fuerza aumenta!", Config.C_MSG_ITEM)
                true
            }
            Config.ITEM_SCROLL_LIGHTNING -> {
                val target = entities.filterIsInstance<Enemy>()
                    .filter { !it.isDead }
                    .minByOrNull { it.distanceTo(player) }
                if (target == null) {
                    log.add("No hay enemigos cercanos.", Config.C_MSG_SYSTEM)
                    false
                } else {
                    val dmg = 20 + Random.nextInt(0, 10)
                    target.takeDamage(dmg)
                    log.add("Un rayo golpea a ${target.name} por ${dmg}!", Config.C_MSG_COMBAT)
                    true
                }
            }
            Config.ITEM_SCROLL_FIREBALL -> {
                val targets = entities.filterIsInstance<Enemy>().filter { !it.isDead }
                if (targets.isEmpty()) {
                    log.add("No hay enemigos a la vista.", Config.C_MSG_SYSTEM)
                    false
                } else {
                    targets.forEach {
                        val dmg = 12 + Random.nextInt(0, 8)
                        it.takeDamage(dmg)
                    }
                    log.add("Una bola de fuego abrasa a todos los enemigos!", Config.C_MSG_COMBAT)
                    true
                }
            }
            Config.ITEM_SCROLL_CONFUSE -> {
                val target = entities.filterIsInstance<Enemy>()
                    .filter { !it.isDead }
                    .minByOrNull { it.distanceTo(player) }
                if (target == null) {
                    log.add("No hay enemigos cercanos.", Config.C_MSG_SYSTEM)
                    false
                } else {
                    target.isConfused = true
                    log.add("${target.name} esta confundido!", Config.C_MSG_ITEM)
                    true
                }
            }
            Config.ITEM_GOLD -> {
                player.gold += goldValue
                log.add("Recoges ${goldValue} monedas de oro.", Config.C_MSG_GOLD)
                true
            }
            else -> {
                player.equip(this, log)
                false
            }
        }
    }

    companion object {
        fun create(x: Int, y: Int, type: String, floor: Int): Item {
            return when (type) {
                Config.ITEM_POTION_HP -> Item(
                    x, y, "!", Config.C_POTION, "Pocion de Vida", type,
                    hpBonus = 15 + floor * 2
                )
                Config.ITEM_POTION_STR -> Item(
                    x, y, "!", Config.C_POTION, "Pocion de Fuerza", type
                )
                Config.ITEM_SCROLL_LIGHTNING -> Item(
                    x, y, "?", Config.C_SCROLL, "Pergamino de Rayo", type
                )
                Config.ITEM_SCROLL_FIREBALL -> Item(
                    x, y, "?", Config.C_SCROLL, "Pergamino de Bola de Fuego", type
                )
                Config.ITEM_SCROLL_CONFUSE -> Item(
                    x, y, "?", Config.C_SCROLL, "Pergamino de Confusion", type
                )
                Config.ITEM_WEAPON_DAGGER -> Item(
                    x, y, "/", Config.C_WEAPON, "Daga", type, atkBonus = 2
                )
                Config.ITEM_WEAPON_SWORD -> Item(
                    x, y, "/", Config.C_WEAPON, "Espada", type, atkBonus = 4
                )
                Config.ITEM_WEAPON_AXE -> Item(
                    x, y, "/", Config.C_WEAPON, "Hacha", type, atkBonus = 6
                )
                Config.ITEM_ARMOR_LEATHER -> Item(
                    x, y, "[", Config.C_ARMOR, "Armadura de Cuero", type, defBonus = 1
                )
                Config.ITEM_ARMOR_CHAIN -> Item(
                    x, y, "[", Config.C_ARMOR, "Cota de Malla", type, defBonus = 3
                )
                Config.ITEM_ARMOR_PLATE -> Item(
                    x, y, "[", Config.C_ARMOR, "Armadura de Placas", type, defBonus = 5
                )
                Config.ITEM_GOLD -> Item(
                    x, y, "$", Config.C_GOLD, "Monedas de Oro", type,
                    goldValue = 10 + Random.nextInt(0, floor * 15 + 1)
                )
                else -> Item(x, y, "?", Config.C_SCROLL, type, type)
            }
        }
    }
}
