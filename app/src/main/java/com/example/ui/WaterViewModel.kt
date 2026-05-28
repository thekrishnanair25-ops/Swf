package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CustomTimer
import com.example.data.WaterDatabase
import com.example.data.WaterLog
import com.example.data.WaterRepository
import com.example.data.WaterSettings
import com.example.receiver.WaterAlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WaterViewModel(
    application: Application,
    private val repository: WaterRepository
) : AndroidViewModel(application) {

    private val database = WaterDatabase.getDatabase(application)

    // Current settings
    val settingsState: StateFlow<WaterSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WaterSettings()
        )

    // Custom timers
    val customTimersState: StateFlow<List<CustomTimer>> = repository.customTimersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All logs
    val allLogsState: StateFlow<List<WaterLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived logs for TODAY only
    val todayLogsState: StateFlow<List<WaterLog>> = repository.allLogs
        .map { logs ->
            val startMs = getStartOfDayTimestamp()
            logs.filter { it.timestamp >= startMs }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Total water logged TODAY
    val totalTodayMlState: StateFlow<Int> = todayLogsState
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Action: Log hydration entry
     */
    fun logWater(amountMl: Int, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertLog(amountMl, timestamp)
            triggerReschedule()
        }
    }

    /**
     * Action: Delete accidental logged entry
     */
    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLogById(id)
            triggerReschedule()
        }
    }

    /**
     * Action: Clear whole history
     */
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            triggerReschedule()
        }
    }

    /**
     * Action: Save/Update user profile
     */
    fun saveProfile(weightKg: Float, activityLevel: String, climate: String, customGoalMl: Int?) {
        viewModelScope.launch {
            repository.updateProfile(weightKg, activityLevel, climate, customGoalMl)
            triggerReschedule()
        }
    }

    /**
     * Action: Update custom active hours
     */
    fun saveActiveHours(wakeMinutes: Int, sleepMinutes: Int) {
        viewModelScope.launch {
            repository.updateActiveHours(wakeMinutes, sleepMinutes)
            triggerReschedule()
        }
    }

    /**
     * Action: Save default smart timer intervals
     */
    fun saveReminderInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            repository.updateReminderInterval(intervalMinutes)
            triggerReschedule()
        }
    }

    /**
     * Action: Snooze notifications for hours
     */
    fun snoozeNotifications(hours: Int) {
        viewModelScope.launch {
            repository.snoozeNotifications(hours)
            triggerReschedule()
        }
    }

    /**
     * Action: Resume/Clear snooze
     */
    fun resumeNotifications() {
        viewModelScope.launch {
            repository.clearSnooze()
            triggerReschedule()
        }
    }

    /**
     * Action: Toggle notifications globally
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleNotifications(enabled)
            triggerReschedule()
        }
    }

    /**
     * Action: Add custom specific or interval alarm
     */
    fun addCustomTimer(type: String, value: Int, daysOfWeek: List<Int>) {
        viewModelScope.launch {
            val daysCode = daysOfWeek.joinToString(",")
            repository.insertCustomTimer(type, value, daysCode)
            triggerReschedule()
        }
    }

    /**
     * Action: Toggle Custom Timer state
     */
    fun toggleCustomTimer(timer: CustomTimer) {
        viewModelScope.launch {
            repository.toggleCustomTimer(timer)
            triggerReschedule()
        }
    }

    /**
     * Action: Delete Custom Timer
     */
    fun deleteCustomTimer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomTimer(id)
            triggerReschedule()
        }
    }

    private fun triggerReschedule() {
        WaterAlarmScheduler.rescheduleAll(getApplication(), database)
    }

    private fun getStartOfDayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Factory provider for simple constructor injection in standard Android
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
                val database = WaterDatabase.getDatabase(application)
                val repository = WaterRepository(database.dao())
                return WaterViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
