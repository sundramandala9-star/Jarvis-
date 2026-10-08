package com.example.service

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(
        val fileName: String,
        val progress: Float,
        val percentage: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speedText: String
    ) : DownloadState()
    data class Success(
        val fileName: String,
        val filePath: String,
        val fileSize: Long,
        val formattedSize: String,
        val fileUri: Uri?,
        val downloadTime: String
    ) : DownloadState()
    data class Failed(
        val fileName: String,
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : DownloadState()
}

data class DownloadedApkItem(
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileSizeText: String,
    val url: String,
    val dateDownloaded: String,
    val isInstalledCandidate: Boolean = true
)

class ApkDownloadManager(private val context: Context) {

    companion object {
        private const val TAG = "ApkDownloadManager"
        const val CHANNEL_ID = "apk_download_channel"
        const val CHANNEL_NAME = "APK Downloads"
        const val NOTIFICATION_ID = 4040
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _recentDownloads = MutableStateFlow<List<DownloadedApkItem>>(emptyList())
    val recentDownloads: StateFlow<List<DownloadedApkItem>> = _recentDownloads.asStateFlow()

    @Volatile
    private var isCancelled = false

    init {
        createNotificationChannel()
        loadExistingDownloads()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for APK download progress and completion"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun isInternetConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    fun getNetworkTypeName(): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "Disconnected"
        val activeNetwork = connectivityManager.activeNetwork ?: return "Disconnected"
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Disconnected"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (Online)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Data (Online)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet (Online)"
            else -> "Connected"
        }
    }

    fun isStoragePermissionRequired(): Boolean {
        // Android 10+ (API 29+) uses Scoped Storage / standard downloads and doesn't require WRITE_EXTERNAL_STORAGE
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    }

    fun hasStoragePermission(): Boolean {
        if (!isStoragePermissionRequired()) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationPermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun hasNotificationPermission(): Boolean {
        if (!isNotificationPermissionRequired()) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun cancelDownload() {
        isCancelled = true
        _downloadState.value = DownloadState.Failed(
            fileName = "Download",
            errorMessage = "Download was cancelled by user.",
            canRetry = true
        )
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }

    suspend fun downloadApk(url: String, customFileName: String? = null): Boolean = withContext(Dispatchers.IO) {
        isCancelled = false

        if (!isInternetConnected()) {
            _downloadState.value = DownloadState.Failed(
                fileName = customFileName ?: "Application.apk",
                errorMessage = "No internet connection detected. Please connect to Wi-Fi or Mobile Data and try again."
            )
            showFailureNotification("No Internet Connection", "Cannot download APK without an active network connection.")
            return@withContext false
        }

        val sanitizedUrl = url.trim()
        if (sanitizedUrl.isBlank() || (!sanitizedUrl.startsWith("http://") && !sanitizedUrl.startsWith("https://"))) {
            _downloadState.value = DownloadState.Failed(
                fileName = "Application.apk",
                errorMessage = "Invalid download URL. The URL must start with http:// or https://"
            )
            return@withContext false
        }

        // Determine File Name
        var fileName = customFileName?.trim() ?: ""
        if (fileName.isBlank()) {
            val urlPath = Uri.parse(sanitizedUrl).lastPathSegment
            fileName = if (!urlPath.isNullOrBlank() && urlPath.endsWith(".apk", ignoreCase = true)) {
                urlPath
            } else {
                "downloaded_app_${System.currentTimeMillis() % 10000}.apk"
            }
        }
        if (!fileName.endsWith(".apk", ignoreCase = true)) {
            fileName += ".apk"
        }

        // Determine Target Directory: Device's Public Downloads folder
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val targetFile = File(downloadsDir, fileName)

        _downloadState.value = DownloadState.Downloading(
            fileName = fileName,
            progress = 0f,
            percentage = 0,
            bytesDownloaded = 0,
            totalBytes = -1,
            speedText = "Connecting..."
        )

        showProgressNotification(fileName, 0, 0, -1)

        try {
            val request = Request.Builder()
                .url(sanitizedUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorMsg = "HTTP Server Error: ${response.code} (${response.message})"
                _downloadState.value = DownloadState.Failed(fileName, errorMsg)
                showFailureNotification(fileName, errorMsg)
                return@withContext false
            }

            val body = response.body
            if (body == null) {
                val errorMsg = "Empty response received from server."
                _downloadState.value = DownloadState.Failed(fileName, errorMsg)
                showFailureNotification(fileName, errorMsg)
                return@withContext false
            }

            val contentLength = body.contentLength()
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L
                var lastUpdateTime = System.currentTimeMillis()
                var bytesSinceLastUpdate = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isCancelled) {
                        targetFile.delete()
                        return@withContext false
                    }

                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    bytesSinceLastUpdate += bytesRead

                    val currentTime = System.currentTimeMillis()
                    val timeDiff = currentTime - lastUpdateTime

                    if (timeDiff >= 300 || totalBytesRead == contentLength) {
                        val speedBps = if (timeDiff > 0) (bytesSinceLastUpdate * 1000) / timeDiff else 0
                        val speedText = formatSpeed(speedBps)

                        val progress = if (contentLength > 0) {
                            (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val percentage = (progress * 100).toInt()

                        _downloadState.value = DownloadState.Downloading(
                            fileName = fileName,
                            progress = progress,
                            percentage = percentage,
                            bytesDownloaded = totalBytesRead,
                            totalBytes = contentLength,
                            speedText = speedText
                        )

                        // Update system notification periodically
                        showProgressNotification(fileName, percentage, totalBytesRead, contentLength)

                        lastUpdateTime = currentTime
                        bytesSinceLastUpdate = 0
                    }
                }

                outputStream.flush()

                // Success!
                val formattedSize = formatFileSize(targetFile.length())
                val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                val contentUri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        targetFile
                    )
                } catch (e: Exception) {
                    Uri.fromFile(targetFile)
                }

                _downloadState.value = DownloadState.Success(
                    fileName = fileName,
                    filePath = targetFile.absolutePath,
                    fileSize = targetFile.length(),
                    formattedSize = formattedSize,
                    fileUri = contentUri,
                    downloadTime = timeStr
                )

                // Save to recent downloads list
                addRecentDownload(
                    DownloadedApkItem(
                        id = System.currentTimeMillis().toString(),
                        fileName = fileName,
                        filePath = targetFile.absolutePath,
                        fileSizeText = formattedSize,
                        url = sanitizedUrl,
                        dateDownloaded = timeStr
                    )
                )

                showSuccessNotification(fileName, targetFile, contentUri)
                true
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: CancellationException) {
            targetFile.delete()
            _downloadState.value = DownloadState.Failed(fileName, "Download cancelled.")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Download failed: ${e.message}")
            val errorMsg = e.localizedMessage ?: "Unknown network error during APK download."
            _downloadState.value = DownloadState.Failed(fileName, errorMsg)
            showFailureNotification(fileName, errorMsg)
            false
        }
    }

    private fun showProgressNotification(fileName: String, progressPercent: Int, downloaded: Long, total: Long) {
        if (!hasNotificationPermission()) return

        val text = if (total > 0) {
            "$progressPercent% (${formatFileSize(downloaded)} / ${formatFileSize(total)})"
        } else {
            "${formatFileSize(downloaded)} downloaded"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $fileName")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (total > 0) {
            builder.setProgress(100, progressPercent, false)
        } else {
            builder.setProgress(100, progressPercent, true)
        }

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing: ${e.message}")
        }
    }

    private fun showSuccessNotification(fileName: String, file: File, contentUri: Uri?) {
        if (!hasNotificationPermission()) return

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("APK Download Succeeded")
            .setContentText("$fileName successfully saved to Downloads folder")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("File '$fileName' (${formatFileSize(file.length())}) was successfully saved to your device's Downloads directory.\nTap to open and install.")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_menu_view, "Install APK", pendingIntent)

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing: ${e.message}")
        }
    }

    private fun showFailureNotification(fileName: String, errorMsg: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("APK Download Failed")
            .setContentText("Failed to download $fileName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("The APK download for '$fileName' failed.\nReason: $errorMsg")
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing: ${e.message}")
        }
    }

    fun openApkFile(file: File): Boolean {
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Cannot launch installer: ${e.message}")
            false
        }
    }

    fun openDownloadsFolder(): Boolean {
        return try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path), "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun shareApkFile(file: File): Boolean {
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Share APK via").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun addRecentDownload(item: DownloadedApkItem) {
        val current = _recentDownloads.value.toMutableList()
        current.removeAll { it.filePath == item.filePath }
        current.add(0, item)
        _recentDownloads.value = current.take(15)
    }

    fun loadExistingDownloads() {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir.exists() && downloadsDir.isDirectory) {
            val apkFiles = downloadsDir.listFiles { file ->
                file.isFile && file.name.endsWith(".apk", ignoreCase = true)
            }?.sortedByDescending { it.lastModified() } ?: emptyList()

            val items = apkFiles.map { file ->
                val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))
                DownloadedApkItem(
                    id = file.name + file.lastModified(),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSizeText = formatFileSize(file.length()),
                    url = "Local File",
                    dateDownloaded = timeStr
                )
            }
            _recentDownloads.value = items.take(15)
        }
    }

    fun deleteDownloadedFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                val current = _recentDownloads.value.toMutableList()
                current.removeAll { it.filePath == filePath }
                _recentDownloads.value = current
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }
}
