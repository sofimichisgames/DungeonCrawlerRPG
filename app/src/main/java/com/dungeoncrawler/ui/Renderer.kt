package com.dungeoncrawler.ui

import android.graphics.Canvas
import android.graphics.Color
import com.dungeoncrawler.Config
import com.dungeoncrawler.game.GameEngine

class Renderer(var sw: Int = 1080, var sh: Int = 1920) {
    fun resize(width: Int, height: Int) {
        sw = width; sh = height
    }
    
    fun render(canvas: Canvas, engine: GameEngine, controls: VirtualControls) {
        canvas.drawColor(Config.C_UI_BG)
        // Render implementation would go here
    }
}
