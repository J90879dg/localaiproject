package com.localaiproject.shared.contracts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanValueRequest(
    @SerialName("imageLabels") val imageLabels: List<String> = emptyList(),
    @SerialName("typedHint") val typedHint: String = "",
    @SerialName("includeSecondHand") val includeSecondHand: Boolean = true,
    val region: String = "us"
)

@Serializable
data class IdentifiedItem(
    @SerialName("canonicalName") val canonicalName: String,
    val confidence: Double,
    @SerialName("matchedAliases") val matchedAliases: List<String>
)

@Serializable
data class ValueEstimate(
    @SerialName("itemName") val itemName: String,
    val region: String,
    val currency: String,
    @SerialName("lowEstimate") val lowEstimate: Double,
    @SerialName("midEstimate") val midEstimate: Double,
    @SerialName("highEstimate") val highEstimate: Double,
    @SerialName("dataTimestampUnix") val dataTimestampUnix: Long,
    val source: String
)

@Serializable
data class ScanValueResponse(
    @SerialName("identifiedItem") val identifiedItem: IdentifiedItem?,
    val valuation: ValueEstimate?,
    val message: String
)

@Serializable
data class ProjectPlanRequest(
    val goal: String
)

@Serializable
data class ProjectPlanResponse(
    val goal: String,
    @SerialName("recommendedItems") val recommendedItems: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val cautions: List<String> = emptyList()
)

@Serializable
data class ExperimentRequestPayload(
    @SerialName("userGoal") val userGoal: String,
    val domain: String,
    val reactants: List<String> = emptyList(),
    @SerialName("chemicalName") val chemicalName: String? = null,
    @SerialName("imageLabels") val imageLabels: List<String> = emptyList(),
    val conditions: Map<String, Double> = emptyMap()
)

@Serializable
data class ExperimentResponsePayload(
    val domain: String,
    val summary: String,
    val outputs: Map<String, String> = emptyMap(),
    val assumptions: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

@Serializable
data class DailyRefreshRequest(
    val force: Boolean = false
)

@Serializable
data class DailyRefreshResponse(
    val refreshed: Boolean,
    val message: String,
    @SerialName("timestampUnix") val timestampUnix: Long? = null
)
