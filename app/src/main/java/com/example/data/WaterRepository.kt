package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WaterRepository(private val dao: WaterDao) {

    // Logs access
    val allLogs: Flow<List<WaterLog>> = dao.getAllLogsFlow()

    // Observe settings with simple default fallback if missing
    val settingsFlow: Flow<WaterSettings> = dao.getSettingsFlow().map { it ?: WaterSettings() }

    // Custom Timers
    val customTimersFlow: Flow<List<CustomTimer>> = dao.getAllCustomTimersFlow()

    suspend fun getSettingsDirect(): WaterSettings {
        return dao.getSettingsDirect() ?: WaterSettings()
    }

    suspend fun insertLog(amountMl: Int, timestamp: Long = System.currentTimeMillis()) {
        dao.insertLog(WaterLog(amountMl = amountMl, timestamp = timestamp))
    }

    suspend fun deleteLogById(id: Long) {
        dao.deleteLogById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllLogs()
    }

    suspend fun updateProfile(
        weightKg: Float,
        activityLevel: String,
        climate: String,
        customGoalMl: Int?
    ) {
        val current = getSettingsDirect()
        val computedGoal = calculateRecommendedGoal(weightKg, activityLevel, climate)
        val copy = current.copy(
            weightKg = weightKg,
            activityLevel = activityLevel,
            climate = climate,
            calculatedGoalMl = computedGoal,
            customGoalMl = customGoalMl
        )
        dao.insertOrUpdateSettings(copy)
    }

    suspend fun updateActiveHours(wakeMinutes: Int, sleepMinutes: Int) {
        val current = getSettingsDirect()
        dao.insertOrUpdateSettings(
            current.copy(
                wakeTimeMinutes = wakeMinutes,
                sleepTimeMinutes = sleepMinutes
            )
        )
    }

    suspend fun updateReminderInterval(intervalMinutes: Int) {
        val current = getSettingsDirect()
        dao.insertOrUpdateSettings(current.copy(activeReminderIntervalMinutes = intervalMinutes))
    }

    suspend fun toggleNotifications(enabled: Boolean) {
        val current = getSettingsDirect()
        dao.insertOrUpdateSettings(current.copy(notificationsEnabled = enabled))
    }

    suspend fun snoozeNotifications(hours: Int) {
        val current = getSettingsDirect()
        val snoozeTime = System.currentTimeMillis() + (hours * 60 * 60 * 1000L)
        dao.insertOrUpdateSettings(current.copy(pauseUntilTimestamp = snoozeTime))
    }

    suspend fun clearSnooze() {
        val current = getSettingsDirect()
        dao.insertOrUpdateSettings(current.copy(pauseUntilTimestamp = 0L))
    }

    suspend fun insertCustomTimer(type: String, value: Int, daysCode: String) {
        dao.insertCustomTimer(CustomTimer(type = type, value = value, daysOfWeek = daysCode))
    }

    suspend fun deleteCustomTimer(id: Long) {
        dao.deleteCustomTimerById(id)
    }

    suspend fun toggleCustomTimer(timer: CustomTimer) {
        dao.updateCustomTimer(timer.copy(isEnabled = !timer.isEnabled))
    }
}
