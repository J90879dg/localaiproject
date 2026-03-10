package com.localaiproject.windows.feature.scan_value

import com.localaiproject.shared.contracts.ScanValueRequest
import com.localaiproject.shared.contracts.IdentifiedItem
import com.localaiproject.shared.contracts.ScanValueResponse
import com.localaiproject.shared.contracts.ValueEstimate
import com.localaiproject.windows.core.client.LocalOfflineApiClient

class DesktopScanValueCoordinator(
    private val apiClient: LocalOfflineApiClient = LocalOfflineApiClient()
) {
    fun estimateWorth(labels: List<String>, hint: String): ScanValueResponse {
        val request = ScanValueRequest(
            imageLabels = labels,
            typedHint = hint,
            includeSecondHand = true,
            region = "us"
        )
        val remote = apiClient.scanAndValue(request)
        if (remote != null) {
            return remote
        }

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
                    source = "offline_fallback_snapshot"
                ),
                message = "Desktop fallback used. Start local API server for live local data."
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
