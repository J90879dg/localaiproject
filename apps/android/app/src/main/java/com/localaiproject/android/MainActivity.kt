package com.localaiproject.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.localaiproject.android.feature.scan_value.ScanValueCoordinator
import com.localaiproject.android.feature.update.DailyUpdateScheduler
import com.localaiproject.android.feature.vision.TfliteImageLabelProvider
import com.localaiproject.android.ui.ProfessionalDashboardScreen
import com.localaiproject.android.ui.theme.LocalAiProfessionalTheme

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
        val updateStatus = "Scheduled every day at 12:00 AM local time."

        setContent {
            LocalAiProfessionalTheme {
                ProfessionalDashboardScreen(
                    valuationMessage = result.message,
                    updateStatus = updateStatus
                )
            }
        }
    }
}
