package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
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
import com.example.viewmodel.JarvisViewModel

@Composable
fun SystemMatrixScreen(
    viewModel: JarvisViewModel
) {
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val systemMemory by viewModel.systemMemory.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYSTEM MATRIX & DIAGNOSTICS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcCyanGlow
                    )
                    Text(
                        text = "Real-time telemetry and hardware sensor telemetry",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.refreshDiagnostics() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HudSurfaceElevated,
                        contentColor = ArcCyanPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("refresh_diagnostics_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // 1. Power Arc Battery Card
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (batteryInfo.level > 20) HudBorder else IronRedGlow.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricHeader(
                        title = "ARC POWER MATRIX (BATTERY)",
                        status = if (batteryInfo.isCharging) "CHARGING (ONLINE)" else "DISCHARGING",
                        icon = Icons.Default.BatteryChargingFull,
                        statusColor = if (batteryInfo.isCharging) TerminalGreen else IronGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.displayLarge,
                            color = if (batteryInfo.level > 20) ArcCyanGlow else IronRedGlow,
                            fontWeight = FontWeight.Bold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "HEALTH: ${batteryInfo.health}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TerminalGreen
                            )
                            Text(
                                text = "TEMP: ${batteryInfo.temperatureCelsius}°C  |  ${batteryInfo.voltageMv} mV",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { batteryInfo.level / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (batteryInfo.level > 20) ArcCyanPrimary else IronRedGlow,
                        trackColor = HudSurfaceElevated
                    )
                }
            }
        }

        // 2. RAM Utilization Meter
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricHeader(
                        title = "NEURAL MEMORY (RAM MONITOR)",
                        status = "LOAD ${systemMemory.usedRamPercent}%",
                        icon = Icons.Default.Memory,
                        statusColor = if (systemMemory.usedRamPercent < 80) TerminalGreen else AlertOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "AVAILABLE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${systemMemory.availRamMb} MB",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "USED", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${systemMemory.totalRamMb - systemMemory.availRamMb} MB",
                                style = MaterialTheme.typography.titleMedium,
                                color = IronGoldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TOTAL RAM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${systemMemory.totalRamMb} MB",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { systemMemory.usedRamPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (systemMemory.usedRamPercent < 80) ArcCyanPrimary else AlertOrange,
                        trackColor = HudSurfaceElevated
                    )
                }
            }
        }

        // 3. Storage Space Telemetry
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricHeader(
                        title = "DATA CORE STORAGE",
                        status = "${systemMemory.freeStorageGb} GB FREE",
                        icon = Icons.Default.SdStorage,
                        statusColor = TextCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${systemMemory.totalStorageGb - systemMemory.freeStorageGb} GB Used of ${systemMemory.totalStorageGb} GB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val usedStorageRatio = if (systemMemory.totalStorageGb > 0) {
                        ((systemMemory.totalStorageGb - systemMemory.freeStorageGb) / systemMemory.totalStorageGb).toFloat()
                    } else 0.5f

                    LinearProgressIndicator(
                        progress = { usedStorageRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = IronGold,
                        trackColor = HudSurfaceElevated
                    )
                }
            }
        }

        // 4. Hardware Unit & Architecture Specifications
        item {
            HudGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HudBorder
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetricHeader(
                        title = "DEVICE MATRIX SPECS",
                        status = "AUTHORIZED UNIT",
                        icon = Icons.Default.PhoneAndroid,
                        statusColor = TerminalGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Hardware Model:", value = systemMemory.deviceModel)
                    DetailRow(label = "Operating OS:", value = systemMemory.androidVersion)
                    DetailRow(label = "CPU Instruction ABI:", value = systemMemory.cpuAbi)
                    DetailRow(label = "AI Processing Engine:", value = "Gemini 3.5 Flash & Offline Neural")
                    DetailRow(label = "Security Protocol:", value = "Jarvis Encrypted Matrix v2.6")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MetricHeader(
    title: String,
    status: String,
    icon: ImageVector,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArcCyanPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ArcCyanGlow,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
