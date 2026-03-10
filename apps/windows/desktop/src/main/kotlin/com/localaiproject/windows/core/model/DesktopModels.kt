package com.localaiproject.windows.core.model

data class LinkedDeviceState(
    val androidConnected: Boolean = false,
    val lastHandshakeUnix: Long = 0L
)

data class AutomationTask(
    val name: String,
    val approved: Boolean,
    val steps: List<String>
)
