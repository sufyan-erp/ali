package com.example.utils

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundEffectsManager {
    private var toneGenerator: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            // Volume 80% (ToneGenerator.MAX_VOLUME is 100)
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playBouncePaddle() {
        scope.launch {
            try {
                // High pitch beep for paddle hit
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }

    fun playBounceWall() {
        scope.launch {
            try {
                // Distinct beep for wall hit
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }

    fun playGameOver() {
        scope.launch {
            try {
                // Descending failure boops
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONGESTION, 250)
                kotlinx.coroutines.delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }

    fun playHighScore() {
        scope.launch {
            try {
                // Happy celebration chirps
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
                kotlinx.coroutines.delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
