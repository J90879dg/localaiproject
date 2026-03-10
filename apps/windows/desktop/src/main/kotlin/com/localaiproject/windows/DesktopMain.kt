package com.localaiproject.windows

import com.localaiproject.windows.core.client.LocalBridgeClient
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
        pythonExecutable = pythonExecutable,
        scriptPath = scriptPath,
        workingDirectory = workingDirectory
    )
    println("Daily refresh success=${updateResult.success}, exit=${updateResult.exitCode}")
    println(updateResult.output)

    val visionLabelProvider = LocalVisionLabelProvider()
    val scanValue = DesktopScanValueCoordinator()
    val valueResult = scanValue.estimateWorth(
        labels = visionLabelProvider.extractLabels(imagePath = "golf_sample_image.png"),
        hint = "used golf ball lot"
    )
    println(valueResult.message)

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
