package com.example.ui.screens

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.ApkDownloadManager
import com.example.service.DownloadState
import com.example.service.DownloadedApkItem
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.IronGold
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File

data class SampleApkPreset(
    val title: String,
    val subtitle: String,
    val url: String,
    val suggestedName: String
)

@Composable
fun ApkDownloaderScreen(
    apkManager: ApkDownloadManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val downloadState by apkManager.downloadState.collectAsStateWithLifecycle()
    val recentDownloads by apkManager.recentDownloads.collectAsStateWithLifecycle()

    var urlInput by remember { mutableStateOf("https://f-droid.org/F-Droid.apk") }
    var fileNameInput by remember { mutableStateOf("F-Droid.apk") }
    var isConnected by remember { mutableStateOf(apkManager.isInternetConnected()) }
    var networkType by remember { mutableStateOf(apkManager.getNetworkTypeName()) }

    var hasStoragePerm by remember { mutableStateOf(apkManager.hasStoragePermission()) }
    var hasNotifPerm by remember { mutableStateOf(apkManager.hasNotificationPermission()) }

    // Storage permission launcher (for Android 9 and lower)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePerm = isGranted
        if (isGranted) {
            Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Notification permission launcher (for Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifPerm = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled for downloads", Toast.LENGTH_SHORT).show()
        }
    }

    // Refresh network & permissions periodically or on load
    LaunchedEffect(Unit) {
        isConnected = apkManager.isInternetConnected()
        networkType = apkManager.getNetworkTypeName()
        hasStoragePerm = apkManager.hasStoragePermission()
        hasNotifPerm = apkManager.hasNotificationPermission()
        apkManager.loadExistingDownloads()
    }

    val samplePresets = remember {
        listOf(
            SampleApkPreset(
                title = "F-Droid Store",
                subtitle = "Open Source App Store",
                url = "https://f-droid.org/F-Droid.apk",
                suggestedName = "F-Droid.apk"
            ),
            SampleApkPreset(
                title = "Signal Android",
                subtitle = "Secure Messenger APK",
                url = "https://updates.signal.org/android/latest.apk",
                suggestedName = "Signal-latest.apk"
            ),
            SampleApkPreset(
                title = "VLC Media Player",
                subtitle = "Video & Audio Player",
                url = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-arm64-v8a.apk",
                suggestedName = "VLC-Android.apk"
            ),
            SampleApkPreset(
                title = "K-9 Mail Client",
                subtitle = "Open Source Email",
                url = "https://github.com/thunderbird/thunderbird-android/releases/download/6.804/k9-6.804.apk",
                suggestedName = "K9-Mail.apk"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Screen Header Card
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ArcCyanGlow.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArcCyanPrimary.copy(alpha = 0.2f))
                                .border(1.dp, ArcCyanPrimary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download APK",
                                tint = ArcCyanPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "APK DOWNLOAD ENGINE",
                                color = TextCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Direct-to-Downloads installer & progress monitor",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // System Telemetry Chips (Internet + Storage + Notifications)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Internet Status Badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isConnected) Color(0xFF0F3A2A) else Color(0xFF3D1616))
                                .border(1.dp, if (isConnected) Color(0xFF00E676) else Color(0xFFFF5252), RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isConnected) Color(0xFF00E676) else Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "Net: Connected" else "Net: Offline",
                                    color = if (isConnected) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Storage Status Badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HudSurfaceVariant)
                                .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = IronGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (apkManager.isStoragePermissionRequired()) {
                                        if (hasStoragePerm) "Storage: Ready" else "Storage: Needed"
                                    } else "Downloads: Direct",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Notification permission banner if missing on Android 13+
                    if (apkManager.isNotificationPermissionRequired() && !hasNotifPerm) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C2410))
                                .border(1.dp, IronGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = IronGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable notifications for download completion alerts",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IronGold),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Allow", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Storage permission banner for Android 9 and lower
                    if (apkManager.isStoragePermissionRequired() && !hasStoragePerm) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF381A1A))
                                .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Storage write permission needed for Downloads folder",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Grant", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Download Status Card (Active, Success, or Failed)
        item {
            AnimatedVisibility(
                visible = downloadState !is DownloadState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val state = downloadState) {
                    is DownloadState.Downloading -> {
                        HudGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_download_card"),
                            borderColor = ArcCyanPrimary
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = ArcCyanPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "DOWNLOADING: ${state.fileName}",
                                            color = TextCyan,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Saving directly to Downloads folder...",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { apkManager.cancelDownload() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Cancel Download",
                                            tint = Color(0xFFFF5252)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Progress Bar
                                if (state.totalBytes > 0) {
                                    LinearProgressIndicator(
                                        progress = { state.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .testTag("download_progress_bar"),
                                        color = ArcCyanPrimary,
                                        trackColor = HudSurfaceVariant
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .testTag("download_progress_bar"),
                                        color = ArcCyanPrimary,
                                        trackColor = HudSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Progress Details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (state.totalBytes > 0) {
                                            "${state.percentage}% • ${apkManager.formatFileSize(state.bytesDownloaded)} / ${apkManager.formatFileSize(state.totalBytes)}"
                                        } else {
                                            "${apkManager.formatFileSize(state.bytesDownloaded)} downloaded"
                                        },
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = IronGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = state.speedText,
                                            color = IronGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is DownloadState.Success -> {
                        HudGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("download_success_card"),
                            borderColor = Color(0xFF00E676)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "DOWNLOAD COMPLETED SUCCESSFULLY",
                                            color = Color(0xFF00E676),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = state.fileName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = { apkManager.resetState() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = TextMuted)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HudSurfaceVariant)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Saved in Downloads folder:",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = state.filePath,
                                            color = TextCyan,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Size: ${state.formattedSize} • ${state.downloadTime}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Action Buttons for Downloaded APK
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            apkManager.openApkFile(File(state.filePath))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("install_apk_button")
                                    ) {
                                        Icon(Icons.Default.InstallMobile, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Install APK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            apkManager.shareApkFile(File(state.filePath))
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", color = TextPrimary, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            apkManager.openDownloadsFolder()
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = ArcCyanPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Folder", color = ArcCyanPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    is DownloadState.Failed -> {
                        HudGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("download_failed_card"),
                            borderColor = Color(0xFFFF5252)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "DOWNLOAD FAILED",
                                            color = Color(0xFFFF5252),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = state.fileName,
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { apkManager.resetState() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = TextMuted)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E1717))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = state.errorMessage,
                                        color = Color(0xFFFF8A80),
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { apkManager.resetState() },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Dismiss", color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (apkManager.isStoragePermissionRequired() && !hasStoragePerm) {
                                                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            } else {
                                                scope.launch {
                                                    apkManager.downloadApk(urlInput, fileNameInput)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Download", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    DownloadState.Idle -> {}
                }
            }
        }

        // Download Form Card
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PACKAGE SOURCE CONFIGURATION",
                        color = IronGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // URL Input Field
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            // auto infer filename if it ends with .apk
                            val segment = it.trim().substringAfterLast('/')
                            if (segment.endsWith(".apk", ignoreCase = true) && segment.length < 50) {
                                fileNameInput = segment
                            }
                        },
                        label = { Text("Direct APK Download URL", color = TextSecondary) },
                        placeholder = { Text("https://example.com/app.apk", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = ArcCyanPrimary)
                        },
                        trailingIcon = {
                            Row {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(onClick = { urlInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        urlInput = clip.trim()
                                        val segment = clip.trim().substringAfterLast('/')
                                        if (segment.endsWith(".apk", ignoreCase = true)) {
                                            fileNameInput = segment
                                        }
                                        Toast.makeText(context, "URL pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = IronGold)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcCyanPrimary,
                            unfocusedBorderColor = HudBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = ArcCyanPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_url_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Optional File Name Input Field
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("Save As File Name (in Downloads folder)", color = TextSecondary) },
                        placeholder = { Text("my_app.apk", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = IronGold)
                        },
                        trailingIcon = {
                            if (fileNameInput.isNotEmpty()) {
                                IconButton(onClick = { fileNameInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IronGold,
                            unfocusedBorderColor = HudBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = IronGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_filename_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sample Presets (Quick Test APKs)
                    Text(
                        text = "QUICK PRESET APKs (1-TAP TEST):",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(samplePresets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HudSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (urlInput == preset.url) ArcCyanPrimary else HudBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        urlInput = preset.url
                                        fileNameInput = preset.suggestedName
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = preset.title,
                                        color = if (urlInput == preset.url) ArcCyanPrimary else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Start Download Primary Button
                    val isDownloading = downloadState is DownloadState.Downloading
                    Button(
                        onClick = {
                            // Check storage permission if needed on older SDKs
                            if (apkManager.isStoragePermissionRequired() && !hasStoragePerm) {
                                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                return@Button
                            }

                            if (!apkManager.isInternetConnected()) {
                                Toast.makeText(context, "No active internet connection", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            if (urlInput.isBlank()) {
                                Toast.makeText(context, "Please enter an APK download URL", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            scope.launch {
                                apkManager.downloadApk(urlInput, fileNameInput)
                            }
                        },
                        enabled = !isDownloading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArcCyanPrimary,
                            disabledContainerColor = ArcCyanPrimary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDownloading) "DOWNLOADING IN PROGRESS..." else "DOWNLOAD APK TO DEVICE",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Open Downloads Directory Button
                    OutlinedButton(
                        onClick = { apkManager.openDownloadsFolder() },
                        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_downloads_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = IronGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN DEVICE DOWNLOADS FOLDER",
                            color = IronGold,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Recent Downloaded APKs in Downloads Folder
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOWNLOADS FOLDER APK PACKAGES",
                            color = TextCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { apkManager.loadExistingDownloads() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh list", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentDownloads.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HudSurfaceVariant)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No APK packages downloaded yet",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Downloaded APKs will be saved to your device's Downloads folder and appear here for fast installation.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recentDownloads.forEach { item ->
                                DownloadedApkRowItem(
                                    item = item,
                                    onInstall = {
                                        apkManager.openApkFile(File(item.filePath))
                                    },
                                    onShare = {
                                        apkManager.shareApkFile(File(item.filePath))
                                    },
                                    onDelete = {
                                        apkManager.deleteDownloadedFile(item.filePath)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DownloadedApkRowItem(
    item: DownloadedApkItem,
    onInstall: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HudSurfaceElevated)
            .border(1.dp, HudBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ArcCyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InstallMobile,
                        contentDescription = null,
                        tint = ArcCyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.fileSizeText} • ${item.dateDownloaded}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete APK",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyanPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(Icons.Default.InstallMobile, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Install APK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShare,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = TextPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}
