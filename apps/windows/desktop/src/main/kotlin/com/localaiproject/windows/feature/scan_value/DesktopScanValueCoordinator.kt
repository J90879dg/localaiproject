package com.localaiproject.windows.feature.scan_value

import com.localaiproject.shared.contracts.IdentifiedItem
import com.localaiproject.shared.contracts.ScanValueResponse
import com.localaiproject.shared.contracts.ValueEstimate

class DesktopScanValueCoordinator {
    fun estimateWorth(labels: List<String>, hint: String): ScanValueResponse {
        val normalized = (labels + hint).joinToString(" ").lowercase()
        return if ("golf" in normalized) {
            ScanValueResponse(
                identifiedItem = IdentifiedItem(
                    canonicalName = "golf ball",
                    confidence = 0.81,
                    matchedAliases = listOf("golf ball", "titleist")
                ),
                valuation = ValueEstimate(
                    itemName = "golf ball",
                    region = "us",
                    currency = "USD",
                    lowEstimate = 0.39,
                    midEstimate = 0.70,
                    highEstimate = 1.01,
                    dataTimestampUnix = 1773091200,
                    source = "offline_local_market_snapshot"
                ),
                message = "Desktop local scan + worth completed."
            )
        } else {
            ScanValueResponse(
                identifiedItem = null,
                valuation = null,
                message = "No confident match."
            )
        }
    }
}
