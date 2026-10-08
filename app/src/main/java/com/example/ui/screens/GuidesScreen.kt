package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TutorialGuide
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudBorderGold
import com.example.ui.theme.HudSurfaceElevated
import com.example.ui.theme.HudSurfaceVariant
import com.example.ui.theme.IronGold
import com.example.ui.theme.IronGoldGlow
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.JarvisViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuidesScreen(
    viewModel: JarvisViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val guides = viewModel.getFilteredGuides()

    val categories = listOf(
        "ALL" to "All Guides",
        "Setup" to "JARVIS Setup",
        "Automation" to "Automation Hub",
        "Tricks2026" to "AI Tricks 2026",
        "IronManHUD" to "Iron Man Protocols",
        "VoiceControl" to "Hindi Voice Setup"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "JARVIS KAISE BANAYE & 2026 AI TRICKS",
                style = MaterialTheme.typography.labelLarge,
                color = ArcCyanGlow
            )
            Text(
                text = "Master mobile automation, voice triggers, Iron Man HUD tuning, and secret Android AI hacks.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 1. Search Bar
        item {
            OutlinedTextField(
                value = uiState.guidesSearchQuery,
                onValueChange = { viewModel.setGuidesSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search tutorials, hacks, voice setups...",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = ArcCyanPrimary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ArcCyanPrimary,
                    unfocusedBorderColor = HudBorder,
                    focusedContainerColor = HudSurfaceVariant,
                    unfocusedContainerColor = HudSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guides_search_input")
            )
        }

        // 2. Category Chips
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catKey, catLabel) ->
                    val isSelected = uiState.selectedCategory == catKey
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setGuidesCategory(catKey) }
                            .border(
                                1.dp,
                                if (isSelected) ArcCyanPrimary else HudBorder,
                                RoundedCornerShape(16.dp)
                            ),
                        color = if (isSelected) ArcCyanPrimary.copy(alpha = 0.25f) else HudSurfaceElevated
                    ) {
                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) ArcCyanGlow else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. Guide Cards
        items(guides) { guide ->
            GuideAccordionCard(
                guide = guide,
                isHindi = uiState.language == "hi",
                onTryCommand = { command ->
                    viewModel.processCommand(command, isVoice = false)
                    viewModel.selectTab(com.example.viewmodel.NavigationTab.ARC_CORE)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GuideAccordionCard(
    guide: TutorialGuide,
    isHindi: Boolean,
    onTryCommand: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    HudGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_card_${guide.id}"),
        borderColor = if (expanded) HudBorderGold else HudBorder,
        glowColor = if (expanded) IronGold.copy(alpha = 0.12f) else ArcCyanPrimary.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ArcCyanPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "[${guide.category.uppercase()}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) guide.titleHindi else guide.titleEnglish,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) guide.summaryHindi else guide.summaryEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) IronGoldGlow else TextCyan
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    guide.steps.forEach { step ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HudSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TerminalGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IronGoldGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) step.explanationHindi else step.explanationEnglish,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                                if (step.codeSnippetOrAction != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HudBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanPrimary.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = step.codeSnippetOrAction,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextCyan,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Secret Tips box
                    if (guide.secretTips.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IronGold.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IronGold.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = IronGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PRO-TIPS & SECRET HACKS:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IronGoldGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                guide.secretTips.forEach { tip ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "• $tip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Quick Run Command Button
                    if (guide.tryCommandPrompt != null) {
                        Button(
                            onClick = { onTryCommand(guide.tryCommandPrompt) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ArcCyanPrimary,
                                contentColor = HudBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRY THIS COMMAND: \"${guide.tryCommandPrompt}\"",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
