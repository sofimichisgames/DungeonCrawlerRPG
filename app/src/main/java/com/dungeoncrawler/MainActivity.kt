package com.dungeoncrawler

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.dungeoncrawler.audio.AudioManager

// =============================================================================
// MainActivity.kt — Punto de entrada de la aplicación
// =============================================================================
class MainActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla siempre encendida durante el juego
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize audio system
        AudioManager.initialize(this)
        // Start menu music immediately
        AudioManager.playStateMusic(Config.STATE_MENU)

        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        // El GameThread se gestiona en surfaceCreated/surfaceDestroyed
        AudioManager.resume()
    }

    override fun onPause() {
        super.onPause()
        AudioManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.release()
    }
}
