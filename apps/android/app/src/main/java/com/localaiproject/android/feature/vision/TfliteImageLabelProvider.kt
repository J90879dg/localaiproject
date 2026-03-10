package com.localaiproject.android.feature.vision

class TfliteImageLabelProvider : ImageLabelProvider {
    override fun extractLabels(imagePath: String): List<String> {
        // TODO: bind local TFLite/ML Kit model inference.
        // Returning deterministic placeholders keeps this shell executable.
        if (imagePath.lowercase().contains("golf")) {
            return listOf("golf ball", "sports ball", "white dimpled sphere")
        }
        return emptyList()
    }
}
