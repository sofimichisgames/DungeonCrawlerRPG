package com.dungeoncrawler.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.dungeoncrawler.Config

/**
 * Wrapper around Android's AudioTrack for playing synthesized audio.
 * Handles initialization, playback, and cleanup.
 */
class AudioTrackPlayer {
    private var audioTrack: AudioTrack? = null
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        try {
            Log.d("AudioTrackPlayer", "Initializing AudioTrack...")
            val bufferSize = AudioTrack.getMinBufferSize(
                Config.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            Log.d("AudioTrackPlayer", "Buffer size: $bufferSize")

            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d("AudioTrackPlayer", "Using Android Q+ AudioTrack.Builder")
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(Config.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                Log.d("AudioTrackPlayer", "Using legacy AudioTrack constructor")
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    Config.SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack?.apply {
                Log.d("AudioTrackPlayer", "Setting volume to ${Config.MUSIC_VOLUME}")
                setVolume(Config.MUSIC_VOLUME)
                Log.d("AudioTrackPlayer", "Starting playback")
                play()
                Log.d("AudioTrackPlayer", "AudioTrack initialized successfully. State: ${this.playState}")
            } ?: throw Exception("AudioTrack creation returned null")

            isInitialized = true
            Log.d("AudioTrackPlayer", "AudioTrackPlayer initialization complete")
        } catch (e: Exception) {
            Log.e("AudioTrackPlayer", "Failed to initialize AudioTrack: ${e.message}", e)
            throw e
        }
    }

    fun play(audioData: ShortArray) {
        val track = audioTrack ?: return
        try {
            // Ensure track is playing
            if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                track.play()
            }
            // Write audio data to the track
            val written = track.write(audioData, 0, audioData.size, AudioTrack.WRITE_BLOCKING)
            if (written < 0) {
                android.util.Log.e("AudioTrackPlayer", "AudioTrack.write() returned error: $written")
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioTrackPlayer", "Error playing audio: ${e.message}", e)
        }
    }

    fun pause() {
        audioTrack?.pause()
    }

    fun resume() {
        audioTrack?.play()
    }

    fun stop() {
        audioTrack?.apply {
            stop()
            flush()
        }
    }

    fun release() {
        audioTrack?.apply {
            stop()
            release()
        }
        audioTrack = null
        isInitialized = false
    }

    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        audioTrack?.setVolume(clampedVolume)
    }
}
