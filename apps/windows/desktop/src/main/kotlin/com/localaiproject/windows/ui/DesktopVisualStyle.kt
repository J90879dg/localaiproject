package com.localaiproject.windows.ui

data class DashboardPanel(
    val title: String,
    val subtitle: String
)

data class DesktopDashboardState(
    val headerTitle: String,
    val headerSubtitle: String,
    val panels: List<DashboardPanel>
)
