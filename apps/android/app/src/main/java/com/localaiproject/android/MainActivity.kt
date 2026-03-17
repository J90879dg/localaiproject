package com.localaiproject.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.localaiproject.android.feature.vibematch.ui.VibeMatchScreen
import com.localaiproject.android.ui.theme.LocalAiProfessionalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalAiProfessionalTheme {
                VibeMatchScreen()
            }
        }
    }
}
