package com.example.data.model

data class TutorialGuide(
    val id: String,
    val titleHindi: String,
    val titleEnglish: String,
    val category: String, // "Setup", "Automation", "Tricks2026", "VoiceControl", "IronManHUD"
    val summaryHindi: String,
    val summaryEnglish: String,
    val steps: List<GuideStep>,
    val tryCommandPrompt: String? = null,
    val secretTips: List<String> = emptyList()
)

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val explanationHindi: String,
    val explanationEnglish: String,
    val codeSnippetOrAction: String? = null
)
