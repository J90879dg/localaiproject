package com.localaiproject.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ProfessionalColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonCyan,
    tertiary = SoftViolet,
    background = MidnightNavy,
    surface = MidnightNavy
)

@Composable
fun LocalAiProfessionalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProfessionalColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
