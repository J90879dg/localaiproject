package com.localaiproject.windows.feature.vision

class LocalVisionLabelProvider {
    fun extractLabels(imagePath: String): List<String> {
        // TODO: connect local ONNX/TFLite inference backend for Windows.
        return if (imagePath.lowercase().contains("golf")) {
            listOf("golf ball", "sports ball", "dimple texture")
        } else {
            emptyList()
        }
    }
}
