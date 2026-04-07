package com.dungeoncrawler.audio

import com.dungeoncrawler.Config
import kotlin.random.Random

/**
 * Melody Generator using Markov chains to create musical sequences.
 * Generates note sequences that sound coherent and pleasant.
 */
class MelodyGenerator {
    data class Note(
        val frequency: Int,
        val durationMs: Int
    )

    /**
     * Generate a melody phrase based on game state and floor.
     * Returns a list of notes with frequencies and durations.
     */
    fun generateMelody(floor: Int, numberOfNotes: Int = 32): List<Note> {
        val scale = Config.SCALE_C_MAJOR
        val notes = mutableListOf<Note>()

        // Start on G4 (mid-range, bright)
        var currentIndex = 4

        // Calculate tempo based on floor (increases with depth)
        val tempo = Config.TEMPO_DEFAULT + (floor * 2).coerceAtMost(20)
        val noteDurationMs = calculateNoteDuration(tempo)

        // Generate melody using Markov chain approach
        for (i in 0 until numberOfNotes) {
            // Weighted transitions: prefer small upward moves, occasional bigger jumps
            val transition = getWeightedTransition()
            currentIndex = (currentIndex + transition).coerceIn(4, 14) // Stay in bright range

            notes.add(Note(
                frequency = scale[currentIndex],
                durationMs = noteDurationMs
            ))
        }

        return notes
    }

    /**
     * Generate an exploration theme melody.
     */
    fun generateExplorationTheme(floor: Int): List<Note> {
        return generateMelody(floor, numberOfNotes = 32)
    }

    /**
     * Generate a combat/danger theme with denser notes.
     */
    fun generateCombatTheme(floor: Int): List<Note> {
        val scale = Config.SCALE_C_MAJOR
        val notes = mutableListOf<Note>()

        var currentIndex = 4
        val tempo = Config.TEMPO_INTENSE + (floor * 3)
        val noteDurationMs = calculateNoteDuration(tempo)

        // More energetic with more frequent transitions
        for (i in 0 until 48) {
            val transition = listOf(+2, +1, +1, +1, -1, 0, 0).random()
            currentIndex = (currentIndex + transition).coerceIn(4, 14)

            notes.add(Note(
                frequency = scale[currentIndex],
                durationMs = noteDurationMs
            ))
        }

        return notes
    }

    /**
     * Generate a victory fanfare theme (triumphant, ascending).
     */
    fun generateVictoryTheme(): List<Note> {
        val scale = Config.SCALE_C_MAJOR
        val notes = mutableListOf<Note>()

        // Start low, ascend triumphantly
        val melodicSequence = listOf(
            4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 13, 12
        )

        for (noteIndex in melodicSequence) {
            notes.add(Note(
                frequency = scale[noteIndex],
                durationMs = Config.NOTE_QUARTER
            ))
        }

        // Hold final note longer
        notes.add(Note(
            frequency = scale[12],
            durationMs = Config.NOTE_HALF
        ))

        return notes
    }

    /**
     * Generate a defeat/death theme (descending, melancholic).
     */
    fun generateDefeatTheme(): List<Note> {
        val scale = Config.SCALE_C_MAJOR
        val notes = mutableListOf<Note>()

        // Start high, descend sadly
        val melodicSequence = listOf(
            12, 11, 10, 9, 8, 7, 6, 5, 4, 4, 4
        )

        for (noteIndex in melodicSequence) {
            notes.add(Note(
                frequency = scale[noteIndex],
                durationMs = Config.NOTE_QUARTER
            ))
        }

        // Hold final low note
        notes.add(Note(
            frequency = scale[4],
            durationMs = Config.NOTE_WHOLE
        ))

        return notes
    }

    /**
     * Get a weighted random transition (Markov chain step).
     * Biases towards small upward movements, occasional bigger jumps and rests.
     */
    private fun getWeightedTransition(): Int {
        val transitions = listOf(
            +1, +1, +1, +1,  // 4 units: move up (high probability)
            -1, -1, -1,      // 3 units: move down (medium probability)
            +2, +2,          // 2 units: bigger jump up (medium probability)
            -2,              // 1 unit: bigger jump down (low probability)
            0, 0, 0          // 3 units: stay same (medium-high probability for phrasing)
        )
        return transitions.random()
    }

    /**
     * Calculate note duration in milliseconds based on tempo (BPM).
     * Returns duration of a quarter note at the given tempo.
     */
    private fun calculateNoteDuration(tempoFactor: Int): Int {
        // At 120 BPM, quarter note = 500ms
        // Adjust based on tempo
        return (500 * 120 / tempoFactor).coerceAtLeast(100)
    }
}
