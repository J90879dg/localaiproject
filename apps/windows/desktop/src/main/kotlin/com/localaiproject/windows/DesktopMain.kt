package com.localaiproject.windows

import com.localaiproject.windows.core.client.LocalBridgeClient
import com.localaiproject.windows.feature.scan_value.DesktopScanValueCoordinator
import com.localaiproject.windows.feature.update.WindowsDailyUpdateCoordinator
import com.localaiproject.windows.feature.vision.LocalVisionLabelProvider

fun main() {
    val bridgeClient = LocalBridgeClient()
    val state = bridgeClient.handshake(androidDeviceId = "android-1")
    check(state.androidConnected)

    val taskCoordinator = WindowsDailyUpdateCoordinator()
    val taskCommand = taskCoordinator.buildDailyMidnightTaskCommand(
        taskName = "LocalAIProjectDailyUpdate",
        executablePath = "C:\\\\localaiproject\\\\run_daily_update.bat"
    )
    println(taskCommand)

    val visionLabelProvider = LocalVisionLabelProvider()
    val scanValue = DesktopScanValueCoordinator()
    val valueResult = scanValue.estimateWorth(
        labels = visionLabelProvider.extractLabels(imagePath = "golf_sample_image.png"),
        hint = "used golf ball lot"
    )
    println(valueResult.message)
}
