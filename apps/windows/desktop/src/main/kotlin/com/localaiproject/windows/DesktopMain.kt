package com.localaiproject.windows

import com.localaiproject.shared.contracts.ExperimentRequestPayload
import com.localaiproject.windows.core.client.LocalBridgeClient
import com.localaiproject.windows.core.client.LocalOfflineApiClient
import com.localaiproject.windows.feature.scan_value.DesktopScanValueCoordinator
import com.localaiproject.windows.feature.update.WindowsDailyUpdateCoordinator
import com.localaiproject.windows.feature.update.WindowsDailyUpdateRunner
import com.localaiproject.windows.feature.vision.LocalVisionLabelProvider
import com.localaiproject.windows.ui.ProfessionalDesktopDashboard
import java.io.File

fun main() {
    val bridgeClient = LocalBridgeClient()
    val state = bridgeClient.handshake(androidDeviceId = "android-1")
    check(state.androidConnected)

    val projectRoot = File(".").absoluteFile
    val apiBaseUrl = "http://127.0.0.1:8765"
    val pythonExecutable = "python3"
    val scriptPath = File(projectRoot, "assistant_core/python/tools/run_daily_market_refresh.py").absolutePath
    val workingDirectory = File(projectRoot, "assistant_core/python").absolutePath

    val taskCoordinator = WindowsDailyUpdateCoordinator()
    val taskCommand = taskCoordinator.buildDailyMidnightTaskCommand(
        taskName = "LocalAIProjectDailyUpdate",
        pythonExecutable = pythonExecutable,
        scriptPath = scriptPath,
        workingDirectory = workingDirectory
    )
    println(taskCommand)

    val updateRunner = WindowsDailyUpdateRunner()
    val updateResult = updateRunner.runNow(
        apiBaseUrl = apiBaseUrl,
        pythonExecutable = pythonExecutable,
        scriptPath = scriptPath,
        workingDirectory = workingDirectory
    )
    println("Daily refresh success=${updateResult.success}, exit=${updateResult.exitCode}")
    println(updateResult.output)

    val visionLabelProvider = LocalVisionLabelProvider()
    val scanValue = DesktopScanValueCoordinator(LocalOfflineApiClient(apiBaseUrl))
    val valueResult = scanValue.estimateWorth(
        labels = visionLabelProvider.extractLabels(imagePath = "golf_sample_image.png"),
        hint = "used golf ball lot"
    )
    println(valueResult.message)

    val apiClient = LocalOfflineApiClient(apiBaseUrl)
    val plan = apiClient.planProject("Build a compact chemistry demo bench")
    if (plan != null) {
        println("Project plan generated with ${plan.steps.size} steps.")
    }
    val experiment = apiClient.runExperiment(
        ExperimentRequestPayload(
            userGoal = "Run safe neutralization simulation",
            domain = "chemistry",
            reactants = listOf("HCl", "NaOH"),
            conditions = mapOf("temperature_c" to 25.0)
        )
    )
    if (experiment != null) {
        println("Experiment summary: ${experiment.summary}")
    }

    val dashboard = ProfessionalDesktopDashboard()
    val visualState = dashboard.buildState(
        updateMessage = "Midnight schedule active. Last run exit=${updateResult.exitCode}",
        valuationMessage = valueResult.message
    )
    println("==== ${visualState.headerTitle} ====")
    println(visualState.headerSubtitle)
    visualState.panels.forEach { panel ->
        println("[${panel.title}] ${panel.subtitle}")
    }
}
