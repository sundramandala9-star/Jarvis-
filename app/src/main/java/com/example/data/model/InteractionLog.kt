package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_logs")
data class InteractionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userQuery: String,
    val jarvisResponse: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionExecuted: String? = null,
    val isVoice: Boolean = true,
    val languageCode: String = "hi" // "hi" or "en"
)
