package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import kotlin.random.Random

object WaterNotificationHelper {
    private const val CHANNEL_ID = "water_tracker_notifications"
    private const val CHANNEL_NAME = "Hydration Reminders"
    private const val CHANNEL_DESC = "Keeps you logged and energized through the day"

    private val titles = listOf(
        "Time for some H2O! 💧",
        "Stay hydrated, stay sharp! 🧠",
        "Your water bottle misses you! 🥛",
        "Take a sip to crush your goals! 🎯",
        "Hydration check! 🐳",
        "Refresh your mind and body! 🌱"
    )

    private val bodies = listOf(
        "A simple glass of water can boost focus and reduce fatigue. Log it now!",
        "Every drop counts towards your health goal. Have a drink!",
        "Keep your momentum going! Tap to log your intake.",
        "Keep yourself feeling light and refreshed today. Drink up!",
        "Don't wait until you visual feeling dry. Hydration is key!"
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showHydrationNotification(context: Context) {
        createNotificationChannel(context)

        // Make intent to launch main activity when notification clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlow = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification details
        val title = titles[Random.nextInt(titles.size)]
        val body = bodies[Random.nextInt(bodies.size)]

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback standard system icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntentFlow)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(1001, builder.build())
            }
        } catch (e: SecurityException) {
            // Suppress exception if permission was dynamic-revoked
        }
    }
}
