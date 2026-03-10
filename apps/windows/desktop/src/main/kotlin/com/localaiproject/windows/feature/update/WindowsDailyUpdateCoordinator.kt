package com.localaiproject.windows.feature.update

class WindowsDailyUpdateCoordinator {
    /**
     * Returns a schtasks command that runs every day at 00:00 local time.
     */
    fun buildDailyMidnightTaskCommand(taskName: String, executablePath: String): String {
        return "schtasks /Create /SC DAILY /ST 00:00 /TN \"$taskName\" /TR \"$executablePath\" /F"
    }
}
