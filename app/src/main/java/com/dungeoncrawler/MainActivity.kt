package com.dungeoncrawler

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

// =============================================================================
// MainActivity.kt — Punto de entrada de la aplicación
// =============================================================================
class MainActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla siempre encendida durante el juego
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        // El GameThread se gestiona en surfaceCreated/surfaceDestroyed
    }

    override fun onPause() {
        super.onPause()
    }
}
