package com.dungeoncrawler.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.dungeoncrawler.Config
import com.dungeoncrawler.game.GameEngine
import com.dungeoncrawler.game.entity.Enemy
import com.dungeoncrawler.game.entity.Item

class Renderer(sw: Int, sh: Int) {
    private var screenW = sw.toFloat()
    private var screenH = sh.toFloat()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.LEFT
    }
    fun resize(w: Int, h: Int) { screenW = w.toFloat(); screenH = h.toFloat() }
    fun render(canvas: Canvas, engine: GameEngine, controls: VirtualControls) {
        canvas.drawColor(Config.C_BLACK)
        when (engine.state) {
            Config.STATE_MENU      -> drawMenu(canvas)
            Config.STATE_GAME_OVER -> drawGameOver(canvas)
            Config.STATE_VICTORY   -> drawVictory(canvas)
            Config.STATE_PLAYING,
            Config.STATE_INVENTORY -> {
                drawHud(canvas, engine)
                drawMap(canvas, engine)
                drawEntities(canvas, engine)
                drawMessageLog(canvas, engine)
                drawControls(canvas)
                if (engine.state == Config.STATE_INVENTORY) drawInventory(canvas, engine)
            }
        }
    }
    private fun drawMenu(canvas: Canvas) {
        val cx = screenW / 2f
        paint.color = Config.C_GOLD; paint.textSize = screenH * 0.07f
        val t1 = "DUNGEON CRAWLER"
        canvas.drawText(t1, cx - paint.measureText(t1) / 2f, screenH * 0.35f, paint)
        paint.color = Config.C_LIGHT_GRAY; paint.textSize = screenH * 0.035f
        val t2 = "RPG ASCII Roguelike"
        canvas.drawText(t2, cx - paint.measureText(t2) / 2f, screenH * 0.46f, paint)
        paint.color = Config.C_WHITE; paint.textSize = screenH * 0.03f
        val t3 = "Toca la pantalla para comenzar"
        canvas.drawText(t3, cx - paint.measureText(t3) / 2f, screenH * 0.65f, paint)
    }
    private fun drawGameOver(canvas: Canvas) {
        val cx = screenW / 2f
        paint.color = Config.C_MSG_DEATH; paint.textSize = screenH * 0.08f
        val t1 = "HAS MUERTO"
        canvas.drawText(t1, cx - paint.measureText(t1) / 2f, screenH * 0.40f, paint)
        paint.color = Config.C_LIGHT_GRAY; paint.textSize = screenH * 0.03f
        val t2 = "Toca para volver a intentarlo"
        canvas.drawText(t2, cx - paint.measureText(t2) / 2f, screenH * 0.60f, paint)
    }
    private fun drawVictory(canvas: Canvas) {
        val cx = screenW / 2f
        paint.color = Config.C_GOLD; paint.textSize = screenH * 0.07f
        val t1 = "VICTORIA"
        canvas.drawText(t1, cx - paint.measureText(t1) / 2f, screenH * 0.38f, paint)
        paint.color = Config.C_HP_GREEN; paint.textSize = screenH * 0.032f
        val t2 = "Conquistaste las mazmorras!"
        canvas.drawText(t2, cx - paint.measureText(t2) / 2f, screenH * 0.52f, paint)
        paint.color = Config.C_LIGHT_GRAY; paint.textSize = screenH * 0.028f
        val t3 = "Toca para jugar de nuevo"
        canvas.drawText(t3, cx - paint.measureText(t3) / 2f, screenH * 0.68f, paint)
    }
    private fun drawHud(canvas: Canvas, engine: GameEngine) {
        val p = engine.player
        val hudH = screenH * 0.08f
        paint.color = Config.C_UI_BG
        canvas.drawRect(0f, 0f, screenW, hudH, paint)
        paint.color = Config.C_UI_BORDER
        canvas.drawLine(0f, hudH, screenW, hudH, paint)
        val fs = hudH * 0.36f; paint.textSize = fs
        val margin = screenW * 0.02f
        var x = margin; val y = hudH * 0.65f
        paint.color = Config.C_WHITE; canvas.drawText("PV:", x, y, paint)
        x += paint.measureText("PV:")
        drawBar(canvas, x, hudH*0.18f, screenW*0.18f, hudH*0.28f, p.hp.toFloat(), p.maxHp.toFloat(), Config.C_HP_RED, Config.C_HP_GREEN, Config.C_HP_AMBER)
        x += screenW * 0.20f
        paint.color = Config.C_LIGHT_GRAY; canvas.drawText("${p.hp}/${p.maxHp}", x, y, paint)
        x += paint.measureText("${p.hp}/${p.maxHp}") + margin
        paint.color = Config.C_WHITE; canvas.drawText("XP:", x, y, paint)
        x += paint.measureText("XP:")
        drawBar(canvas, x, hudH*0.18f, screenW*0.14f, hudH*0.28f, p.xp.toFloat(), p.xpNext.toFloat(), Config.C_XP_BAR, Config.C_XP_BAR, Config.C_XP_BAR)
        x += screenW * 0.16f
        paint.color = Config.C_LIGHT_GRAY; canvas.drawText("Nv${p.level}", x, y, paint)
        x += paint.measureText("Nv${p.level}") + margin
        paint.color = Config.C_STAIRS; canvas.drawText("P${p.floor}", x, y, paint)
        x += paint.measureText("P${p.floor}") + margin
        paint.color = Config.C_GOLD; canvas.drawText("${p.gold}g", x, y, paint)
    }
    private fun drawBar(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, cur: Float, max: Float, low: Int, high: Int, mid: Int) {
        paint.color = Config.C_UI_BORDER; canvas.drawRect(x, y, x+w, y+h, paint)
        val ratio = (cur/max).coerceIn(0f,1f)
        paint.color = when { ratio>0.6f -> high; ratio>0.3f -> mid; else -> low }
        canvas.drawRect(x, y, x+w*ratio, y+h, paint)
    }
    private fun mapCell(engine: GameEngine): Triple<Float,Float,Float> {
        val hudH = screenH * 0.08f
        val ctrlH = screenH * 0.30f
        val mapAreaH = screenH - hudH - ctrlH
        val cell = minOf(screenW / Config.MAP_W.toFloat(), mapAreaH / Config.MAP_H.toFloat())
        val offX = (screenW - cell * Config.MAP_W) / 2f
        val offY = hudH + (mapAreaH - cell * Config.MAP_H) / 2f
        return Triple(offX, offY, cell)
    }
    private fun drawMap(canvas: Canvas, engine: GameEngine) {
        val map = engine.gameMap
        val (offX, offY, cell) = mapCell(engine)

        for (mx in 0 until Config.MAP_W) {
            for (my in 0 until Config.MAP_H) {
                if (!map.explored[mx][my]) continue

                val vis = map.visible[mx][my]
                val px = offX + (mx.toFloat() * cell)
                val py = offY + (my.toFloat() * cell)

                when (map.tiles[mx][my]) {
                    Config.TILE_WALL -> {
                        paint.color = if (vis) Config.C_LIGHT_WALL else Config.C_DARK_WALL
                        canvas.drawRect(px, py, px + cell, py + cell, paint)
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f
                        paint.color = Color.parseColor("#696969")
                        canvas.drawRect(px, py, px + cell, py + cell, paint)
                        paint.style = Paint.Style.FILL
                    }
                    Config.TILE_FLOOR -> {
                        paint.color = if (vis) Config.C_LIGHT_FLOOR else Config.C_DARK_FLOOR
                        canvas.drawRect(px, py, px + cell, py + cell, paint)
                    }
                    Config.TILE_STAIRS -> {
                        paint.color = if (vis) Config.C_STAIRS else Config.C_DARK_FLOOR
                        canvas.drawRect(px, py, px + cell, py + cell, paint)
                    }
                }
            }
        }
    }
    private fun drawEntities(canvas: Canvas, engine: GameEngine) {
        val map = engine.gameMap
        val (offX, offY, cell) = mapCell(engine)

        for (ent in engine.entities.sortedBy { it.renderOrder }) {
            if (!map.visible[ent.x][ent.y]) continue

            val px = offX + (ent.x.toFloat() * cell)
            val py = offY + (ent.y.toFloat() * cell)

            when (ent.char) {
                '@' -> {
                    paint.color = Color.parseColor("#4169E1")
                    canvas.drawCircle(px + cell/2f, py + cell/2f, cell/2.5f, paint)
                }
                'E', 'o', 'G' -> {
                    paint.color = Color.parseColor("#DC143C")
                    canvas.drawCircle(px + cell/2f, py + cell/2f, cell/2.8f, paint)
                }
                else -> {
                    paint.color = ent.color
                    canvas.drawCircle(px + cell/2f, py + cell/2f, cell/3.5f, paint)
                }
            }
        }
    }
    private fun drawMessageLog(canvas: Canvas, engine: GameEngine) {
        val ctrlH = screenH * 0.30f
        val logBottom = screenH - ctrlH
        val fs = screenH * 0.022f; paint.textSize = fs
        val margin = screenW * 0.02f
        val messages = engine.messageLog.recent(3)
        for (i in messages.indices) {
            val (text, color) = messages[i]
            paint.color = color
            paint.alpha = if (i == messages.lastIndex) 255 else 140
            canvas.drawText(text.take(55), margin, logBottom - fs*(messages.size-1-i) - margin, paint)
        }
        paint.alpha = 255
    }
    private fun drawControls(canvas: Canvas) {
        val padSize = screenW * 0.38f; val btnSize = padSize / 3f
        val padLeft = screenW * 0.02f; val padBottom = screenH - screenH * 0.02f
        val padTop  = padBottom - padSize
        paint.color = Config.C_UI_BG
        canvas.drawRect(0f, padTop - padSize*0.08f, screenW, screenH, paint)
        paint.color = Config.C_UI_BORDER
        canvas.drawLine(0f, padTop - padSize*0.08f, screenW, padTop - padSize*0.08f, paint)
        val fs = btnSize * 0.38f
        val cx = padLeft + padSize/2f; val cy = padTop + padSize/2f
        drawBtn(canvas, cx-btnSize/2f, padTop,              btnSize, "^", fs)
        drawBtn(canvas, cx-btnSize/2f, padBottom-btnSize,   btnSize, "v", fs)
        drawBtn(canvas, padLeft,       cy-btnSize/2f,       btnSize, "<", fs)
        drawBtn(canvas, padLeft+padSize-btnSize, cy-btnSize/2f, btnSize, ">", fs)
        drawBtn(canvas, cx-btnSize/2f, cy-btnSize/2f,       btnSize, ".", fs)
        val actRight = screenW - screenW*0.02f
        val actLeft  = actRight - padSize
        val actTop   = padBottom - padSize
        val lbls = listOf("INV",",","G","\\","\u00b7","/")
        for (i in lbls.indices) {
            val r = i / 3; val c = i % 3
            drawBtn(canvas, actLeft + c*btnSize, actTop + r*btnSize, btnSize, lbls[i], fs)
        }
    }
    private fun drawBtn(canvas: Canvas, x: Float, y: Float, size: Float, label: String, fs: Float) {
        paint.color = Config.C_UI_BORDER
        canvas.drawRect(x+2f, y+2f, x+size-2f, y+size-2f, paint)
        paint.color = Config.C_LIGHT_GRAY; paint.textSize = fs
        canvas.drawText(label, x+(size-paint.measureText(label))/2f, y+size*0.65f, paint)
    }
    private fun drawInventory(canvas: Canvas, engine: GameEngine) {
        val p = engine.player
        val ow = screenW*0.55f; val oh = screenH*0.75f
        val ox = (screenW-ow)/2f; val oy = (screenH-oh)/2f
        paint.color = Config.C_UI_BG; canvas.drawRect(ox, oy, ox+ow, oy+oh, paint)
        paint.color = Config.C_UI_BORDER; canvas.drawRect(ox, oy, ox+ow, oy+oh, paint)
        val fs = oh*0.05f; val margin = ow*0.05f
        paint.textSize = fs; paint.color = Config.C_HIGHLIGHT
        canvas.drawText("INVENTARIO", ox+margin, oy+fs*1.5f, paint)
        paint.color = Config.C_GRAY; paint.textSize = fs*0.7f
        canvas.drawText("Atk:${p.atk}  Def:${p.def}  Oro:${p.gold}", ox+margin, oy+fs*2.5f, paint)
        paint.textSize = fs
        val startY = oy+fs*4.3f; val lineH = fs*1.25f
        if (p.inventory.isEmpty()) {
            paint.color = Config.C_GRAY; canvas.drawText("(vacio)", ox+margin, startY, paint)
        } else {
            p.inventory.forEachIndexed { i, item ->
                val lbl = ('a'.code + i).toChar().toString() + ") "
                paint.color = Config.C_HIGHLIGHT; canvas.drawText(lbl, ox+margin, startY+i*lineH, paint)
                paint.color = item.color
                canvas.drawText(item.char+" "+item.name, ox+margin+paint.measureText(lbl), startY+i*lineH, paint)
            }
        }
        val equipY = oy+oh-fs*3.5f
        paint.textSize = fs*0.8f
        paint.color = Config.C_WEAPON
        canvas.drawText("Arma: "+(p.equippedWeapon?.name ?: "ninguna"), ox+margin, equipY, paint)
        paint.color = Config.C_ARMOR
        canvas.drawText("Armadura: "+(p.equippedArmor?.name ?: "ninguna"), ox+margin, equipY+fs, paint)
        paint.color = Config.C_GRAY; paint.textSize = fs*0.65f
        canvas.drawText("Toca item para usar. Fuera para cerrar.", ox+margin, oy+oh-fs*0.5f, paint)
    }
}
