package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JarvisDatabase
import com.example.data.TutorialsProvider
import com.example.data.model.InteractionLog
import com.example.data.model.TutorialGuide
import com.example.data.model.VoiceMacro
import com.example.data.repository.JarvisRepository
import com.example.service.BatteryStatusInfo
import com.example.service.DeviceAutomationManager
import com.example.service.JarvisTtsManager
import com.example.service.SystemMemoryInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class JarvisCoreState {
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING,
    EXECUTING
}

enum class NavigationTab(val labelHindi: String, val labelEnglish: String) {
    APK_DOWNLOADER("एपीके डाउनलोडर", "APK Downloader"),
    ARC_CORE("आर्क कोर", "Arc Core"),
    AUTOMATION("ऑटोमेशन", "Automation"),
    GUIDES("सेटअप गाइड", "Guides & Tricks"),
    SYSTEM_MATRIX("सिस्टम मैट्रिक्स", "System Matrix")
}

data class JarvisUiState(
    val coreState: JarvisCoreState = JarvisCoreState.STANDBY,
    val statusMessage: String = "JARVIS Core Systems 100% Online",
    val activeProtocol: String? = null,
    val isFlashlightOn: Boolean = false,
    val language: String = "hi", // "hi" or "en"
    val ttsPitch: Float = 0.92f,
    val ttsRate: Float = 1.05f,
    val isTtsEnabled: Boolean = true,
    val isListening: Boolean = false,
    val currentQuery: String = "",
    val latestResponse: String = "Welcome back, sir. All automated protocols and neural networks are online and ready for your command.",
    val selectedCategory: String = "ALL",
    val guidesSearchQuery: String = ""
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val database = JarvisDatabase.getDatabase(application, viewModelScope)
    private val repository = JarvisRepository(database.jarvisDao())
    val deviceManager = DeviceAutomationManager(application)
    val ttsManager = JarvisTtsManager(application)
    val apkManager = com.example.service.ApkDownloadManager(application)

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.APK_DOWNLOADER)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _batteryInfo = MutableStateFlow(deviceManager.getBatteryStatus())
    val batteryInfo: StateFlow<BatteryStatusInfo> = _batteryInfo.asStateFlow()

    private val _systemMemory = MutableStateFlow(deviceManager.getSystemMemoryInfo())
    val systemMemory: StateFlow<SystemMemoryInfo> = _systemMemory.asStateFlow()

    val allMacros: StateFlow<List<VoiceMacro>> = repository.allMacros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<InteractionLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshDiagnostics()
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    _uiState.value = _uiState.value.copy(coreState = JarvisCoreState.SPEAKING)
                } else if (_uiState.value.coreState == JarvisCoreState.SPEAKING) {
                    _uiState.value = _uiState.value.copy(coreState = JarvisCoreState.STANDBY)
                }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        if (tab == NavigationTab.SYSTEM_MATRIX) {
            refreshDiagnostics()
        }
    }

    fun setLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang)
        ttsManager.setLanguage(lang)
    }

    fun toggleTts() {
        val newState = !_uiState.value.isTtsEnabled
        _uiState.value = _uiState.value.copy(isTtsEnabled = newState)
        if (!newState) {
            ttsManager.stop()
        }
    }

    fun updateTtsSettings(pitch: Float, rate: Float) {
        _uiState.value = _uiState.value.copy(ttsPitch = pitch, ttsRate = rate)
        ttsManager.pitch = pitch
        ttsManager.speed = rate
    }

    fun refreshDiagnostics() {
        _batteryInfo.value = deviceManager.getBatteryStatus()
        _systemMemory.value = deviceManager.getSystemMemoryInfo()
        _uiState.value = _uiState.value.copy(isFlashlightOn = deviceManager.isFlashlightActive())
    }

    fun setListeningState(listening: Boolean) {
        _uiState.value = _uiState.value.copy(
            isListening = listening,
            coreState = if (listening) JarvisCoreState.LISTENING else JarvisCoreState.STANDBY,
            statusMessage = if (listening) "JARVIS Listening... Speak now" else "JARVIS Standby"
        )
    }

    fun handleVoiceResult(speechText: String) {
        if (speechText.isBlank()) {
            setListeningState(false)
            return
        }
        processCommand(speechText, isVoice = true)
    }

    fun processCommand(command: String, isVoice: Boolean = false) {
        val cleanCmd = command.trim()
        if (cleanCmd.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            currentQuery = cleanCmd,
            coreState = JarvisCoreState.PROCESSING,
            statusMessage = "Analyzing Command: '$cleanCmd'..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val lower = cleanCmd.lowercase()

            // 1. Direct Hardware/Macro Match Check
            val activeMacros = repository.getActiveMacros()
            val matchedMacro = activeMacros.firstOrNull { macro ->
                lower.contains(macro.triggerPhrase.lowercase()) ||
                        macro.triggerPhrase.lowercase().contains(lower)
            }

            if (matchedMacro != null) {
                executeMacroAction(matchedMacro, cleanCmd, isVoice)
                return@launch
            }

            // 2. Built-in Intent Patterns
            when {
                lower.contains("torch on") || lower.contains("flashlight on") || lower.contains("torch jala") || lower.contains("light on") -> {
                    deviceManager.setFlashlight(true)
                    _uiState.value = _uiState.value.copy(isFlashlightOn = true)
                    val response = if (_uiState.value.language == "hi") "Torch on kar di gayi hai, sir." else "Flashlight activated, sir."
                    deliverResponse(cleanCmd, response, "FLASHLIGHT_ON", isVoice)
                }
                lower.contains("torch off") || lower.contains("flashlight off") || lower.contains("torch band") || lower.contains("light band") -> {
                    deviceManager.setFlashlight(false)
                    _uiState.value = _uiState.value.copy(isFlashlightOn = false)
                    val response = if (_uiState.value.language == "hi") "Torch off kar di gayi hai, sir." else "Flashlight turned off, sir."
                    deliverResponse(cleanCmd, response, "FLASHLIGHT_OFF", isVoice)
                }
                lower.contains("battery") || lower.contains("charging") -> {
                    val batt = deviceManager.getBatteryStatus()
                    _batteryInfo.value = batt
                    val response = if (_uiState.value.language == "hi") {
                        "Sir, battery level ${batt.level}% hai. Health: ${batt.health}, Temperature: ${batt.temperatureCelsius}°C."
                    } else {
                        "Power levels are at ${batt.level} percent, sir. Battery health is ${batt.health} and operating temperature is ${batt.temperatureCelsius}°C."
                    }
                    deliverResponse(cleanCmd, response, "BATTERY_CHECK", isVoice)
                }
                lower.contains("camera") -> {
                    val msg = deviceManager.launchApp("CAMERA")
                    deliverResponse(cleanCmd, msg, "OPEN_CAMERA", isVoice)
                }
                lower.contains("dialer") || lower.contains("phone call") || lower.contains("call lagao") -> {
                    val msg = deviceManager.launchApp("DIALER")
                    deliverResponse(cleanCmd, msg, "OPEN_DIALER", isVoice)
                }
                lower.contains("calculator") || lower.contains("hisab") -> {
                    val msg = deviceManager.launchApp("CALCULATOR")
                    deliverResponse(cleanCmd, msg, "OPEN_CALCULATOR", isVoice)
                }
                lower.contains("youtube") -> {
                    val msg = deviceManager.launchApp("YOUTUBE")
                    deliverResponse(cleanCmd, msg, "OPEN_YOUTUBE", isVoice)
                }
                lower.contains("silent") || lower.contains("chup") -> {
                    deviceManager.setSoundProfile("SILENT")
                    val response = if (_uiState.value.language == "hi") "Phone silent mode par set kar diya gaya hai." else "Ringer switched to silent mode, sir."
                    deliverResponse(cleanCmd, response, "SET_SILENT", isVoice)
                }
                lower.contains("party") -> {
                    executeProtocol("PARTY_PROTOCOL", cleanCmd, isVoice)
                }
                lower.contains("stealth") -> {
                    executeProtocol("STEALTH_PROTOCOL", cleanCmd, isVoice)
                }
                lower.contains("morning") -> {
                    executeProtocol("MORNING_PROTOCOL", cleanCmd, isVoice)
                }
                lower.contains("apk") || lower.contains("download") -> {
                    selectTab(NavigationTab.APK_DOWNLOADER)
                    val response = if (_uiState.value.language == "hi") {
                        "APK Downloader engine khol diya gaya hai, sir. Aap direct URL paste karke APK download kar sakte hain."
                    } else {
                        "Opening APK Downloader engine, sir. You can download external APK packages directly to your device's Downloads directory."
                    }
                    deliverResponse(cleanCmd, response, "OPEN_DOWNLOADER", isVoice)
                }
                else -> {
                    // 3. Fallback to Gemini AI Neural Brain
                    queryAiBrain(cleanCmd, isVoice)
                }
            }
        }
    }

    private suspend fun executeMacroAction(macro: VoiceMacro, rawQuery: String, isVoice: Boolean) {
        when (macro.actionType) {
            "FLASHLIGHT_ON" -> {
                deviceManager.setFlashlight(true)
                _uiState.value = _uiState.value.copy(isFlashlightOn = true)
                deliverResponse(rawQuery, "Flashlight initialized per macro routine, sir.", "FLASHLIGHT_ON", isVoice)
            }
            "FLASHLIGHT_OFF" -> {
                deviceManager.setFlashlight(false)
                _uiState.value = _uiState.value.copy(isFlashlightOn = false)
                deliverResponse(rawQuery, "Flashlight deactivated, sir.", "FLASHLIGHT_OFF", isVoice)
            }
            "BATTERY_CHECK" -> {
                val batt = deviceManager.getBatteryStatus()
                _batteryInfo.value = batt
                val msg = "Battery power is at ${batt.level} percent with ${batt.health} condition, sir."
                deliverResponse(rawQuery, msg, "BATTERY_CHECK", isVoice)
            }
            "OPEN_APP" -> {
                val msg = deviceManager.launchApp(macro.actionParam)
                deliverResponse(rawQuery, msg, "OPEN_APP", isVoice)
            }
            "COMBO_PROTOCOL" -> {
                executeProtocol(macro.actionParam, rawQuery, isVoice)
            }
            "SPEAK" -> {
                deliverResponse(rawQuery, macro.actionParam, "SPEAK", isVoice)
            }
            else -> {
                queryAiBrain(rawQuery, isVoice)
            }
        }
    }

    fun executeProtocol(protocolName: String, query: String = protocolName, isVoice: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                coreState = JarvisCoreState.EXECUTING,
                activeProtocol = protocolName,
                statusMessage = "Executing Protocol: $protocolName..."
            )

            when (protocolName) {
                "PARTY_PROTOCOL" -> {
                    deviceManager.setSoundProfile("MAX")
                    deviceManager.setFlashlight(true)
                    delay(300)
                    deviceManager.setFlashlight(false)
                    delay(200)
                    deviceManager.setFlashlight(true)
                    val resp = if (_uiState.value.language == "hi") {
                        "House Party Protocol active sir! Media volume full hai aur visual lighting strobe sequence online hai!"
                    } else {
                        "House Party Protocol initiated, sir. Sound output at maximum and strobe illumination sequence running."
                    }
                    deliverResponse(query, resp, "PARTY_PROTOCOL", isVoice)
                }
                "STEALTH_PROTOCOL" -> {
                    deviceManager.setSoundProfile("SILENT")
                    deviceManager.setFlashlight(false)
                    _uiState.value = _uiState.value.copy(isFlashlightOn = false)
                    val resp = if (_uiState.value.language == "hi") {
                        "Stealth Mode active hai sir. Ringer mute kar diya gaya hai aur all lights deactivated hain."
                    } else {
                        "Stealth Protocol engaged. Device silenced and light emissions deactivated."
                    }
                    deliverResponse(query, resp, "STEALTH_PROTOCOL", isVoice)
                }
                "MORNING_PROTOCOL" -> {
                    val batt = deviceManager.getBatteryStatus()
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    val resp = if (_uiState.value.language == "hi") {
                        "Shubh Prabhat sir! Abhi samay $timeStr hai. Battery ${batt.level}% par hai. All core telemetry nominal hai. Have a fantastic productive day!"
                    } else {
                        "Good morning, sir. The current time is $timeStr. Power levels stand at ${batt.level} percent. All core diagnostics are nominal. Ready for your instructions."
                    }
                    deliverResponse(query, resp, "MORNING_PROTOCOL", isVoice)
                }
                "CLEAN_PROTOCOL" -> {
                    repository.clearLogs()
                    refreshDiagnostics()
                    val resp = if (_uiState.value.language == "hi") {
                        "Clean Slate protocol complete sir! Activity logs reset ho chuki hain aur memory refreshed hai."
                    } else {
                        "Clean Slate Protocol executed. Interaction logs cleared and system matrix refreshed."
                    }
                    deliverResponse(query, resp, "CLEAN_PROTOCOL", isVoice)
                }
                else -> {
                    deliverResponse(query, "Protocol $protocolName executed successfully.", protocolName, isVoice)
                }
            }
        }
    }

    private suspend fun queryAiBrain(prompt: String, isVoice: Boolean) {
        val historyList = recentLogs.value.take(4).map { it.userQuery to it.jarvisResponse }
        val result = repository.askJarvis(prompt, historyList)
        val textResponse = result.getOrDefault("System processing complete, sir.")
        deliverResponse(prompt, textResponse, "AI_QUERY", isVoice)
    }

    private suspend fun deliverResponse(
        query: String,
        response: String,
        actionTag: String? = null,
        isVoice: Boolean = false
    ) {
        repository.logInteraction(
            userQuery = query,
            jarvisResponse = response,
            actionExecuted = actionTag,
            isVoice = isVoice,
            languageCode = _uiState.value.language
        )

        _uiState.value = _uiState.value.copy(
            coreState = if (_uiState.value.isTtsEnabled) JarvisCoreState.SPEAKING else JarvisCoreState.STANDBY,
            latestResponse = response,
            statusMessage = "JARVIS Response Ready"
        )

        if (_uiState.value.isTtsEnabled) {
            ttsManager.speak(response)
        }
    }

    // Manual Hardware Toggles
    fun toggleFlashlightManual() {
        val newState = deviceManager.toggleFlashlight()
        _uiState.value = _uiState.value.copy(isFlashlightOn = deviceManager.isFlashlightActive())
        val msg = if (deviceManager.isFlashlightActive()) "Flashlight activated" else "Flashlight turned off"
        _uiState.value = _uiState.value.copy(statusMessage = msg)
    }

    fun addCustomMacro(phrase: String, actionType: String, actionParam: String, desc: String) {
        if (phrase.isBlank()) return
        viewModelScope.launch {
            repository.insertMacro(
                VoiceMacro(
                    triggerPhrase = phrase.trim().lowercase(),
                    actionType = actionType,
                    actionParam = actionParam,
                    description = desc,
                    isBuiltIn = false
                )
            )
            _uiState.value = _uiState.value.copy(statusMessage = "Custom Voice Macro '$phrase' created!")
        }
    }

    fun deleteMacro(macro: VoiceMacro) {
        viewModelScope.launch {
            repository.deleteMacro(macro.id)
        }
    }

    fun toggleMacro(macro: VoiceMacro) {
        viewModelScope.launch {
            repository.updateMacro(macro.copy(isEnabled = !macro.isEnabled))
        }
    }

    fun setGuidesCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setGuidesSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(guidesSearchQuery = query)
    }

    fun getFilteredGuides(): List<TutorialGuide> {
        val category = _uiState.value.selectedCategory
        val query = _uiState.value.guidesSearchQuery.trim().lowercase()
        return repository.getAllGuides().filter { guide ->
            val matchCategory = category == "ALL" || guide.category.equals(category, ignoreCase = true)
            val matchQuery = query.isEmpty() ||
                    guide.titleHindi.lowercase().contains(query) ||
                    guide.titleEnglish.lowercase().contains(query) ||
                    guide.summaryHindi.lowercase().contains(query) ||
                    guide.summaryEnglish.lowercase().contains(query)
            matchCategory && matchQuery
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            _uiState.value = _uiState.value.copy(statusMessage = "Interaction history cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
