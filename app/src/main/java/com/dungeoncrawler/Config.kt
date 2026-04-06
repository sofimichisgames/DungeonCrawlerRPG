package com.dungeoncrawler

import android.graphics.Color

// =============================================================================
// Config.kt — Constantes globales del juego
// =============================================================================
object Config {

    // --- Mapa ---
    const val MAP_W          = 60
    const val MAP_H          = 40
    const val MAX_ROOMS      = 20
    const val MIN_ROOM_SIZE  = 5
    const val MAX_ROOM_SIZE  = 12
    const val MAX_ENEMIES    = 3
    const val MAX_ITEMS      = 2
    const val MAX_FLOOR      = 10
    const val FOV_RADIUS     = 8

    // --- Tiles ---
    const val TILE_WALL   = 0
    const val TILE_FLOOR  = 1
    const val TILE_STAIRS = 2

    // --- Estados del juego ---
    const val STATE_MENU      = 0
    const val STATE_PLAYING   = 1
    const val STATE_INVENTORY = 2
    const val STATE_GAME_OVER = 3
    const val STATE_VICTORY   = 4

    // --- Tipos de ítems ---
    const val ITEM_POTION_HP       = "potion_hp"
    const val ITEM_POTION_STR      = "potion_str"
    const val ITEM_SCROLL_LIGHTNING= "scroll_lightning"
    const val ITEM_SCROLL_FIREBALL = "scroll_fireball"
    const val ITEM_SCROLL_CONFUSE  = "scroll_confuse"
    const val ITEM_WEAPON_DAGGER   = "dagger"
    const val ITEM_WEAPON_SWORD    = "sword"
    const val ITEM_WEAPON_AXE      = "axe"
    const val ITEM_WEAPON_MACE     = "mace"
    const val ITEM_WEAPON_SPEAR    = "spear"
    const val ITEM_WEAPON_HALBERD  = "halberd"
    const val ITEM_WEAPON_GREATSWORD = "greatsword"
    const val ITEM_WEAPON_SCIMITAR = "scimitar"
    const val ITEM_WEAPON_BOW      = "bow"
    const val ITEM_WEAPON_WARHAMMER = "warhammer"
    const val ITEM_ARMOR_LEATHER   = "leather"
    const val ITEM_ARMOR_CHAIN     = "chain"
    const val ITEM_ARMOR_PLATE     = "plate"
    const val ITEM_GOLD            = "gold"

    // --- XP ---
    const val XP_BASE       = 50
    const val XP_MULTIPLIER = 1.5f

    // --- Inventario ---
    const val MAX_INVENTORY = 26

    // =========================================================================
    // COLORES (ARGB)
    // =========================================================================
    val C_BLACK        = Color.rgb(0, 0, 0)
    val C_DARK_FLOOR   = Color.rgb(20, 18, 28)
    val C_LIGHT_FLOOR  = Color.rgb(65, 58, 78)
    val C_DARK_WALL    = Color.rgb(38, 38, 52)
    val C_LIGHT_WALL   = Color.rgb(95, 95, 118)
    val C_STAIRS       = Color.rgb(200, 175, 80)
    val C_UI_BG        = Color.rgb(12, 12, 18)
    val C_UI_BORDER    = Color.rgb(55, 55, 78)
    val C_WHITE        = Color.rgb(255, 255, 255)
    val C_GRAY         = Color.rgb(90, 90, 100)
    val C_LIGHT_GRAY   = Color.rgb(170, 170, 180)
    val C_HIGHLIGHT    = Color.rgb(80, 130, 220)

    // Entidades
    val C_PLAYER   = Color.rgb(100, 200, 255)
    val C_GOBLIN   = Color.rgb(70,  185,  70)
    val C_ORC      = Color.rgb(190, 110,  55)
    val C_TROLL    = Color.rgb(130,  85, 175)
    val C_SKELETON = Color.rgb(210, 205, 185)
    val C_DEMON    = Color.rgb(230,  55,  55)
    val C_DRAGON   = Color.rgb(245, 155,  25)

    // Ítems
    val C_POTION  = Color.rgb(210,  80, 210)
    val C_SCROLL  = Color.rgb(210, 210,  55)
    val C_WEAPON  = Color.rgb(175, 220, 255)
    val C_ARMOR   = Color.rgb(185, 185, 210)
    val C_GOLD    = Color.rgb(255, 215,   0)

    // HP / XP bars
    val C_HP_RED   = Color.rgb(200,  45,  45)
    val C_HP_GREEN = Color.rgb(45,  200,  80)
    val C_HP_AMBER = Color.rgb(220, 175,  45)
    val C_XP_BAR   = Color.rgb(145,  75, 225)

    // Mensajes
    val C_MSG_DEFAULT = Color.rgb(200, 200, 200)
    val C_MSG_COMBAT  = Color.rgb(255, 120,  75)
    val C_MSG_ITEM    = Color.rgb(115, 225, 115)
    val C_MSG_SYSTEM  = Color.rgb(115, 185, 255)
    val C_MSG_DEATH   = Color.rgb(255,  55,  55)
    val C_MSG_GOLD    = Color.rgb(255, 215,   0)
}
