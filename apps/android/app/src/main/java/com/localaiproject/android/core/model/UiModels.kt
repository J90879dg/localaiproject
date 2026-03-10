package com.localaiproject.android.core.model

data class AssistantMessage(
    val role: String,
    val content: String
)

data class LabSessionState(
    val mode: String = "chemistry",
    val warnings: List<String> = emptyList(),
    val lastSummary: String = ""
)
