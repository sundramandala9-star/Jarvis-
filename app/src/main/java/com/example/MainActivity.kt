package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JarvisViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val TAG = "JarvisMainActivity"
    private val viewModel: JarvisViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null

    private val speechIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.handleVoiceResult(spokenText)
            } else {
                viewModel.setListeningState(false)
            }
        } else {
            viewModel.setListeningState(false)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted) {
            initSpeechRecognizer()
            startListening()
        } else {
            Toast.makeText(this, "Microphone permission required for voice assistant", Toast.LENGTH_SHORT).show()
            viewModel.setListeningState(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initSpeechRecognizer()

        setContent {
            MyApplicationTheme {
                MainApp(
                    viewModel = viewModel,
                    onStartVoiceRecognition = { checkAndStartVoiceRecognition() }
                )
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        viewModel.setListeningState(true)
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        viewModel.setListeningState(false)
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition status code: $error")
                        viewModel.setListeningState(false)
                    }

                    override fun onResults(results: Bundle?) {
                        viewModel.setListeningState(false)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val query = matches?.firstOrNull()
                        if (!query.isNullOrBlank()) {
                            viewModel.handleVoiceResult(query)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun checkAndStartVoiceRecognition() {
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        val neededPermissions = mutableListOf<String>()
        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA)
        }

        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            startListening()
        }
    }

    private fun startListening() {
        val langCode = viewModel.uiState.value.language
        val localeTag = if (langCode == "hi") "hi-IN" else "en-US"

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS is listening...")
        }

        try {
            if (speechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(this)) {
                viewModel.setListeningState(true)
                speechRecognizer?.startListening(intent)
            } else {
                viewModel.setListeningState(true)
                speechIntentLauncher.launch(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "SpeechRecognizer direct start warning: ${e.message}, falling back to system intent")
            try {
                viewModel.setListeningState(true)
                speechIntentLauncher.launch(intent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Voice recognition service unavailable", Toast.LENGTH_SHORT).show()
                viewModel.setListeningState(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
