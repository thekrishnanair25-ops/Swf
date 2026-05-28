package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.WaterDatabase

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Alarm onReceive: action = $action")
        
        val database = WaterDatabase.getDatabase(context)

        if (action == WaterAlarmScheduler.ACTION_ALARM) {
            // Display notification banner offline
            WaterNotificationHelper.showHydrationNotification(context)
        }

        // Reschedule next wake alarm seamlessly (handles BOOT_COMPLETED & repeats)
        WaterAlarmScheduler.rescheduleAll(context, database)
    }
}
