package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.JarvisDao
import com.example.data.model.InteractionLog
import com.example.data.model.VoiceMacro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VoiceMacro::class, InteractionLog::class],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_assistant_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialMacros(database.jarvisDao())
                    }
                }
            }

            private suspend fun populateInitialMacros(dao: JarvisDao) {
                val defaultMacros = listOf(
                    VoiceMacro(
                        triggerPhrase = "turn on flashlight",
                        actionType = "FLASHLIGHT_ON",
                        actionParam = "",
                        description = "Flashlight / Torch activate karta hai",
                        isBuiltIn = true,
                        iconName = "ic_flashlight"
                    ),
                    VoiceMacro(
                        triggerPhrase = "torch on karo",
                        actionType = "FLASHLIGHT_ON",
                        actionParam = "",
                        description = "Hindi Voice Trigger: Torch on",
                        isBuiltIn = true,
                        iconName = "ic_flashlight"
                    ),
                    VoiceMacro(
                        triggerPhrase = "turn off flashlight",
                        actionType = "FLASHLIGHT_OFF",
                        actionParam = "",
                        description = "Torch deactivate karta hai",
                        isBuiltIn = true,
                        iconName = "ic_flashlight"
                    ),
                    VoiceMacro(
                        triggerPhrase = "battery check karo",
                        actionType = "BATTERY_CHECK",
                        actionParam = "",
                        description = "Battery level aur health diagnostic bolta hai",
                        isBuiltIn = true,
                        iconName = "ic_battery"
                    ),
                    VoiceMacro(
                        triggerPhrase = "good morning jarvis",
                        actionType = "COMBO_PROTOCOL",
                        actionParam = "MORNING_PROTOCOL",
                        description = "Morning briefing: Time, battery, system status & motivational Iron Man quote",
                        isBuiltIn = true,
                        iconName = "ic_sun"
                    ),
                    VoiceMacro(
                        triggerPhrase = "house party protocol",
                        actionType = "COMBO_PROTOCOL",
                        actionParam = "PARTY_PROTOCOL",
                        description = "Flashlight strobe simulation + max media volume + Party status",
                        isBuiltIn = true,
                        iconName = "ic_party"
                    ),
                    VoiceMacro(
                        triggerPhrase = "open camera",
                        actionType = "OPEN_APP",
                        actionParam = "CAMERA",
                        description = "Camera view kholta hai",
                        isBuiltIn = true,
                        iconName = "ic_camera"
                    ),
                    VoiceMacro(
                        triggerPhrase = "stealth mode",
                        actionType = "COMBO_PROTOCOL",
                        actionParam = "STEALTH_PROTOCOL",
                        description = "Silent profile + flashlight off + dimmed HUD",
                        isBuiltIn = true,
                        iconName = "ic_shield"
                    )
                )
                for (macro in defaultMacros) {
                    dao.insertMacro(macro)
                }
            }
        }
    }
}
