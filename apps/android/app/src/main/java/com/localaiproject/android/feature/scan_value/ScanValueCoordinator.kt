package com.localaiproject.android.feature.scan_value

import com.localaiproject.android.core.client.OfflineCoreClient
import com.localaiproject.shared.contracts.ScanValueRequest
import com.localaiproject.shared.contracts.ScanValueResponse

class ScanValueCoordinator(
    private val client: OfflineCoreClient = OfflineCoreClient()
) {
    fun estimateWorthFromScan(
        imageLabels: List<String>,
        typedHint: String,
        region: String = "us"
    ): ScanValueResponse {
        val request = ScanValueRequest(
            imageLabels = imageLabels,
            typedHint = typedHint,
            includeSecondHand = true,
            region = region
        )
        return client.scanAndValue(request)
    }
}
