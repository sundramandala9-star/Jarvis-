package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ArcReactorView
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudBorderBright
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.IronGold
import com.example.ui.theme.IronGoldGlow
import com.example.ui.theme.IronRedGlow
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisCoreState
import com.example.viewmodel.JarvisViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArcCoreScreen(
    viewModel: JarvisViewModel,
    onStartVoiceRecognition: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val quickCommands = listOf(
        "Torch on karo" to "FLASHLIGHT_ON",
        "Battery status do" to "BATTERY_CHECK",
        "House party protocol" to "PARTY_PROTOCOL",
        "Stealth mode" to "STEALTH_PROTOCOL",
        "Good morning Jarvis" to "MORNING_PROTOCOL",
        "Open camera" to "OPEN_CAMERA",
        "Iron Man quote" to "AI_QUOTE",
        "Mobile smart kaise banaye" to "AI_TRICK"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Top Telemetry Status Bar
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (uiState.coreState) {
                                        JarvisCoreState.STANDBY -> TerminalGreen
                                        JarvisCoreState.LISTENING -> IronRedGlow
                                        JarvisCoreState.PROCESSING -> IronGold
                                        JarvisCoreState.SPEAKING -> ArcCyanGlow
                                        JarvisCoreState.EXECUTING -> AlertOrange
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CORE: ${uiState.coreState.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PWR: ${batteryInfo.level}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (batteryInfo.level > 20) TerminalGreen else IronRedGlow
                        )
                        Text(
                            text = if (uiState.language == "hi") "HIN / ENG" else "ENG / HIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = IronGold
                        )
                        Text(
                            text = "SEC: NOMINAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 2. Interactive Arc Reactor Hero
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "J.A.R.V.I.S. NEURAL CORE",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcCyanGlow,
                    letterSpacing = 2.sp
                )
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                ArcReactorView(
                    coreState = uiState.coreState,
                    modifier = Modifier.testTag("arc_reactor_touch_target"),
                    onClick = {
                        if (uiState.isListening) {
                            viewModel.setListeningState(false)
                        } else {
                            onStartVoiceRecognition()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Action Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = {
                            if (uiState.isListening) {
                                viewModel.setListeningState(false)
                            } else {
                                onStartVoiceRecognition()
                            }
                        })
                        .border(
                            1.dp,
                            if (uiState.isListening) IronRedGlow else ArcCyanPrimary,
                            RoundedCornerShape(24.dp)
                        )
                        .testTag("voice_assistant_mic_btn"),
                    color = if (uiState.isListening) IronRedGlow.copy(alpha = 0.2f) else HudSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input Trigger",
                            tint = if (uiState.isListening) IronRedGlow else ArcCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isListening) "LISTENING... TAP TO STOP" else "TAP ARC REACTOR OR SPEAK",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (uiState.isListening) IronRedGlow else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Response Card & Live Feed
        item {
            HudGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jarvis_response_card"),
                borderColor = HudBorderBright,
                glowColor = ArcCyanPrimary.copy(alpha = 0.15f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SYS.RESPONSE.FEED",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArcCyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.currentQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• \"${uiState.currentQuery}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.ttsManager.speak(uiState.latestResponse)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Replay JARVIS Voice",
                                tint = ArcCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.latestResponse,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 4. Text Command Input Box
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    placeholder = {
                        Text(
                            text = if (uiState.language == "hi") "Command likhein ya bole (e.g. 'Torch on')" else "Type or speak command...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyanPrimary,
                        unfocusedBorderColor = HudBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HudSurfaceVariant,
                        unfocusedContainerColor = HudSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.processCommand(textInput, isVoice = false)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (textInput.isNotBlank()) {
                                viewModel.processCommand(textInput, isVoice = false)
                                textInput = ""
                                keyboardController?.hide()
                            }
                        }
                        .border(1.dp, ArcCyanPrimary, RoundedCornerShape(12.dp))
                        .testTag("command_send_button"),
                    color = ArcCyanPrimary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Command",
                            tint = ArcCyanPrimary
                        )
                    }
                }
            }
        }

        // 5. Quick Trigger Commands Flow
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK PROTOCOL SHORTCUTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickCommands.forEach { (label, tag) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.processCommand(label, isVoice = false)
                                }
                                .border(1.dp, HudBorder, RoundedCornerShape(16.dp)),
                            color = HudSurfaceElevated
                        ) {
                            Text(
                                text = "▸ $label",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextCyan,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Terminal Logs Header
        if (recentLogs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT PROTOCOL ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "CLEAR LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = IronGold,
                        modifier = Modifier.clickable { viewModel.clearHistory() }
                    )
                }
            }

            items(recentLogs.take(5)) { log ->
                HudGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = HudBorder.copy(alpha = 0.5f),
                    showTechCorners = false
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "USER: \"${log.userQuery}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IronGoldGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (log.actionExecuted != null) {
                                Text(
                                    text = "[${log.actionExecuted}]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TerminalGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "JARVIS: ${log.jarvisResponse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
