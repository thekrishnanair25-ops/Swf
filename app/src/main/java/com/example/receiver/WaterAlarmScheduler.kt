package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.CustomTimer
import com.example.data.WaterDatabase
import com.example.data.WaterSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object WaterAlarmScheduler {
    private const val TAG = "WaterAlarmScheduler"
    const val ACTION_ALARM = "com.example.ACTION_WATER_ALARM"

    fun rescheduleAll(context: Context, database: WaterDatabase) {
        // Launch in background since we need to read Room database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = database.dao()
                val settings = dao.getSettingsDirect() ?: WaterSettings()
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager == null) {
                    Log.e(TAG, "AlarmManager not available")
                    return@launch
                }

                // Intent pointing to AlarmReceiver
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_ALARM
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    1002,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 1. If notifications are disabled or user snoozed indefinitely (not standard but possible):
                if (!settings.notificationsEnabled) {
                    alarmManager.cancel(pendingIntent)
                    Log.d(TAG, "Notifications are disabled. Canceled future alarms.")
                    return@launch
                }

                val nowMs = System.currentTimeMillis()

                // 2. Handle Snooze / Quick Pause
                if (settings.pauseUntilTimestamp > nowMs) {
                    // Set next alarm exactly when snooze expires
                    scheduleAlarmCompat(alarmManager, settings.pauseUntilTimestamp, pendingIntent)
                    Log.d(TAG, "Snoozed! Set next alarm for when snooze ends: ${settings.pauseUntilTimestamp}")
                    return@launch
                }

                // 3. Gather timer candidates
                val candidates = mutableListOf<Long>()

                // Candidate A: Default smart interval timer
                val defIntervalMs = settings.activeReminderIntervalMinutes * 60 * 1000L
                candidates.add(nowMs + defIntervalMs)

                // Candidate B: Custom timers from Database
                val customTimers = dao.getAllCustomTimersDirect().filter { it.isEnabled }
                for (timer in customTimers) {
                    if (timer.type == "INTERVAL") {
                        candidates.add(nowMs + (timer.value * 60 * 1000L))
                    } else if (timer.type == "SPECIFIC") {
                        // value is minutes past midnight (e.g. 540 for 9:00 AM)
                        val triggerToday = getTimestampForMinutesPastMidnight(timer.value)
                        
                        // Let's find the next matching repeating day starting from today
                        val targetTime = getNextMatchingDayTimestamp(triggerToday, timer.daysOfWeek)
                        candidates.add(targetTime)
                    }
                }

                // 4. Adjust each candidate so it falls inside the Sleep/Wake window
                val adjustedCandidates = candidates.map { candidateTime ->
                    adjustTimestampToWakeWindow(candidateTime, settings)
                }.filter { it > nowMs }

                if (adjustedCandidates.isEmpty()) {
                    Log.d(TAG, "No valid future alarms. Falling back to default at wake window.")
                    // Fallback: Alarm at Wake time of tomorrow
                    val nextWake = getNextWakeTimeMs(settings.wakeTimeMinutes)
                    scheduleAlarmCompat(alarmManager, nextWake, pendingIntent)
                    return@launch
                }

                // 5. Select the earliest matching alarm
                val nextTriggerTime = adjustedCandidates.minOrNull() ?: (nowMs + defIntervalMs)
                scheduleAlarmCompat(alarmManager, nextTriggerTime, pendingIntent)
                Log.d(TAG, "Scheduled next water reminder for: $nextTriggerTime")

            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarms", e)
            }
        }
    }

    private fun scheduleAlarmCompat(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Under fallback, always schedule regular battery-optimized alarms
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun getTimestampForMinutesPastMidnight(minutes: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getNextMatchingDayTimestamp(baseTimeMs: Long, daysOfWeekCSV: String): Long {
        if (daysOfWeekCSV.isBlank()) {
            // Repeats every day
            return if (baseTimeMs > System.currentTimeMillis()) baseTimeMs else baseTimeMs + (24 * 60 * 60 * 1000L)
        }

        val enabledDays = daysOfWeekCSV.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet() // e.g. {1, 2, 3, 4, 5, 6, 7} matching Calendar.DAY_OF_WEEK

        if (enabledDays.isEmpty()) {
            return if (baseTimeMs > System.currentTimeMillis()) baseTimeMs else baseTimeMs + (24 * 60 * 60 * 1000L)
        }

        val cal = Calendar.getInstance()
        cal.timeInMillis = baseTimeMs

        // Look up to 7 days ahead for a matching day of week
        for (i in 0..7) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (enabledDays.contains(dayOfWeek) && cal.timeInMillis > System.currentTimeMillis()) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return baseTimeMs
    }

    /**
     * If the timestamp is outside the awake hours (defined by settings),
     * move it to the wake time of the next day.
     */
    private fun adjustTimestampToWakeWindow(timestamp: Long, settings: WaterSettings): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        
        val minOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val wake = settings.wakeTimeMinutes
        val sleep = settings.sleepTimeMinutes

        return if (wake < sleep) {
            if (minOfDay in wake..sleep) {
                timestamp
            } else if (minOfDay < wake) {
                // Same day, but before wake time. Move to wake time of TODAY.
                val todayWake = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    set(Calendar.HOUR_OF_DAY, wake / 60)
                    set(Calendar.MINUTE, wake % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                todayWake.timeInMillis
            } else {
                // Past sleep time. Move to wake time of TOMORROW.
                val tomorrowWake = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, wake / 60)
                    set(Calendar.MINUTE, wake % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                tomorrowWake.timeInMillis
            }
        } else {
            // Wrap-around wake hours support (e.g. night shifts: awake from 8 PM to 4 AM)
            if (minOfDay >= wake || minOfDay <= sleep) {
                timestamp
            } else {
                // Outside wake. Standard shift to wake time of TODAY or TOMORROW.
                val wakeCalendar = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    set(Calendar.HOUR_OF_DAY, wake / 60)
                    set(Calendar.MINUTE, wake % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (wakeCalendar.timeInMillis < timestamp) {
                    wakeCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                wakeCalendar.timeInMillis
            }
        }
    }

    private fun getNextWakeTimeMs(wakeMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, wakeMinutes / 60)
            set(Calendar.MINUTE, wakeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
