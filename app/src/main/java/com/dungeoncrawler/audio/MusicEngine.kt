package com.dungeoncrawler.audio

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dungeoncrawler.Config
import kotlin.concurrent.thread

/**
 * Music Engine that manages music generation and playback.
 * Handles state transitions and generates appropriate musical tracks.
 */
class MusicEngine(
    private val audioPlayer: AudioTrackPlayer
) {
    private val synthesizer = Synthesizer()
    private val melodyGenerator = MelodyGenerator()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentState = -1
    private var currentFloor = 0
    private var isPlaying = false
    private var musicThread: Thread? = null

    fun initialize() {
        audioPlayer.initialize()
    }

    /**
     * Change the music based on game state.
     * Different states get different themes.
     */
    fun playStateMusic(state: Int, floor: Int = 0) {
        if (currentState == state && currentFloor == floor) {
            return // Already playing this music
        }

        currentState = state
        currentFloor = floor

        // Stop current music and start new theme
        stop()

        val notes = when (state) {
            Config.STATE_MENU -> melodyGenerator.generateMelody(0, numberOfNotes = 16)
            Config.STATE_PLAYING -> melodyGenerator.generateExplorationTheme(floor)
            Config.STATE_INVENTORY -> melodyGenerator.generateExplorationTheme(floor)
            Config.STATE_VICTORY -> melodyGenerator.generateVictoryTheme()
            Config.STATE_GAME_OVER -> melodyGenerator.generateDefeatTheme()
            else -> melodyGenerator.generateMelody(0, numberOfNotes = 16)
        }

        playNotes(notes)
    }

    /**
     * Play a combat theme when enemies are nearby.
     */
    fun playCombatTheme(floor: Int) {
        if (currentState == Config.STATE_PLAYING) {
            stop()
            val notes = melodyGenerator.generateCombatTheme(floor)
            playNotes(notes)
        }
    }

    /**
     * Play a sequence of notes.
     * Runs in a background thread to avoid blocking.
     */
    private fun playNotes(notes: List<MelodyGenerator.Note>) {
        if (isPlaying) {
            Log.w("MusicEngine", "Already playing music, ignoring new request")
            return // Already playing
        }

        Log.d("MusicEngine", "Starting playback of ${notes.size} notes")
        isPlaying = true
        musicThread = thread(daemon = true) {
            try {
                for ((index, note) in notes.withIndex()) {
                    if (!isPlaying) {
                        Log.d("MusicEngine", "Playback interrupted at note $index")
                        break
                    }

                    // Synthesize the note
                    val audioData = synthesizer.synthesizeNote(
                        frequency = note.frequency.toFloat(),
                        durationMs = note.durationMs,
                        waveform = Synthesizer.Waveform.TRIANGLE
                    )

                    Log.d("MusicEngine", "Playing note $index: ${note.frequency}Hz for ${note.durationMs}ms (${audioData.size} samples)")

                    // Play the note
                    audioPlayer.play(audioData)

                    // Wait for the note to complete (full duration, not just 10%)
                    Thread.sleep(note.durationMs.toLong())
                }
                Log.d("MusicEngine", "Playback complete")
            } catch (e: Exception) {
                Log.e("MusicEngine", "Error during music playback: ${e.message}", e)
            } finally {
                isPlaying = false
            }
        }
    }

    /**
     * Loop the current theme (repeats music continuously).
     */
    fun loop() {
        if (!isPlaying) {
            playStateMusic(currentState, currentFloor)
        }
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun resume() {
        audioPlayer.resume()
    }

    fun stop() {
        isPlaying = false
        audioPlayer.stop()
    }

    fun setVolume(volume: Float) {
        audioPlayer.setVolume(volume)
    }

    fun release() {
        stop()
        audioPlayer.release()
    }
}
