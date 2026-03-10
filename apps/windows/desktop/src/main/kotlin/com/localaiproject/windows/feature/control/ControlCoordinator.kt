package com.localaiproject.windows.feature.control

import com.localaiproject.windows.core.model.AutomationTask

class ControlCoordinator {
    fun buildApprovedTask(name: String, steps: List<String>): AutomationTask {
        return AutomationTask(
            name = name,
            approved = true,
            steps = steps
        )
    }
}
