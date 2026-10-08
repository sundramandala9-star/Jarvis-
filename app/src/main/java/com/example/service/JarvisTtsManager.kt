package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisTtsManager(context: Context) {
    private val TAG = "JarvisTtsManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentLanguage = MutableStateFlow("hi") // "hi" or "en"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    var pitch: Float = 0.92f
    var speed: Float = 1.05f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setPitch(pitch)
                tts?.setSpeechRate(speed)
                setLanguage(_currentLanguage.value)
                setupProgressListener()
                Log.d(TAG, "TTS initialized successfully")
            } else {
                Log.w(TAG, "TTS engine initializing or pending locale download")
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
        if (!isInitialized) return
        val locale = when (langCode.lowercase()) {
            "hi" -> Locale("hi", "IN")
            else -> Locale("en", "US")
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default locale
            tts?.setLanguage(Locale.US)
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null || text.isBlank()) {
            onDone?.invoke()
            return
        }

        // Auto-detect Hindi vs English characters if needed
        val hasHindiChar = text.any { it in '\u0900'..'\u097F' }
        val targetLocale = if (hasHindiChar) Locale("hi", "IN") else Locale("en", "US")
        tts?.setLanguage(targetLocale)
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        _isSpeaking.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
