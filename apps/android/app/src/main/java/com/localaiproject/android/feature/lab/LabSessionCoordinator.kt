package com.localaiproject.android.feature.lab

import com.localaiproject.android.core.model.LabSessionState

class LabSessionCoordinator {
    private var state = LabSessionState()

    fun switchMode(mode: String) {
        state = state.copy(mode = mode)
    }

    fun updateSummary(summary: String, warnings: List<String>) {
        state = state.copy(lastSummary = summary, warnings = warnings)
    }

    fun currentState(): LabSessionState = state
}
