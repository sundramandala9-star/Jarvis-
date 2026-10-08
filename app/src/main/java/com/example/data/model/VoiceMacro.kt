package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_macros")
data class VoiceMacro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerPhrase: String,
    val actionType: String, // e.g. "FLASHLIGHT_ON", "FLASHLIGHT_OFF", "BATTERY_CHECK", "SPEAK", "OPEN_APP", "COMBO_PROTOCOL"
    val actionParam: String = "", // e.g. text to speak or package name or volume level
    val description: String,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val iconName: String = "ic_bolt"
)
