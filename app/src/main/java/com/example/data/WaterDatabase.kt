package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Room
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 1. Water Log Entity
@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// 2. User Settings Entity
@Entity(tableName = "water_settings")
data class WaterSettings(
    @PrimaryKey val id: Int = 1,
    val weightKg: Float = 70.0f,
    val activityLevel: String = "Medium", // "Low", "Medium", "High"
    val climate: String = "Moderate",     // "Cold", "Moderate", "Hot"
    val calculatedGoalMl: Int = 2500,
    val customGoalMl: Int? = null,
    val wakeTimeMinutes: Int = 420,       // 7:00 AM
    val sleepTimeMinutes: Int = 1380,     // 11:00 PM
    val activeReminderIntervalMinutes: Int = 60,
    val notificationsEnabled: Boolean = true,
    val pauseUntilTimestamp: Long = 0L    // Timestamp of snooze expiry
) {
    val dailyGoalMl: Int
        get() = customGoalMl ?: calculatedGoalMl
}

// 3. Custom Timer Entity
@Entity(tableName = "custom_timers")
data class CustomTimer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INTERVAL" or "SPECIFIC"
    val value: Int,   // Interval minutes (e.g., 90) or minutes-from-midnight (e.g., 540 for 9:00 AM)
    val daysOfWeek: String, // Comma-separated day names or indices e.g. "Mon,Tue,Wed" or empty (all days)
    val isEnabled: Boolean = true
)

// Calculation helper for recommended daily water intake:
// Formulated on standard guidelines (weight in kg * 35ml + activity + climate factors)
fun calculateRecommendedGoal(weightKg: Float, activityLevel: String, climate: String): Int {
    val base = (weightKg * 35).toInt()
    val activityBonus = when (activityLevel) {
        "High" -> 1000
        "Medium" -> 500
        else -> 0
    }
    val climateBonus = when (climate) {
        "Hot" -> 500
        "Cold" -> -200
        else -> 0
    }
    return (base + activityBonus + climateBonus).coerceIn(1000, 6000)
}

// 4. Room DAO
@Dao
interface WaterDao {
    // Logs Queries
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun clearAllLogs()

    // Settings Queries
    @Query("SELECT * FROM water_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<WaterSettings?>

    @Query("SELECT * FROM water_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): WaterSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: WaterSettings)

    // Custom Timers Queries
    @Query("SELECT * FROM custom_timers ORDER BY id DESC")
    fun getAllCustomTimersFlow(): Flow<List<CustomTimer>>

    @Query("SELECT * FROM custom_timers")
    suspend fun getAllCustomTimersDirect(): List<CustomTimer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTimer(timer: CustomTimer)

    @Update
    suspend fun updateCustomTimer(timer: CustomTimer)

    @Query("DELETE FROM custom_timers WHERE id = :id")
    suspend fun deleteCustomTimerById(id: Long)
}

// 5. Room Database
@Database(entities = [WaterLog::class, WaterSettings::class, CustomTimer::class], version = 1, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun dao(): WaterDao

    companion object {
        @Volatile
        private var INSTANCE: WaterDatabase? = null

        fun getDatabase(context: Context): WaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterDatabase::class.java,
                    "water_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
