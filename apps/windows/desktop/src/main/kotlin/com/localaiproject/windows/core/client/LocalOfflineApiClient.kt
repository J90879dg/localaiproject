package com.localaiproject.windows.core.client

import com.localaiproject.shared.contracts.ExperimentRequestPayload
import com.localaiproject.shared.contracts.ExperimentResponsePayload
import com.localaiproject.shared.contracts.ProjectPlanRequest
import com.localaiproject.shared.contracts.ProjectPlanResponse
import com.localaiproject.shared.contracts.ScanValueRequest
import com.localaiproject.shared.contracts.ScanValueResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

class LocalOfflineApiClient(
    private val baseUrl: String = "http://127.0.0.1:8765"
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun scanAndValue(request: ScanValueRequest): ScanValueResponse? {
        return postJson(
            path = "/api/scan-value",
            body = json.encodeToString(ScanValueRequest.serializer(), request)
        ) { payload ->
            json.decodeFromString(ScanValueResponse.serializer(), payload)
        }
    }

    fun planProject(goal: String): ProjectPlanResponse? {
        return postJson(
            path = "/api/project-plan",
            body = json.encodeToString(ProjectPlanRequest.serializer(), ProjectPlanRequest(goal = goal))
        ) { payload ->
            json.decodeFromString(ProjectPlanResponse.serializer(), payload)
        }
    }

    fun runExperiment(request: ExperimentRequestPayload): ExperimentResponsePayload? {
        return postJson(
            path = "/api/experiment",
            body = json.encodeToString(ExperimentRequestPayload.serializer(), request)
        ) { payload ->
            json.decodeFromString(ExperimentResponsePayload.serializer(), payload)
        }
    }

    private fun <T> postJson(path: String, body: String, decode: (String) -> T): T? {
        return try {
            val endpoint = URL("$baseUrl$path")
            val connection = endpoint.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 2500
            connection.readTimeout = 2500
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return null
            }
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            decode(responseBody)
        } catch (_: Exception) {
            null
        }
    }
}
