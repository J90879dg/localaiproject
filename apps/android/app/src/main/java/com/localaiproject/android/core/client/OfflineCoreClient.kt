package com.localaiproject.android.core.client

import com.localaiproject.shared.contracts.IdentifiedItem
import com.localaiproject.shared.contracts.ScanValueRequest
import com.localaiproject.shared.contracts.ScanValueResponse
import com.localaiproject.shared.contracts.ValueEstimate

class OfflineCoreClient {
    /**
     * Placeholder transport call.
     * Real implementation should call local Python/Kotlin core runtime.
     */
    fun scanAndValue(request: ScanValueRequest): ScanValueResponse {
        val hint = request.typedHint.lowercase()
        if (hint.contains("golf")) {
            return ScanValueResponse(
                identifiedItem = IdentifiedItem(
                    canonicalName = "golf ball",
                    confidence = 0.78,
                    matchedAliases = listOf("golf ball")
                ),
                valuation = ValueEstimate(
                    itemName = "golf ball",
                    region = request.region,
                    currency = "USD",
                    lowEstimate = 0.39,
                    midEstimate = 0.70,
                    highEstimate = 1.01,
                    dataTimestampUnix = 1773091200,
                    source = "offline_local_market_snapshot"
                ),
                message = "Offline scan + worth estimation completed."
            )
        }
        return ScanValueResponse(
            identifiedItem = null,
            valuation = null,
            message = "No confident item match. Add more image labels or a clearer typed hint."
        )
    }
}
