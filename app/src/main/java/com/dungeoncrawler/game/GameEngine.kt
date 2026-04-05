package com.dungeoncrawler.game

import com.dungeoncrawler.Config
import com.dungeoncrawler.game.entity.Enemy
import com.dungeoncrawler.game.entity.Entity
import com.dungeoncrawler.game.entity.Item
import com.dungeoncrawler.game.entity.Player

// =============================================================================
// GameEngine.kt — Estado central del juego, lógica de turnos
// =============================================================================
class GameEngine {

    var state         = Config.STATE_MENU
    lateinit var player: Player
    lateinit var gameMap: GameMap
    val entities      = mutableListOf<Entity>()
    val messageLog    = MessageLog()
    var currentFloor  = 1
    var needsRedraw   = true

    private val dungeonGen = DungeonGenerator()

    // ------------------------------------------------------------------
    fun newGame() {
        player       = Player(5, 5)
        gameMap      = GameMap()
        currentFloor = 1
        messageLog.clear()
        messageLog.add("¡Bienvenido a las mazmorras! Llega al piso 10.", Config.C_MSG_SYSTEM)
        messageLog.add("Toca el D-pad para moverte. INV para el inventario.", Config.C_MSG_SYSTEM)
        loadFloor()
        state = Config.STATE_PLAYING
    }

    fun loadFloor() {
        val result = dungeonGen.generate(gameMap, currentFloor)
        player.x = result.playerStart.first
        player.y = result.playerStart.second
        player.floor = currentFloor

        entities.clear()
        entities.add(player)

        for (enemy in result.enemies)
            if (enemy.x != player.x || enemy.y != player.y)
                entities.add(enemy)

        for (item in result.items)
            if (item.x != player.x || item.y != player.y)
                entities.add(item)

        FovSystem.compute(gameMap, player.x, player.y)
        messageLog.add("Piso $currentFloor: las sombras te rodean.", Config.C_MSG_SYSTEM)
        needsRedraw = true
    }

    // ------------------------------------------------------------------
    // Acciones del jugador
    // ------------------------------------------------------------------
    fun playerMove(dx: Int, dy: Int) {
        if (state != Config.STATE_PLAYING) return
        val acted = player.tryMoveOrAttack(dx, dy, gameMap, entities, messageLog)
        if (acted) afterPlayerTurn()
    }

    fun playerWait() {
        if (state != Config.STATE_PLAYING) return
        afterPlayerTurn()
    }

    fun playerPickUp() {
        if (state != Config.STATE_PLAYING) return
        val item = entities.filterIsInstance<Item>()
            .firstOrNull { it.x == player.x && it.y == player.y }
        if (item == null) {
            messageLog.add("No hay nada aquí para recoger.", Config.C_MSG_SYSTEM)
            return
        }
        if (player.pickUp(item, messageLog)) {
            entities.remove(item)
            afterPlayerTurn()
        }
    }

    fun playerDescend() {
        if (state != Config.STATE_PLAYING) return
        if (player.x == gameMap.stairsX && player.y == gameMap.stairsY) {
            if (currentFloor >= Config.MAX_FLOOR) {
                state = Config.STATE_VICTORY
                messageLog.add("¡Has conquistado las mazmorras!", Config.C_MSG_SYSTEM)
            } else {
                currentFloor++
                loadFloor()
            }
        } else {
            messageLog.add("No hay escaleras aquí. Busca el símbolo >", Config.C_MSG_SYSTEM)
        }
        needsRedraw = true
    }

    fun useInventoryItem(index: Int) {
        if (index < 0 || index >= player.inventory.size) return
        val item = player.inventory[index]
        val consumed = player.useItem(item, entities, messageLog)
        if (consumed) player.inventory.removeAt(index)
        state = Config.STATE_PLAYING
        afterPlayerTurn()
    }

    // ------------------------------------------------------------------
    private fun afterPlayerTurn() {
        if (player.isDead) {
            state = Config.STATE_GAME_OVER
            messageLog.add("Has caído en las profundidades...", Config.C_MSG_DEATH)
            needsRedraw = true
            return
        }
        enemyTurn()
        cleanupDead()
        FovSystem.compute(gameMap, player.x, player.y)
        needsRedraw = true
    }

    private fun enemyTurn() {
        entities.filterIsInstance<Enemy>().forEach { enemy ->
            enemy.takeTurn(player, gameMap, entities, messageLog)
        }
        if (player.isDead) {
            state = Config.STATE_GAME_OVER
            messageLog.add("Has caído en las profundidades...", Config.C_MSG_DEATH)
        }
    }

    private fun cleanupDead() {
        val dead = entities.filterIsInstance<Enemy>().filter { it.isDead }
        dead.forEach { enemy ->
            player.gainXp(enemy.xpReward, messageLog)
            entities.remove(enemy)
        }
    }

    // ------------------------------------------------------------------
    // Helpers de consulta
    // ------------------------------------------------------------------
    fun entitiesAt(x: Int, y: Int): List<Entity> =
        entities.filter { it.x == x && it.y == y }

    fun itemsAtPlayer(): List<Item> =
        entities.filterIsInstance<Item>().filter { it.x == player.x && it.y == player.y }
}
