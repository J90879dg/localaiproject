package com.localaiproject.shared.contracts

data class ScanValueRequest(
    val imageLabels: List<String> = emptyList(),
    val typedHint: String = "",
    val includeSecondHand: Boolean = true,
    val region: String = "us"
)

data class IdentifiedItem(
    val canonicalName: String,
    val confidence: Double,
    val matchedAliases: List<String>
)

data class ValueEstimate(
    val itemName: String,
    val region: String,
    val currency: String,
    val lowEstimate: Double,
    val midEstimate: Double,
    val highEstimate: Double,
    val dataTimestampUnix: Long,
    val source: String
)

data class ScanValueResponse(
    val identifiedItem: IdentifiedItem?,
    val valuation: ValueEstimate?,
    val message: String
)
