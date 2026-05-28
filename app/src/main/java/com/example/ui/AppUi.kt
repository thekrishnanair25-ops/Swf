package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CustomTimer
import com.example.data.WaterLog
import com.example.data.WaterSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AppTab {
    HYDRATION, REMINDERS, PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerApp(viewModel: WaterViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HYDRATION) }
    
    val settings by viewModel.settingsState.collectAsState()
    val todayLogs by viewModel.todayLogsState.collectAsState()
    val totalTodayMl by viewModel.totalTodayMlState.collectAsState()
    val customTimers by viewModel.customTimersState.collectAsState()
    val allLogs by viewModel.allLogsState.collectAsState()

    val context = LocalContext.current

    // Set greeting based on local time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning! ☀️"
            in 12..16 -> "Good afternoon! 🌤️"
            else -> "Good evening! 🌙"
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.HYDRATION,
                    onClick = { selectedTab = AppTab.HYDRATION },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Hydration Home") },
                    label = { Text("Hydrate") },
                    modifier = Modifier.testTag("tab_hydration")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.REMINDERS,
                    onClick = { selectedTab = AppTab.REMINDERS },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Reminders") },
                    label = { Text("Reminders") },
                    modifier = Modifier.testTag("tab_reminders")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.PROFILE,
                    onClick = { selectedTab = AppTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile Settings") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Beautiful date format matching the mockup
            val currentDateString = remember {
                val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                formatter.format(Calendar.getInstance().time).uppercase()
            }

            // Header matching the Immersive UI layout exactly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentDateString,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "H2O Adaptive",
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // Profile Avatar linking directly to Profile settings
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        .clickable { selectedTab = AppTab.PROFILE }
                        .testTag("avatar_profile_nav"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                AppTab.HYDRATION -> HydrationTabScreen(
                    totalTodayMl = totalTodayMl,
                    settings = settings,
                    todayLogs = todayLogs,
                    allLogs = allLogs,
                    onLogWater = { viewModel.logWater(it) },
                    onDeleteLog = { viewModel.deleteLog(it) },
                    onClearHistory = { viewModel.clearHistory() },
                    onToggleNotifications = { viewModel.toggleNotifications(it) }
                )
                AppTab.REMINDERS -> RemindersTabScreen(
                    settings = settings,
                    customTimers = customTimers,
                    onToggleNotifications = { viewModel.toggleNotifications(it) },
                    onSaveReminderInterval = { viewModel.saveReminderInterval(it) },
                    onSaveActiveHours = { wake, sleep -> viewModel.saveActiveHours(wake, sleep) },
                    onSnooze = { viewModel.snoozeNotifications(it) },
                    onResume = { viewModel.resumeNotifications() },
                    onAddCustomTimer = { type, value, days -> viewModel.addCustomTimer(type, value, days) },
                    onToggleCustomTimer = { viewModel.toggleCustomTimer(it) },
                    onDeleteCustomTimer = { viewModel.deleteCustomTimer(it) }
                )
                AppTab.PROFILE -> ProfileTabScreen(
                    settings = settings,
                    onSaveProfile = { weight, act, clim, override -> viewModel.saveProfile(weight, act, clim, override) }
                )
            }
        }
    }
}

// ==========================================
// 1. HYDRATION / PROGRESS SCREEN
// ==========================================
@Composable
fun HydrationTabScreen(
    totalTodayMl: Int,
    settings: WaterSettings,
    todayLogs: List<WaterLog>,
    allLogs: List<WaterLog>,
    onLogWater: (Int) -> Unit,
    onDeleteLog: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit
) {
    var showCustomLogDialog by remember { mutableStateOf(false) }
    val progressFraction = if (settings.dailyGoalMl > 0) {
        (totalTodayMl.toFloat() / settings.dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual circular glass hydration graphic with glowing backdrop & overlay FAB
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. Radial glowing backdrop matching the Tailwind gradient
                Box(
                    modifier = Modifier
                        .size(244.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // 2. Main rounded glass circle wrapper
                Box(
                    modifier = Modifier
                        .size(208.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 6.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        animationSpec = tween(durationMillis = 1000)
                    )

                    // The wavy water container
                    WaterBottleCanvas(progress = animatedProgress)

                    // Details text inside
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", totalTodayMl.toFloat() / 1000f),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light,
                                color = if (animatedProgress > 0.45f) Color.White else MaterialTheme.colorScheme.tertiary,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "L",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (animatedProgress > 0.45f) Color.White else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )
                        }
                        
                        Text(
                            text = String.format(Locale.US, "of %.1fL Goal", settings.dailyGoalMl.toFloat() / 1000f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (animatedProgress > 0.45f) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // 3. Absolute positioned Fast Log FAB in bottom-right corner of circle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 136.dp, bottom = 4.dp)
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { showCustomLogDialog = true }
                        .testTag("add_custom_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log custom amount",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Animated target goal accomplishment description
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (totalTodayMl >= settings.dailyGoalMl) "Target reached! Spectacular job! 🎉" else "Keep taking clean sips of health!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (totalTodayMl >= settings.dailyGoalMl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick Log Presets Title & Configuration Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK LOG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // 3-Column Preset Grid matching the HTML exactly
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Column 1: Glass (250ml)
                Card(
                    onClick = { onLogWater(250) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("preset_250")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🥛", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("250ml", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Glass", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }

                // Column 2: Bottle (500ml) - Active Primary selection colors
                Card(
                    onClick = { onLogWater(500) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("preset_500")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧴", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("500ml", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Bottle", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
                    }
                }

                // Column 3: Custom entry
                Card(
                    onClick = { showCustomLogDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("custom_amount_preset")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("☕", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Custom", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Manual", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Cozy Reminder Corner Notification Card (Bottom elements)
        item {
            val nextReminderText = remember(settings) {
                val now = Calendar.getInstance()
                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                
                val wake = settings.wakeTimeMinutes
                val sleep = settings.sleepTimeMinutes
                val interval = settings.activeReminderIntervalMinutes
                
                var targetMinutes = wake
                if (nowMinutes >= wake) {
                    val elapsed = nowMinutes - wake
                    val intervalsPassed = elapsed / interval
                    targetMinutes = wake + (intervalsPassed + 1) * interval
                }
                
                if (targetMinutes > sleep) {
                    "Tomorrow at ${formatMinutes(wake)}"
                } else {
                    formatMinutes(targetMinutes)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Styled glowing bell container icon matching HTML
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = if (settings.notificationsEnabled) "Next reminder at $nextReminderText" else "Reminders are offline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Interval: Every ${settings.activeReminderIntervalMinutes} mins",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { onToggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("home_notifications_toggle")
                    )
                }
            }
        }

        // History list Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (todayLogs.isNotEmpty()) {
                    Text(
                        text = "Clear history",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onClearHistory() }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Actual Log History Container
        if (todayLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty list",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No logs yet today. Take your first dynamic drop to active logging!",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(todayLogs, key = { it.id }) { log ->
                TodayLogItem(log = log, onDelete = { onDeleteLog(log.id) })
            }
        }

        // Hydration trends / Past 7 Days analytics visual representation (Canvas Based)
        item {
            HydrationTrendsChartCard(allLogs = allLogs, dailyGoalMl = settings.dailyGoalMl)
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // Custom Log Entry Dialogue Slider Window
    if (showCustomLogDialog) {
        CustomLogDialog(
            onDismiss = { showCustomLogDialog = false },
            onConfirm = {
                onLogWater(it)
                showCustomLogDialog = false
            }
        )
    }
}

data class PresetOption(val amount: Int, val label: String, val volumeText: String)

@Composable
fun WaterBottleCanvas(progress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val bottlePath = Path().apply {
            // Draw a rounded glass container contour inside canvas
            val radius = 24.dp.toPx()
            moveTo(radius, 10f)
            lineTo(width - radius, 10f)
            quadraticBezierTo(width, 10f, width, radius)
            lineTo(width, height - radius)
            quadraticBezierTo(width, height, width - radius, height)
            lineTo(radius, height)
            quadraticBezierTo(0f, height, 0f, height - radius)
            lineTo(0f, radius)
            quadraticBezierTo(0f, 10f, radius, 10f)
            close()
        }

        // Draw bottle background outline
        drawPath(
            path = bottlePath,
            color = Color(0x330077B6) // Very light translucent background
        )

        // Wave Filling Liquid Draw
        clipPath(bottlePath) {
            val fillHeight = height * progress
            val topY = height - fillHeight

            if (fillHeight > 0f) {
                val wavePath = Path().apply {
                    moveTo(0f, topY)
                    // Create natural wave curves
                    val x1 = width * 0.25f
                    val y1 = topY - 10f
                    val x2 = width * 0.75f
                    val y2 = topY + 10f
                    lineTo(x1, y1)
                    lineTo(x2, y2)
                    lineTo(width, topY)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF48CAE4), // Ice bright light blue
                            Color(0xFF0077B6)  // Vibrant rich blue
                        ),
                        startY = topY,
                        endY = height
                    )
                )
            }
        }
    }
}

@Composable
fun TodayLogItem(log: WaterLog, onDelete: () -> Unit) {
    val timeString = remember(log.timestamp) {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        formatter.format(log.timestamp)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💧", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${log.amountMl} ml",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Logged at $timeString",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_log_${log.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete log entry",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CustomLogDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var mlValue by remember { mutableStateOf(250f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Custom Water", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Dialogue")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                // Large volume text
                Text(
                    text = "${mlValue.toInt()} ml",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Smooth precise slider
                Slider(
                    value = mlValue,
                    onValueChange = { mlValue = it },
                    valueRange = 50f..1500f,
                    steps = 57, // increments of 25ml
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_slider")
                )

                // Selection of immediate additions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(100, 200, 300, 400).forEach { add ->
                        OutlinedButton(
                            onClick = { mlValue = (mlValue + add).coerceIn(50f, 1500f) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+$add", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onConfirm(mlValue.toInt()) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("custom_confirm_button")
                ) {
                    Text("Confirm Intake", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun HydrationTrendsChartCard(allLogs: List<WaterLog>, dailyGoalMl: Int) {
    // Custom Bar Chart representing past 7 days of entries
    val daysData = remember(allLogs) {
        val list = mutableListOf<Pair<String, Int>>()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val checkCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val checkDayStr = sdf.format(checkCal.time)
            
            // Filter database logs falling on checkDay
            val daySum = allLogs.filter { log ->
                val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                logCal.get(Calendar.YEAR) == checkCal.get(Calendar.YEAR) &&
                logCal.get(Calendar.DAY_OF_YEAR) == checkCal.get(Calendar.DAY_OF_YEAR)
            }.sumOf { it.amountMl }

            list.add(Pair(checkDayStr, daySum))
        }
        list
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Last 7 Days Consistency",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                daysData.forEach { (dayLabel, amount) ->
                    val pct = if (dailyGoalMl > 0) (amount.toFloat() / dailyGoalMl.toFloat()) else 0f
                    val animatedPct by animateFloatAsState(targetValue = pct.coerceIn(0f, 1.5f), animationSpec = tween(600))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Numeric tooltip
                        if (amount > 0) {
                            Text(
                                text = "${amount}ml",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // Colored bar canvas
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(0.7f * animatedPct.coerceAtLeast(0.04f)) // capped height
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (amount >= dailyGoalMl) {
                                            listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                                        } else {
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                        }
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day label
                        Text(
                            text = dayLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. TIMERS & REMINDERS SCREEN
// ==========================================
@Composable
fun RemindersTabScreen(
    settings: WaterSettings,
    customTimers: List<CustomTimer>,
    onToggleNotifications: (Boolean) -> Unit,
    onSaveReminderInterval: (Int) -> Unit,
    onSaveActiveHours: (Int, Int) -> Unit,
    onSnooze: (Int) -> Unit,
    onResume: () -> Unit,
    onAddCustomTimer: (String, Int, List<Int>) -> Unit,
    onToggleCustomTimer: (CustomTimer) -> Unit,
    onDeleteCustomTimer: (Long) -> Unit
) {
    var showAddTimerDialog by remember { mutableStateOf(false) }

    // Derive snooze state
    val isSnoozed = remember(settings.pauseUntilTimestamp) {
        settings.pauseUntilTimestamp > System.currentTimeMillis()
    }
    
    val snoozeRemainingString = remember(settings.pauseUntilTimestamp) {
        if (settings.pauseUntilTimestamp <= System.currentTimeMillis()) ""
        else {
            val remainMs = settings.pauseUntilTimestamp - System.currentTimeMillis()
            val minutes = (remainMs / (1000 * 60)) % 60
            val hours = (remainMs / (1000 * 60 * 60))
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Enable Master Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Hydration Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Receive smart health cues to drink water", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { onToggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("notifications_toggle")
                    )
                }
            }
        }

        if (settings.notificationsEnabled) {
            // SNOOZE CONTROLS CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSnoozed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isSnoozed) "Notifications Temporarily Snoozed" else "Quick Pause Reminders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSnoozed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSnoozed) "Reminders are paused for $snoozeRemainingString" else "Mute notifications while in meetings, studies or theaters",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isSnoozed) {
                            Button(
                                onClick = onResume,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resume_notifications")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume Alarms", fontSize = 13.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 2, 3).forEach { hrs ->
                                    OutlinedButton(
                                        onClick = { onSnooze(hrs) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("snooze_${hrs}_hour")
                                    ) {
                                        Text("${hrs} hrs")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SMART INTERVAL & ACTIVE WINDOW WINDOWS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Active Working Window", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Reminders stop automatically outside these sleep/wake bounds", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        
                        Spacer(modifier = Modifier.height(14.dp))

                        // Render wake/sleep sliders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wake Up: ${formatMinutes(settings.wakeTimeMinutes)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Slider(
                                    value = settings.wakeTimeMinutes.toFloat(),
                                    onValueChange = { onSaveActiveHours(it.toInt(), settings.sleepTimeMinutes) },
                                    valueRange = 300f..720f, // 5:00 AM to 12:00 PM
                                    steps = 14,
                                    modifier = Modifier.testTag("wake_slider")
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sleep: ${formatMinutes(settings.sleepTimeMinutes)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Slider(
                                    value = settings.sleepTimeMinutes.toFloat(),
                                    onValueChange = { onSaveActiveHours(settings.wakeTimeMinutes, it.toInt()) },
                                    valueRange = 1080f..1439f, // 6:00 PM to 11:59 PM
                                    steps = 12,
                                    modifier = Modifier.testTag("sleep_slider")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Smart Base Interval: ${settings.activeReminderIntervalMinutes} minutes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Slider(
                            value = settings.activeReminderIntervalMinutes.toFloat(),
                            onValueChange = { onSaveReminderInterval(it.toInt()) },
                            valueRange = 15f..180f,
                            steps = 11, // increment of 15 minutes roughly
                            modifier = Modifier.testTag("base_interval_slider")
                        )
                    }
                }
            }

            // CUSTOM ALARMS & TIME-STAMPS LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Adaptation Alarms",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedButton(
                        onClick = { showAddTimerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_custom_alarm_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom")
                    }
                }
            }

            if (customTimers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No custom repeating alarms yet.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(customTimers, key = { it.id }) { timer ->
                    CustomTimerItem(
                        timer = timer,
                        onToggle = { onToggleCustomTimer(timer) },
                        onDelete = { onDeleteCustomTimer(timer.id) }
                    )
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Notifications Disabled", fontWeight = FontWeight.Bold)
                        Text(
                            "Please enable health notifications above to set snooze windows and configure repeating reminders.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    if (showAddTimerDialog) {
        AddTimerDialog(
            onDismiss = { showAddTimerDialog = false },
            onSave = { type, value, days ->
                onAddCustomTimer(type, value, days)
                showAddTimerDialog = false
            }
        )
    }
}

@Composable
fun CustomTimerItem(timer: CustomTimer, onToggle: () -> Unit, onDelete: () -> Unit) {
    val title = remember(timer) {
        if (timer.type == "INTERVAL") "Every ${timer.value} minutes"
        else formatMinutes(timer.value)
    }

    val daysLabel = remember(timer.daysOfWeek) {
        if (timer.daysOfWeek.isBlank()) "Everyday"
        else {
            val dayNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            timer.daysOfWeek.split(",")
                .mapNotNull { it.toIntOrNull() }
                .map { dayNames.getOrElse(it) { "" } }
                .filter { it.isNotEmpty() }
                .joinToString(", ")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_timer_item_${timer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(daysLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = timer.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_custom_timer_${timer.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_custom_timer_${timer.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete custom timer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, value: Int, days: List<Int>) -> Unit
) {
    var type by remember { mutableStateOf("INTERVAL") } // "INTERVAL" or "SPECIFIC"
    var minuteValue by remember { mutableStateOf(60) } // Default interval or timeOfDay (e.g. 540 for 9:00 AM)

    // specific times: hour and minutes
    var specificHour by remember { mutableStateOf(9) } // 9 AM
    var specificMinute by remember { mutableStateOf(0) }

    // day indexes from Calendar.DAY_OF_WEEK: Sunday=1, Monday=2 ... Saturday=7
    val selectedDays = remember { mutableStateListOf(2, 3, 4, 5, 6) } // Monday-Friday default

    val dayNames = listOf(
        Pair(2, "Mon"), Pair(3, "Tue"), Pair(4, "Wed"),
        Pair(5, "Thu"), Pair(6, "Fri"), Pair(7, "Sat"), Pair(1, "Sun")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Adaptation Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }

                // Type selector
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { type = "INTERVAL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "INTERVAL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = if (type == "INTERVAL") Color.White else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Interval")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { type = "SPECIFIC" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "SPECIFIC") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = if (type == "SPECIFIC") Color.White else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Specific Time")
                        }
                    }
                }

                // Timer value controls
                item {
                    if (type == "INTERVAL") {
                        Column {
                            Text("Interval length: $minuteValue minutes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = minuteValue.toFloat(),
                                onValueChange = { minuteValue = it.toInt() },
                                valueRange = 15f..240f,
                                steps = 15,
                                modifier = Modifier.testTag("new_timer_interval_slider")
                            )
                        }
                    } else {
                        Column {
                            Text("Alarm Time: ${formatTimeValues(specificHour, specificMinute)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Simple interactive grid dials
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                                    Text("Hour", fontSize = 11.sp)
                                    Slider(
                                        value = specificHour.toFloat(),
                                        onValueChange = { specificHour = it.toInt() },
                                        valueRange = 0f..23f,
                                        steps = 23
                                    )
                                }
                                Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                                    Text("Minute", fontSize = 11.sp)
                                    Slider(
                                        value = specificMinute.toFloat(),
                                        onValueChange = { specificMinute = it.toInt() },
                                        valueRange = 0f..59f,
                                        steps = 59
                                    )
                                }
                            }
                        }
                    }
                }

                // Days of week Selector
                item {
                    Column {
                        Text("Active Days of Week:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNames.forEach { (calCode, abb) ->
                                val selected = selectedDays.contains(calCode)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                        .clickable {
                                            if (selected) selectedDays.remove(calCode) else selectedDays.add(calCode)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = abb,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Save button
                item {
                    Button(
                        onClick = {
                            val computedValue = if (type == "INTERVAL") minuteValue else (specificHour * 60 + specificMinute)
                            onSave(type, computedValue, selectedDays.toList())
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_custom_timer")
                    ) {
                        Text("Save adaptation timer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. PROFILE & CALCULATIONS SCREEN
// ==========================================
@Composable
fun ProfileTabScreen(
    settings: WaterSettings,
    onSaveProfile: (weight: Float, activity: String, climate: String, customGoal: Int?) -> Unit
) {
    var weightInput by remember { mutableStateOf(settings.weightKg) }
    var activityLevel by remember { mutableStateOf(settings.activityLevel) }
    var climate by remember { mutableStateOf(settings.climate) }
    
    // Override logic
    var overrideEnabled by remember { mutableStateOf(settings.customGoalMl != null) }
    var customGoalInput by remember { mutableStateOf(settings.customGoalMl?.toString() ?: "3000") }

    // Re-trigger Save whenever user interacts with elements to deliver friction-free auto-save settings!
    LaunchedEffect(weightInput, activityLevel, climate, overrideEnabled, customGoalInput) {
        val overrideVal = if (overrideEnabled) customGoalInput.toIntOrNull()?.coerceIn(500, 10000) else null
        onSaveProfile(weightInput, activityLevel, climate, overrideVal)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GUIDELINES & FORMULATION BANNER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Intake Goal Formulation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "We determine your recommended water guidelines basing on clear clinical values: 35ml per kg base body weight, adjusted dynamically for physical sports output and environmental climate stress.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }

        // PHYSICAL PROFILE INPUT CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Baseline Characteristics", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Weight Picker slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Body weight", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${weightInput.toInt()} kg", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            valueRange = 40f..150f,
                            steps = 110,
                            modifier = Modifier.testTag("profile_weight_slider")
                        )
                    }

                    // Activity Level Selectors
                    Column {
                        Text("Sports & Activity volume:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Low", "Medium", "High").forEach { level ->
                                val selected = activityLevel == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                        .clickable { activityLevel = level }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Climate Selectors
                    Column {
                        Text("General Climate:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Cold", "Moderate", "Hot").forEach { temp ->
                                val selected = climate == temp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                        .clickable { climate = temp }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = temp,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GOAL & MANUAL VALUE OVERRIDE CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Intake Goal Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Display computed guidelines
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calculated Daily Target", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("${settings.calculatedGoalMl} ml", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Manual override option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manual Target Override", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Set your own specific objective goal amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = overrideEnabled,
                            onCheckedChange = { overrideEnabled = it },
                            modifier = Modifier.testTag("manual_override_toggle")
                        )
                    }

                    if (overrideEnabled) {
                        OutlinedTextField(
                            value = customGoalInput,
                            onValueChange = { customGoalInput = it },
                            label = { Text("Custom Intake Goal (ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_goal_input")
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}


// ==========================================
// VALUE FORMATTING HELPERS
// ==========================================
private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    val amPm = if (h >= 12) "PM" else "AM"
    val dispH = if (h == 0) 12 else if (h > 12) h - 12 else h
    return String.format(Locale.getDefault(), "%d:%02d %s", dispH, m, amPm)
}

private fun formatTimeValues(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val dispH = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format(Locale.getDefault(), "%d:%02d %s", dispH, minute, amPm)
}
