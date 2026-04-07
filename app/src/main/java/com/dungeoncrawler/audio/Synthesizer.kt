package com.dungeoncrawler.audio

import android.util.Log
import com.dungeoncrawler.Config
import kotlin.math.PI

/**
 * Synthesizer that generates audio samples using oscillators and ADSR envelope.
 * Supports multiple waveforms: sine, square, triangle, sawtooth.
 */
class Synthesizer {
    private val sampleRate = Config.SAMPLE_RATE

    enum class Waveform {
        SINE, TRIANGLE, SQUARE, SAWTOOTH
    }

    /**
     * Synthesize a single note with the given frequency and duration.
     * Returns PCM audio samples as ShortArray.
     */
    fun synthesizeNote(
        frequency: Float,
        durationMs: Int,
        waveform: Waveform = Waveform.TRIANGLE
    ): ShortArray {
        try {
            val totalSamples = (durationMs * sampleRate / 1000)
            Log.d("Synthesizer", "Generating $waveform note at ${frequency}Hz for ${durationMs}ms ($totalSamples samples)")

            val samples = ShortArray(totalSamples)
            val envelope = ADSREnvelope(durationMs.toFloat())

            when (waveform) {
                Waveform.SINE -> generateSineWave(frequency, samples, envelope)
                Waveform.TRIANGLE -> generateTriangleWave(frequency, samples, envelope)
                Waveform.SQUARE -> generateSquareWave(frequency, samples, envelope)
                Waveform.SAWTOOTH -> generateSawtoothWave(frequency, samples, envelope)
            }

            Log.d("Synthesizer", "Successfully generated audio. Sample range: [${samples.minOrNull()}, ${samples.maxOrNull()}]")
            return samples
        } catch (e: Exception) {
            Log.e("Synthesizer", "Error synthesizing note: ${e.message}", e)
            throw e
        }
    }

    private fun generateSineWave(frequency: Float, samples: ShortArray, envelope: ADSREnvelope) {
        for (i in samples.indices) {
            val phase = 2 * PI * frequency * i / sampleRate
            val sample = (Math.sin(phase) * 32767 * envelope.getValue(i)).toShort()
            samples[i] = sample
        }
    }

    private fun generateTriangleWave(frequency: Float, samples: ShortArray, envelope: ADSREnvelope) {
        for (i in samples.indices) {
            val period = sampleRate / frequency
            val phaseInPeriod = (i % period.toInt()).toFloat() / period
            val triangleValue = if (phaseInPeriod < 0.5f) {
                4 * phaseInPeriod - 1
            } else {
                3 - 4 * phaseInPeriod
            }
            val sample = (triangleValue * 32767 * envelope.getValue(i)).toShort()
            samples[i] = sample
        }
    }

    private fun generateSquareWave(frequency: Float, samples: ShortArray, envelope: ADSREnvelope) {
        for (i in samples.indices) {
            val period = sampleRate / frequency
            val phaseInPeriod = (i % period.toInt()).toFloat() / period
            val squareValue = if (phaseInPeriod < 0.5f) 1f else -1f
            val sample = (squareValue * 32767 * envelope.getValue(i)).toShort()
            samples[i] = sample
        }
    }

    private fun generateSawtoothWave(frequency: Float, samples: ShortArray, envelope: ADSREnvelope) {
        for (i in samples.indices) {
            val period = sampleRate / frequency
            val phaseInPeriod = (i % period.toInt()).toFloat() / period
            val sawtoothValue = 2 * phaseInPeriod - 1
            val sample = (sawtoothValue * 32767 * envelope.getValue(i)).toShort()
            samples[i] = sample
        }
    }

    /**
     * ADSR (Attack, Decay, Sustain, Release) Envelope
     * Controls the volume over the lifetime of a note.
     */
    private class ADSREnvelope(durationMs: Float) {
        private val attackSamples = (Config.ATTACK_TIME * Config.SAMPLE_RATE / 1000f).toInt()
        private val decaySamples = (Config.DECAY_TIME * Config.SAMPLE_RATE / 1000f).toInt()
        private val releaseSamples = (Config.RELEASE_TIME * Config.SAMPLE_RATE / 1000f).toInt()
        private val totalSamples = (durationMs * Config.SAMPLE_RATE / 1000f).toInt()
        private val sustainSamples = totalSamples - attackSamples - decaySamples - releaseSamples

        fun getValue(sampleIndex: Int): Float {
            return when {
                sampleIndex < attackSamples -> {
                    // Attack phase: ramp from 0 to 1
                    sampleIndex.toFloat() / attackSamples
                }
                sampleIndex < attackSamples + decaySamples -> {
                    // Decay phase: ramp from 1 to sustain level
                    val decayProgress = (sampleIndex - attackSamples).toFloat() / decaySamples
                    1f - decayProgress * (1f - Config.SUSTAIN_LEVEL)
                }
                sampleIndex < attackSamples + decaySamples + sustainSamples -> {
                    // Sustain phase: hold sustain level
                    Config.SUSTAIN_LEVEL
                }
                else -> {
                    // Release phase: ramp from sustain to 0
                    val releaseProgress = (sampleIndex - attackSamples - decaySamples - sustainSamples).toFloat() / releaseSamples
                    Config.SUSTAIN_LEVEL * (1f - releaseProgress)
                }
            }
        }
    }
}
