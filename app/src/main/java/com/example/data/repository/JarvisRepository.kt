package com.example.data.repository

import com.example.data.TutorialsProvider
import com.example.data.api.GeminiClient
import com.example.data.dao.JarvisDao
import com.example.data.model.InteractionLog
import com.example.data.model.TutorialGuide
import com.example.data.model.VoiceMacro
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val jarvisDao: JarvisDao
) {
    val allMacros: Flow<List<VoiceMacro>> = jarvisDao.getAllMacros()
    val recentLogs: Flow<List<InteractionLog>> = jarvisDao.getRecentLogs()

    suspend fun insertMacro(macro: VoiceMacro): Long = jarvisDao.insertMacro(macro)

    suspend fun updateMacro(macro: VoiceMacro) = jarvisDao.updateMacro(macro)

    suspend fun deleteMacro(macroId: Long) = jarvisDao.deleteMacro(macroId)

    suspend fun getActiveMacros(): List<VoiceMacro> = jarvisDao.getActiveMacrosList()

    suspend fun logInteraction(
        userQuery: String,
        jarvisResponse: String,
        actionExecuted: String? = null,
        isVoice: Boolean = true,
        languageCode: String = "hi"
    ): Long {
        return jarvisDao.insertLog(
            InteractionLog(
                userQuery = userQuery,
                jarvisResponse = jarvisResponse,
                actionExecuted = actionExecuted,
                isVoice = isVoice,
                languageCode = languageCode
            )
        )
    }

    suspend fun clearLogs() = jarvisDao.clearAllLogs()

    fun getAllGuides(): List<TutorialGuide> = TutorialsProvider.guides

    suspend fun askJarvis(
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        return GeminiClient.queryJarvis(prompt, history)
    }
}
