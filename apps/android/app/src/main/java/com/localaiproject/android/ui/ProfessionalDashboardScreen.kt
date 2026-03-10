package com.localaiproject.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localaiproject.android.ui.theme.ElectricBlue
import com.localaiproject.android.ui.theme.GlassWhite
import com.localaiproject.android.ui.theme.MidnightNavy
import com.localaiproject.android.ui.theme.NeonCyan
import com.localaiproject.android.ui.theme.SoftViolet

@Composable
fun ProfessionalDashboardScreen(
    valuationMessage: String,
    updateStatus: String
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(MidnightNavy, ElectricBlue.copy(alpha = 0.45f), SoftViolet.copy(alpha = 0.35f))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ultimate Virtual Lab",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = GlassWhite
        )
        Text(
            text = "Offline AI • Scan • Simulate • Build",
            color = NeonCyan,
            fontSize = 14.sp
        )

        GlassCard(title = "Daily 12:00 AM Update", content = updateStatus)
        GlassCard(title = "Scan + Worth", content = valuationMessage)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(label = "Chem Lab", value = "Ready")
            MetricCard(label = "Physics Lab", value = "Ready")
            MetricCard(label = "Avatar", value = "Online")
        }
    }
}

@Composable
private fun GlassCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = NeonCyan, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, color = GlassWhite)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassWhite.copy(alpha = 0.14f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, color = NeonCyan, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = GlassWhite, fontWeight = FontWeight.Bold)
        }
    }
}
