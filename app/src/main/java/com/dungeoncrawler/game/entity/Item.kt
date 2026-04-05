package com.dungeoncrawler.game.entity

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.MessageLog

class Item private constructor(
    x: Int, y: Int,
    char: String, color: Int,
    override val name: String,
    val type: String,
    val atkBonus: Int = 0,
    val defBonus: Int = 0
) : Entity(x, y, char, color, name, blocks = false) {
    
    val isWeapon get() = type.startsWith("weapon")
    val isArmor get() = type.startsWith("armor")
    
    init { renderOrder = 0 }
    
    fun use(player: Player?, entities: List<Entity>?, log: MessageLog?): Boolean = false
    
    companion object {
        fun create(x: Int, y: Int, type: String, floor: Int): Item {
            return Item(x, y, "?", Config.C_GOLD, "Item", type)
        }
    }
}
