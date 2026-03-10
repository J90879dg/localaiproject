package com.localaiproject.android.core.client

import com.localaiproject.shared.contracts.IdentifiedItem
import com.localaiproject.shared.contracts.ProjectPlanRequest
import com.localaiproject.shared.contracts.ProjectPlanResponse
import com.localaiproject.shared.contracts.ExperimentRequestPayload
import com.localaiproject.shared.contracts.ExperimentResponsePayload
import com.localaiproject.shared.contracts.ScanValueRequest
import com.localaiproject.shared.contracts.ScanValueResponse
import com.localaiproject.shared.contracts.ValueEstimate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OfflineCoreClient(
    private val baseUrl: String = "http://127.0.0.1:8765"
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Calls local offline API server and falls back to deterministic local behavior.
     */
    fun scanAndValue(request: ScanValueRequest): ScanValueResponse {
        val remoteResult = callScanValueApi(request)
        if (remoteResult != null) {
            return remoteResult
        }
        return fallbackResponse(request)
    }

    fun planProject(goal: String): ProjectPlanResponse? {
        val request = ProjectPlanRequest(goal = goal)
        return callApi(
            path = "/api/project-plan",
            requestBody = json.encodeToString(ProjectPlanRequest.serializer(), request)
        ) { body ->
            json.decodeFromString(ProjectPlanResponse.serializer(), body)
        }
    }

    fun runExperiment(request: ExperimentRequestPayload): ExperimentResponsePayload? {
        return callApi(
            path = "/api/experiment",
            requestBody = json.encodeToString(ExperimentRequestPayload.serializer(), request)
        ) { body ->
            json.decodeFromString(ExperimentResponsePayload.serializer(), body)
        }
    }

    private fun callScanValueApi(request: ScanValueRequest): ScanValueResponse? {
        val payload = json.encodeToString(ScanValueRequest.serializer(), request)
        return callApi(path = "/api/scan-value", requestBody = payload) { body ->
            json.decodeFromString(ScanValueResponse.serializer(), body)
        }
    }

    private fun <T> callApi(
        path: String,
        requestBody: String,
        decode: (String) -> T
    ): T? {
        val executor = Executors.newSingleThreadExecutor()
        return try {
            executor.submit<T?> {
                val endpoint = URL("$baseUrl$path")
                val connection = endpoint.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { output ->
                    output.write(requestBody.toByteArray(Charsets.UTF_8))
                }
                val code = connection.responseCode
                if (code !in 200..299) {
                    return@submit null
                }
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                decode(responseBody)
            }.get(2500, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            null
        } finally {
            executor.shutdownNow()
        }
    }

    private fun fallbackResponse(request: ScanValueRequest): ScanValueResponse {
        val hint = request.typedHint.lowercase()
        return if (hint.contains("golf")) {
            ScanValueResponse(
                identifiedItem = IdentifiedItem(
                    canonicalName = "golf ball",
                    confidence = 0.73,
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
                    source = "offline_fallback_snapshot"
                ),
                message = "Local fallback used. Start offline API server for live local data."
            )
        } else {
            ScanValueResponse(
                identifiedItem = null,
                valuation = null,
                message = "No confident item match. Add more image labels or a clearer typed hint."
            )
        }
    }
}
