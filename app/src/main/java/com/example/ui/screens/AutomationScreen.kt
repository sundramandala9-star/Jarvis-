package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VoiceMacro
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudBorderGold
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.IronGold
import com.example.ui.theme.IronGoldGlow
import com.example.ui.theme.IronRed
import com.example.ui.theme.IronRedGlow
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun AutomationScreen(
    viewModel: JarvisViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allMacros by viewModel.allMacros.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DEVICE AUTOMATION & HARDWARE MATRIX",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcCyanGlow
                )
                Text(
                    text = "Control physical hardware modules, run Tony Stark protocols, or build no-code voice macros.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 1. Hardware Control Grid
            item {
                HudGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = HudBorder
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "HARDWARE QUICK TOGGLES",
                            style = MaterialTheme.typography.labelSmall,
                            color = IronGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HardwareTile(
                                title = if (uiState.isFlashlightOn) "Torch ON" else "Torch OFF",
                                subtitle = "Camera LED",
                                icon = if (uiState.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                isActive = uiState.isFlashlightOn,
                                activeColor = ArcCyanPrimary,
                                modifier = Modifier.weight(1f),
                                testTag = "toggle_flashlight_btn",
                                onClick = { viewModel.toggleFlashlightManual() }
                            )

                            HardwareTile(
                                title = "Silent Mode",
                                subtitle = "Ringer Mute",
                                icon = Icons.Default.VolumeOff,
                                isActive = false,
                                activeColor = IronGold,
                                modifier = Modifier.weight(1f),
                                testTag = "sound_silent_btn",
                                onClick = { viewModel.processCommand("Silent mode", false) }
                            )

                            HardwareTile(
                                title = "Max Sound",
                                subtitle = "Full Output",
                                icon = Icons.Default.VolumeUp,
                                isActive = false,
                                activeColor = TerminalGreen,
                                modifier = Modifier.weight(1f),
                                testTag = "sound_max_btn",
                                onClick = { viewModel.deviceManager.setSoundProfile("MAX") }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HardwareTile(
                                title = "Camera",
                                subtitle = "Sensor Lens",
                                icon = Icons.Default.CameraAlt,
                                isActive = false,
                                activeColor = TextCyan,
                                modifier = Modifier.weight(1f),
                                testTag = "launch_camera_btn",
                                onClick = { viewModel.deviceManager.launchApp("CAMERA") }
                            )

                            HardwareTile(
                                title = "Phone Dialer",
                                subtitle = "Call Matrix",
                                icon = Icons.Default.Phone,
                                isActive = false,
                                activeColor = TextCyan,
                                modifier = Modifier.weight(1f),
                                testTag = "launch_dialer_btn",
                                onClick = { viewModel.deviceManager.launchApp("DIALER") }
                            )

                            HardwareTile(
                                title = "Calculator",
                                subtitle = "Quick Math",
                                icon = Icons.Default.Calculate,
                                isActive = false,
                                activeColor = TextCyan,
                                modifier = Modifier.weight(1f),
                                testTag = "launch_calc_btn",
                                onClick = { viewModel.deviceManager.launchApp("CALCULATOR") }
                            )
                        }
                    }
                }
            }

            // 2. Iron Man Preset Protocols
            item {
                HudGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = HudBorderGold,
                    glowColor = IronGold.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STARK PROTOCOL SEQUENCES",
                            style = MaterialTheme.typography.labelSmall,
                            color = IronGoldGlow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ProtocolRow(
                            title = "House Party Protocol",
                            desc = "Torch strobe pulse + 100% volume + party greeting",
                            icon = Icons.Default.FlashlightOn,
                            tagColor = AlertOrange,
                            onClick = { viewModel.executeProtocol("PARTY_PROTOCOL") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProtocolRow(
                            title = "Stealth Protocol",
                            desc = "Silent profile + deactivates all flashlights + dimmed HUD",
                            icon = Icons.Default.Shield,
                            tagColor = IronGold,
                            onClick = { viewModel.executeProtocol("STEALTH_PROTOCOL") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProtocolRow(
                            title = "Morning Briefing Protocol",
                            desc = "Speaks time, battery levels, and motivational status",
                            icon = Icons.Default.WbSunny,
                            tagColor = ArcCyanPrimary,
                            onClick = { viewModel.executeProtocol("MORNING_PROTOCOL") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProtocolRow(
                            title = "Clean Slate Protocol",
                            desc = "Resets activity logs and runs RAM diagnostic refresh",
                            icon = Icons.Default.BatteryChargingFull,
                            tagColor = TerminalGreen,
                            onClick = { viewModel.executeProtocol("CLEAN_PROTOCOL") }
                        )
                    }
                }
            }

            // 3. Custom Voice Macro Hub Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VOICE TRIGGER MACROS (NO-CODE)",
                            style = MaterialTheme.typography.labelLarge,
                            color = ArcCyanPrimary
                        )
                        Text(
                            text = "${allMacros.size} Active Routines Registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArcCyanPrimary,
                            contentColor = HudBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_macro_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Macro", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. List of Voice Macros
            items(allMacros) { macro ->
                MacroItemCard(
                    macro = macro,
                    onToggle = { viewModel.toggleMacro(macro) },
                    onDelete = { viewModel.deleteMacro(macro) },
                    onRun = { viewModel.processCommand(macro.triggerPhrase, false) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog) {
        AddMacroDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { phrase, actionType, actionParam, desc ->
                viewModel.addCustomMacro(phrase, actionType, actionParam, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HardwareTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isActive) activeColor else HudBorder,
                RoundedCornerShape(12.dp)
            )
            .testTag(testTag),
        color = if (isActive) activeColor.copy(alpha = 0.22f) else HudSurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else TextCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ProtocolRow(
    title: String,
    desc: String,
    icon: ImageVector,
    tagColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        color = HudSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tagColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "EXECUTE",
                style = MaterialTheme.typography.labelSmall,
                color = tagColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MacroItemCard(
    macro: VoiceMacro,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onRun: () -> Unit
) {
    HudGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (macro.isEnabled) HudBorder else HudBorder.copy(alpha = 0.2f),
        showTechCorners = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onRun)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\"${macro.triggerPhrase}\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (macro.isEnabled) ArcCyanGlow else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (macro.isBuiltIn) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IronGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = IronGold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = macro.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Action: [${macro.actionType}] ${if (macro.actionParam.isNotBlank()) "▸ ${macro.actionParam}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalGreen
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = macro.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyanPrimary,
                        checkedTrackColor = ArcCyanPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = HudSurfaceElevated
                    )
                )

                if (!macro.isBuiltIn) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Macro",
                            tint = IronRedGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMacroDialog(
    onDismiss: () -> Unit,
    onConfirm: (phrase: String, actionType: String, actionParam: String, desc: String) -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("FLASHLIGHT_ON") }
    var actionParam by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val actionOptions = listOf(
        "FLASHLIGHT_ON" to "Turn On Flashlight",
        "FLASHLIGHT_OFF" to "Turn Off Flashlight",
        "BATTERY_CHECK" to "Battery Diagnostics",
        "OPEN_APP" to "Launch App (Camera/Dialer/Maps)",
        "SPEAK" to "JARVIS Custom Voice Speech"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HudSurfaceVariant,
        titleContentColor = ArcCyanPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(text = "CREATE VOICE TRIGGER MACRO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Voice Phrase (Trigger):", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                OutlinedTextField(
                    value = phrase,
                    onValueChange = { phrase = it },
                    placeholder = { Text("e.g. 'Party time' or 'Padhai shuru'", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ArcCyanPrimary,
                        unfocusedBorderColor = HudBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Hardware Action:", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                actionOptions.forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actionType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (actionType == type) ArcCyanPrimary else HudBorder,
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (actionType == type) ArcCyanGlow else TextSecondary
                        )
                    }
                }

                if (actionType == "OPEN_APP" || actionType == "SPEAK") {
                    Text(
                        text = if (actionType == "OPEN_APP") "App Name (CAMERA, DIALER, MAPS, CALCULATOR):" else "Text for JARVIS to Speak:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCyan
                    )
                    OutlinedTextField(
                        value = actionParam,
                        onValueChange = { actionParam = it },
                        placeholder = { Text(if (actionType == "OPEN_APP") "CAMERA" else "Yes sir, protocol initiated.", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ArcCyanPrimary,
                            unfocusedBorderColor = HudBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(text = "Routine Description:", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Short description of routine", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ArcCyanPrimary,
                        unfocusedBorderColor = HudBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phrase.isNotBlank()) {
                        val desc = if (description.isNotBlank()) description else "Custom user voice macro for '$phrase'"
                        onConfirm(phrase, actionType, actionParam, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcCyanPrimary, contentColor = HudBackground)
            ) {
                Text("Save Macro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
