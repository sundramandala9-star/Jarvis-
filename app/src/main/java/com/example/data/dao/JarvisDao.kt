package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InteractionLog
import com.example.data.model.VoiceMacro
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Macros
    @Query("SELECT * FROM voice_macros ORDER BY isBuiltIn DESC, id DESC")
    fun getAllMacros(): Flow<List<VoiceMacro>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: VoiceMacro): Long

    @Update
    suspend fun updateMacro(macro: VoiceMacro)

    @Query("DELETE FROM voice_macros WHERE id = :macroId")
    suspend fun deleteMacro(macroId: Long)

    @Query("SELECT * FROM voice_macros WHERE isEnabled = 1")
    suspend fun getActiveMacrosList(): List<VoiceMacro>

    // Interaction Logs
    @Query("SELECT * FROM interaction_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<InteractionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InteractionLog): Long

    @Query("DELETE FROM interaction_logs")
    suspend fun clearAllLogs()
}
