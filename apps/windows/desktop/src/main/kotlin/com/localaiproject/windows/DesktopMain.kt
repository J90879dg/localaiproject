package com.localaiproject.windows

import com.localaiproject.windows.core.client.LocalBridgeClient
import com.localaiproject.windows.feature.scan_value.DesktopScanValueCoordinator

fun main() {
    val bridgeClient = LocalBridgeClient()
    val state = bridgeClient.handshake(androidDeviceId = "android-1")
    check(state.androidConnected)

    val scanValue = DesktopScanValueCoordinator()
    val valueResult = scanValue.estimateWorth(
        labels = listOf("golfball", "white dimpled sphere"),
        hint = "used golf ball lot"
    )
    println(valueResult.message)
}
