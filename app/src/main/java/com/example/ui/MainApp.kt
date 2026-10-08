package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.ArcCoreScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.GuidesScreen
import com.example.ui.screens.SystemMatrixScreen
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.IronGold
import com.example.ui.theme.IronGoldGlow
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisViewModel
import com.example.viewmodel.NavigationTab

@Composable
fun MainApp(
    viewModel: JarvisViewModel,
    onStartVoiceRecognition: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showVoiceSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HudBackground)
    ) {
        // Decorative background HUD texture
        Image(
            painter = painterResource(id = R.drawable.bg_jarvis_hud),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.18f
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopHudBar(
                    language = uiState.language,
                    isTtsEnabled = uiState.isTtsEnabled,
                    isTorchOn = uiState.isFlashlightOn,
                    onToggleLanguage = {
                        val newLang = if (uiState.language == "hi") "en" else "hi"
                        viewModel.setLanguage(newLang)
                    },
                    onToggleTts = { viewModel.toggleTts() },
                    onOpenSettings = { showVoiceSettings = true },
                    onToggleTorch = { viewModel.toggleFlashlightManual() }
                )
            },
            bottomBar = {
                BottomHudNavigationBar(
                    currentTab = currentTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    NavigationTab.ARC_CORE -> ArcCoreScreen(
                        viewModel = viewModel,
                        onStartVoiceRecognition = onStartVoiceRecognition
                    )
                    NavigationTab.AUTOMATION -> AutomationScreen(viewModel = viewModel)
                    NavigationTab.GUIDES -> GuidesScreen(viewModel = viewModel)
                    NavigationTab.SYSTEM_MATRIX -> SystemMatrixScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showVoiceSettings) {
        VoiceSettingsDialog(
            currentPitch = uiState.ttsPitch,
            currentRate = uiState.ttsRate,
            onDismiss = { showVoiceSettings = false },
            onApply = { pitch, rate ->
                viewModel.updateTtsSettings(pitch, rate)
                showVoiceSettings = false
            }
        )
    }
}

@Composable
fun TopHudBar(
    language: String,
    isTtsEnabled: Boolean,
    isTorchOn: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTts: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTorch: () -> Unit
) {
    Surface(
        color = HudSurface.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_jarvis_logo),
                    contentDescription = "JARVIS HUD",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, ArcCyanPrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "J.A.R.V.I.S.",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArcCyanGlow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI VOICE & AUTOMATION HUB",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCyan,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quick Control Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Switcher Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HudSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleLanguage)
                        .testTag("lang_toggle_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = IronGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == "hi") "हिन्दी" else "ENG",
                            style = MaterialTheme.typography.labelSmall,
                            color = IronGoldGlow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Torch Quick Toggle
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = "Flashlight Toggle",
                        tint = if (isTorchOn) ArcCyanPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // TTS Speech Toggle
                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "TTS Toggle",
                        tint = if (isTtsEnabled) ArcCyanGlow else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Voice Pitch & Rate Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Modulation Settings",
                        tint = TextCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomHudNavigationBar(
    currentTab: NavigationTab,
    onSelectTab: (NavigationTab) -> Unit
) {
    Surface(
        color = HudSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf(
                NavigationTab.ARC_CORE to Icons.Default.Radio,
                NavigationTab.AUTOMATION to Icons.Default.Build,
                NavigationTab.GUIDES to Icons.Default.MenuBook,
                NavigationTab.SYSTEM_MATRIX to Icons.Default.Info
            )

            tabs.forEach { (tab, icon) ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.labelEnglish,
                            tint = if (isSelected) ArcCyanPrimary else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = tab.labelEnglish,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ArcCyanGlow else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArcCyanPrimary,
                        unselectedIconColor = TextMuted,
                        indicatorColor = ArcCyanPrimary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}

@Composable
fun VoiceSettingsDialog(
    currentPitch: Float,
    currentRate: Float,
    onDismiss: () -> Unit,
    onApply: (pitch: Float, rate: Float) -> Unit
) {
    var pitch by remember { mutableFloatStateOf(currentPitch) }
    var rate by remember { mutableFloatStateOf(currentRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HudSurfaceVariant,
        title = {
            Text(
                text = "JARVIS VOICE MODULATION",
                style = MaterialTheme.typography.titleMedium,
                color = ArcCyanGlow,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Adjust TTS acoustic timbre to sound authentic to Iron Man's J.A.R.V.I.S.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Voice Pitch Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Voice Pitch (Deep/Crisp):", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                        Text(text = "%.2fx".format(pitch), style = MaterialTheme.typography.labelSmall, color = IronGold)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyanPrimary,
                            activeTrackColor = ArcCyanPrimary,
                            inactiveTrackColor = HudSurfaceElevated
                        )
                    )
                }

                // Speech Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Speech Rate (Cadence):", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                        Text(text = "%.2fx".format(rate), style = MaterialTheme.typography.labelSmall, color = IronGold)
                    }
                    Slider(
                        value = rate,
                        onValueChange = { rate = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyanPrimary,
                            activeTrackColor = ArcCyanPrimary,
                            inactiveTrackColor = HudSurfaceElevated
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HudBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "RECOMMENDED JARVIS PRESETS:",
                            style = MaterialTheme.typography.labelSmall,
                            color = IronGoldGlow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tony Stark British JARVIS: Pitch 0.90x | Rate 1.05x\n• Hindi Robot Matrix: Pitch 0.95x | Rate 1.00x",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(pitch, rate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArcCyanPrimary,
                    contentColor = HudBackground
                )
            ) {
                Text(text = "Apply Acoustics", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
