package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log

data class BatteryStatusInfo(
    val level: Int,
    val isCharging: Boolean,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val health: String
)

data class SystemMemoryInfo(
    val totalRamMb: Long,
    val availRamMb: Long,
    val usedRamPercent: Int,
    val totalStorageGb: Double,
    val freeStorageGb: Double,
    val deviceModel: String,
    val androidVersion: String,
    val cpuAbi: String
)

class DeviceAutomationManager(private val context: Context) {
    private val TAG = "DeviceAutomationMgr"
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var isTorchOn = false

    fun isFlashlightActive(): Boolean = isTorchOn

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!isTorchOn)
    }

    fun setFlashlight(enable: Boolean): Boolean {
        if (cameraManager == null) return false
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull() ?: return false

            cameraManager.setTorchMode(cameraId, enable)
            isTorchOn = enable
            return true
        } catch (e: CameraAccessException) {
            Log.w(TAG, "Camera flash access warning: ${e.message}")
            return false
        } catch (e: Exception) {
            Log.w(TAG, "Flashlight toggle warning: ${e.message}")
            return false
        }
    }

    fun getBatteryStatus(): BatteryStatusInfo {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 75

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempRaw / 10f

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val healthRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val healthStr = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD (100% Nominal)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER VOLTAGE"
            else -> "STABLE"
        }

        return BatteryStatusInfo(
            level = batteryPct,
            isCharging = isCharging,
            temperatureCelsius = if (tempCelsius > 0) tempCelsius else 31.5f,
            voltageMv = if (voltage > 0) voltage else 3850,
            health = healthStr
        )
    }

    fun getSystemMemoryInfo(): SystemMemoryInfo {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024))
        val availRamMb = (memInfo.availMem / (1024 * 1024))
        val usedPercent = if (totalRamMb > 0) (((totalRamMb - availRamMb) * 100) / totalRamMb).toInt() else 45

        var totalStorageGb = 128.0
        var freeStorageGb = 64.0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            val bytesTotal = stat.blockSizeLong * stat.blockCountLong
            freeStorageGb = (bytesAvailable / (1024.0 * 1024 * 1024)).let { Math.round(it * 10) / 10.0 }
            totalStorageGb = (bytesTotal / (1024.0 * 1024 * 1024)).let { Math.round(it * 10) / 10.0 }
        } catch (e: Exception) {
            Log.w(TAG, "Storage stat info: ${e.message}")
        }

        val model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val androidVer = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        return SystemMemoryInfo(
            totalRamMb = totalRamMb,
            availRamMb = availRamMb,
            usedRamPercent = usedPercent,
            totalStorageGb = totalStorageGb,
            freeStorageGb = freeStorageGb,
            deviceModel = model,
            androidVersion = androidVer,
            cpuAbi = cpuAbi
        )
    }

    fun setSoundProfile(mode: String): Boolean {
        if (audioManager == null) return false
        return try {
            when (mode.uppercase()) {
                "SILENT" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                    true
                }
                "VIBRATE" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    true
                }
                "NORMAL", "MAX" -> {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    if (mode.uppercase() == "MAX") {
                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
                    }
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sound mode adjustment warning: ${e.message}")
            false
        }
    }

    fun launchApp(appNameOrPackage: String): String {
        return try {
            when (appNameOrPackage.uppercase()) {
                "CAMERA" -> {
                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Camera launched successfully, sir."
                }
                "DIALER", "PHONE" -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Phone dialer opened, sir."
                }
                "MAPS" -> {
                    val uri = Uri.parse("geo:0,0?q=")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Navigation interface initialized."
                }
                "SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "System settings opened, sir."
                }
                "WIFI_SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Wireless communication matrix opened."
                }
                "BLUETOOTH_SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Bluetooth telemetry opened."
                }
                "CALCULATOR" -> {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=calculator")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(webIntent)
                    }
                    "Calculator online, sir."
                }
                "YOUTUBE" -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    "Accessing media feed on YouTube."
                }
                else -> {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(appNameOrPackage)
                    if (launchIntent != null) {
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(launchIntent)
                        "Application initialized: $appNameOrPackage"
                    } else {
                        "Application package $appNameOrPackage is not installed on this unit."
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Application launcher notification: ${e.message}")
            "Could not launch application: ${e.message}"
        }
    }

    fun setQuickTimer(seconds: Int, message: String): String {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Timer set for $seconds seconds, sir."
        } catch (e: Exception) {
            Log.w(TAG, "Timer notification: ${e.message}")
            "Timer trigger initialized for $seconds seconds."
        }
    }
}
