package com.localaiproject.android.feature.vision

interface ImageLabelProvider {
    fun extractLabels(imagePath: String): List<String>
}
