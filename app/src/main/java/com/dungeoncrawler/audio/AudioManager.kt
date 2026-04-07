package com.dungeoncrawler.audio

import android.content.Context
import android.util.Log
import com.dungeoncrawler.Config

/**
 * AudioManager - Singleton for managing all audio in the game.
 * Provides a simple interface for the rest of the game to control music.
 */
object AudioManager {
    private var musicEngine: MusicEngine? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d("AudioManager", "Already initialized, skipping")
            return
        }

        try {
            Log.d("AudioManager", "Initializing AudioManager...")
            val audioPlayer = AudioTrackPlayer()
            musicEngine = MusicEngine(audioPlayer)
            Log.d("AudioManager", "MusicEngine created, initializing...")
            musicEngine?.initialize()
            Log.d("AudioManager", "Setting volume...")
            musicEngine?.setVolume(Config.MUSIC_VOLUME)

            isInitialized = true
            Log.d("AudioManager", "AudioManager initialization complete")
        } catch (e: Exception) {
            Log.e("AudioManager", "Failed to initialize AudioManager: ${e.message}", e)
            isInitialized = false
        }
    }

    /**
     * Play music for the given game state.
     */
    fun playStateMusic(state: Int, floor: Int = 0) {
        musicEngine?.playStateMusic(state, floor)
    }

    /**
     * Play a combat theme when enemies are nearby.
     */
    fun playCombatTheme(floor: Int) {
        musicEngine?.playCombatTheme(floor)
    }

    /**
     * Pause the current music.
     */
    fun pause() {
        musicEngine?.pause()
    }

    /**
     * Resume the paused music.
     */
    fun resume() {
        musicEngine?.resume()
    }

    /**
     * Stop the current music.
     */
    fun stop() {
        musicEngine?.stop()
    }

    /**
     * Set the music volume (0.0 to 1.0).
     */
    fun setVolume(volume: Float) {
        musicEngine?.setVolume(volume.coerceIn(0f, 1f))
    }

    /**
     * Release all audio resources.
     * Call this when the app is shutting down.
     */
    fun release() {
        musicEngine?.release()
        isInitialized = false
    }
}
