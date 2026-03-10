package com.localaiproject.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.localaiproject.android.feature.scan_value.ScanValueCoordinator

class MainActivity : ComponentActivity() {
    private val scanValueCoordinator = ScanValueCoordinator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Placeholder bootstrap. Replace with Compose UI and navigation graph.
        val result = scanValueCoordinator.estimateWorthFromScan(
            imageLabels = listOf("white golf ball", "titleist"),
            typedHint = "used golf ball"
        )
        // Keep a hard reference to avoid lint elimination in skeleton mode.
        check(result.message.isNotBlank())
    }
}
