package com.localaiproject.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.localaiproject.android.feature.scan_value.ScanValueCoordinator
import com.localaiproject.android.feature.update.DailyUpdateScheduler
import com.localaiproject.android.feature.vision.TfliteImageLabelProvider

class MainActivity : ComponentActivity() {
    private val scanValueCoordinator = ScanValueCoordinator()
    private val dailyUpdateScheduler = DailyUpdateScheduler()
    private val imageLabelProvider = TfliteImageLabelProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dailyUpdateScheduler.scheduleEveryMidnight(this)

        // Placeholder bootstrap. Replace with Compose UI and navigation graph.
        val result = scanValueCoordinator.estimateWorthFromScan(
            imageLabels = imageLabelProvider.extractLabels(imagePath = "sample_golf_photo.jpg"),
            typedHint = "used golf ball"
        )
        // Keep a hard reference to avoid lint elimination in skeleton mode.
        check(result.message.isNotBlank())
    }
}
