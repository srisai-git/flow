package com.simats.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Immutable
import androidx.core.app.NotificationCompat
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.viewmodel.TimerMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Immutable
data class TimerServiceState(
    val currentMode: TimerMode = TimerMode.FOCUS,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0,
    val targetEndTimeMillis: Long = 0L,
    val activeUserId: Int = 1,
    val toastMessage: String? = null
) {
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val formattedTime: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }

    fun getDurationMinutes(mode: TimerMode = currentMode): Int {
        return when (mode) {
            TimerMode.FOCUS -> focusMinutes
            TimerMode.SHORT_BREAK -> shortBreakMinutes
            TimerMode.LONG_BREAK -> longBreakMinutes
        }
    }
}

/**
 * Foreground Service for FocusFlow Pomodoro Timer.
 * Manages countdown, silent status updates, and session completion logging.
 */
class FocusTimerService : Service() {

    companion object {
        const val RUNNING_CHANNEL_ID = "focus_timer_clean_v4"
        const val COMPLETE_CHANNEL_ID = "focus_complete_clean_v4"
        const val NOTIFICATION_ID = 1001
        const val COMPLETE_NOTIFICATION_ID = 1002

        const val ACTION_START = "com.simats.focusflow.action.START"
        const val ACTION_PAUSE = "com.simats.focusflow.action.PAUSE"
        const val ACTION_RESUME = "com.simats.focusflow.action.RESUME"
        const val ACTION_RESET = "com.simats.focusflow.action.RESET"
        const val ACTION_SKIP = "com.simats.focusflow.action.SKIP"
        const val ACTION_SELECT_MODE = "com.simats.focusflow.action.SELECT_MODE"
        const val ACTION_SET_DURATIONS = "com.simats.focusflow.action.SET_DURATIONS"
        const val ACTION_STOP_SERVICE = "com.simats.focusflow.action.STOP_SERVICE"

        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_FOCUS_MINUTES = "extra_focus_minutes"
        const val EXTRA_SHORT_BREAK_MINUTES = "extra_short_break_minutes"
        const val EXTRA_LONG_BREAK_MINUTES = "extra_long_break_minutes"

        private val _timerStateFlow = MutableStateFlow(TimerServiceState())
        val timerStateFlow: StateFlow<TimerServiceState> = _timerStateFlow.asStateFlow()

        fun startTimer(context: Context, userId: Int) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_USER_ID, userId)
            }
            startServiceCompat(context, intent)
        }

        fun pauseTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            startServiceCompat(context, intent)
        }

        fun resumeTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_RESUME
            }
            startServiceCompat(context, intent)
        }

        fun resetTimer(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_RESET
            }
            startServiceCompat(context, intent)
        }

        fun resetService(context: Context) {
            _timerStateFlow.value = TimerServiceState()
            try {
                val intent = Intent(context, FocusTimerService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }

        fun skipSession(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_SKIP
            }
            startServiceCompat(context, intent)
        }

        fun selectMode(context: Context, mode: TimerMode) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_SELECT_MODE
                putExtra(EXTRA_MODE, mode.name)
            }
            startServiceCompat(context, intent)
        }

        fun setDurations(context: Context, focus: Int, shortBreak: Int, longBreak: Int) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_SET_DURATIONS
                putExtra(EXTRA_FOCUS_MINUTES, focus)
                putExtra(EXTRA_SHORT_BREAK_MINUTES, shortBreak)
                putExtra(EXTRA_LONG_BREAK_MINUTES, longBreak)
            }
            startServiceCompat(context, intent)
        }

        private fun startServiceCompat(context: Context, intent: Intent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore service start failures under OS background limits
            }
        }
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var tickerJob: Job? = null
    private val repository = ProductivityRepository()
    private var isForegroundActive = false

    override fun onCreate() {
        super.onCreate()
        ProductivityRepository.init(applicationContext)
        createNotificationChannels()
        serviceScope.launch(Dispatchers.IO) {
            try {
                val session = SessionManager.getInstance(applicationContext).userSessionFlow.first()
                if (session.userId != null && session.userId > 0) {
                    _timerStateFlow.update { it.copy(activeUserId = session.userId) }
                }
            } catch (e: Exception) {
                // Retain default user ID
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        val userId = intent.getIntExtra(EXTRA_USER_ID, _timerStateFlow.value.activeUserId)
        if (userId > 0 && userId != _timerStateFlow.value.activeUserId) {
            _timerStateFlow.update { it.copy(activeUserId = userId) }
        }

        when (action) {
            ACTION_START -> handleStart()
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_RESET -> handleReset()
            ACTION_SKIP -> handleSkip()
            ACTION_SELECT_MODE -> {
                val modeName = intent.getStringExtra(EXTRA_MODE)
                val mode = try {
                    TimerMode.valueOf(modeName ?: TimerMode.FOCUS.name)
                } catch (e: Exception) {
                    TimerMode.FOCUS
                }
                handleSelectMode(mode)
            }
            ACTION_SET_DURATIONS -> {
                val focus = intent.getIntExtra(EXTRA_FOCUS_MINUTES, 25)
                val shortBreak = intent.getIntExtra(EXTRA_SHORT_BREAK_MINUTES, 5)
                val longBreak = intent.getIntExtra(EXTRA_LONG_BREAK_MINUTES, 15)
                handleSetDurations(focus, shortBreak, longBreak)
            }
            ACTION_STOP_SERVICE -> handleStopService()
        }

        return START_STICKY
    }

    private fun handleStart() {
        var state = _timerStateFlow.value
        if (state.remainingSeconds <= 0) {
            val secs = state.getDurationMinutes() * 60
            _timerStateFlow.update { it.copy(totalSeconds = secs, remainingSeconds = secs) }
            state = _timerStateFlow.value
        }

        val targetEnd = SystemClock.elapsedRealtime() + (state.remainingSeconds * 1000L)
        _timerStateFlow.update {
            it.copy(isRunning = true, targetEndTimeMillis = targetEnd, toastMessage = null)
        }

        startForegroundWithNotification()
        startTickerLoop()
    }

    private fun handleResume() {
        val state = _timerStateFlow.value
        if (state.remainingSeconds <= 0) {
            val secs = state.getDurationMinutes() * 60
            _timerStateFlow.update { it.copy(totalSeconds = secs, remainingSeconds = secs) }
        }

        val targetEnd = SystemClock.elapsedRealtime() + (_timerStateFlow.value.remainingSeconds * 1000L)
        _timerStateFlow.update {
            it.copy(isRunning = true, targetEndTimeMillis = targetEnd, toastMessage = null)
        }

        startForegroundWithNotification()
        startTickerLoop()
    }

    private fun handlePause() {
        tickerJob?.cancel()
        _timerStateFlow.update { it.copy(isRunning = false) }
        updateOngoingNotification()
    }

    private fun handleReset() {
        tickerJob?.cancel()
        val current = _timerStateFlow.value
        val elapsedSeconds = current.totalSeconds - current.remainingSeconds
        val elapsedMinutes = elapsedSeconds / 60

        // If user intentionally stopped an active focus interval that ran for at least 1 min, dispatch to save_session.php
        if (current.currentMode == TimerMode.FOCUS && elapsedMinutes >= 1) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val now = Date()
            val endTime = sdf.format(now)
            val cal = Calendar.getInstance().apply {
                time = now
                add(Calendar.MINUTE, -elapsedMinutes)
            }
            val startTime = sdf.format(cal.time)
            val sessionUuid = java.util.UUID.randomUUID().toString()

            serviceScope.launch {
                repository.logSession(
                    userId = current.activeUserId,
                    durationMinutes = elapsedMinutes,
                    sessionType = "pomodoro",
                    durationSeconds = elapsedSeconds,
                    startTime = startTime,
                    endTime = endTime,
                    sessionUuid = sessionUuid
                )
            }
        }

        val secs = current.getDurationMinutes() * 60
        _timerStateFlow.update {
            it.copy(
                isRunning = false,
                totalSeconds = secs,
                remainingSeconds = secs,
                targetEndTimeMillis = 0L,
                toastMessage = "Timer reset to ${current.getDurationMinutes()}m"
            )
        }
        updateOngoingNotification()
    }

    private fun handleSkip() {
        tickerJob?.cancel()
        val current = _timerStateFlow.value
        val nextMode = when (current.currentMode) {
            TimerMode.FOCUS -> {
                if ((current.completedSessions + 1) % 4 == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
            }
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> TimerMode.FOCUS
        }
        val nextSecs = current.getDurationMinutes(nextMode) * 60
        _timerStateFlow.update {
            it.copy(
                currentMode = nextMode,
                isRunning = false,
                totalSeconds = nextSecs,
                remainingSeconds = nextSecs,
                targetEndTimeMillis = 0L,
                toastMessage = "Switched to ${nextMode.title}"
            )
        }
        updateOngoingNotification()
    }

    private fun handleSelectMode(mode: TimerMode) {
        tickerJob?.cancel()
        val secs = _timerStateFlow.value.getDurationMinutes(mode) * 60
        _timerStateFlow.update {
            it.copy(
                currentMode = mode,
                isRunning = false,
                totalSeconds = secs,
                remainingSeconds = secs,
                targetEndTimeMillis = 0L,
                toastMessage = null
            )
        }
        updateOngoingNotification()
    }

    private fun handleSetDurations(focus: Int, shortBreak: Int, longBreak: Int) {
        val f = focus.coerceIn(5, 90)
        val sb = shortBreak.coerceIn(1, 15)
        val lb = longBreak.coerceIn(10, 30)

        tickerJob?.cancel()
        _timerStateFlow.update { current ->
            val activeMins = when (current.currentMode) {
                TimerMode.FOCUS -> f
                TimerMode.SHORT_BREAK -> sb
                TimerMode.LONG_BREAK -> lb
            }
            val secs = activeMins * 60
            current.copy(
                focusMinutes = f,
                shortBreakMinutes = sb,
                longBreakMinutes = lb,
                totalSeconds = secs,
                remainingSeconds = secs,
                isRunning = false,
                toastMessage = "Durations saved: $f min focus / $sb min break"
            )
        }
        updateOngoingNotification()
    }

    private fun handleStopService() {
        tickerJob?.cancel()
        _timerStateFlow.update { it.copy(isRunning = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForegroundActive = false
        stopSelf()
    }

    private fun startTickerLoop() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive && _timerStateFlow.value.isRunning) {
                val now = SystemClock.elapsedRealtime()
                val target = _timerStateFlow.value.targetEndTimeMillis
                val remainingSecs = max(0, ((target - now + 500) / 1000).toInt())

                _timerStateFlow.update { it.copy(remainingSeconds = remainingSecs) }
                updateOngoingNotification()

                if (remainingSecs <= 0) {
                    onTimerFinished()
                    break
                }
                delay(1000)
            }
        }
    }

    private fun onTimerFinished() {
        triggerCompletionAlert()

        val current = _timerStateFlow.value
        val completedMode = current.currentMode
        val sessionDuration = current.getDurationMinutes(completedMode)

        if (completedMode == TimerMode.FOCUS) {
            val newCompletedCount = current.completedSessions + 1
            val nextMode = if (newCompletedCount % 4 == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
            val nextSecs = current.getDurationMinutes(nextMode) * 60

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val now = Date()
            val endTime = sdf.format(now)
            val cal = Calendar.getInstance()
            cal.time = now
            cal.add(Calendar.MINUTE, -sessionDuration)
            val startTime = sdf.format(cal.time)

            _timerStateFlow.update {
                it.copy(
                    isRunning = false,
                    completedSessions = newCompletedCount,
                    currentMode = nextMode,
                    totalSeconds = nextSecs,
                    remainingSeconds = nextSecs,
                    targetEndTimeMillis = 0L,
                    toastMessage = "Focus session logged ($sessionDuration min). Great job!"
                )
            }

            val sessionUuid = java.util.UUID.randomUUID().toString()
            serviceScope.launch {
                repository.logSession(
                    userId = current.activeUserId,
                    durationMinutes = sessionDuration,
                    sessionType = "pomodoro",
                    durationSeconds = sessionDuration * 60,
                    startTime = startTime,
                    endTime = endTime,
                    sessionUuid = sessionUuid
                )
            }
        } else {
            val nextSecs = current.focusMinutes * 60
            _timerStateFlow.update {
                it.copy(
                    isRunning = false,
                    currentMode = TimerMode.FOCUS,
                    totalSeconds = nextSecs,
                    remainingSeconds = nextSecs,
                    targetEndTimeMillis = 0L,
                    toastMessage = "Break completed. Ready to focus again?"
                )
            }
        }

        updateOngoingNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceJob.cancel()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val runningChannel = NotificationChannel(
                RUNNING_CHANNEL_ID,
                "Focus Timer Running",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing focus timer countdown"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }

            val completeChannel = NotificationChannel(
                COMPLETE_CHANNEL_ID,
                "Focus Timer Completed",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a focus or break session finishes"
                enableVibration(true)
            }

            manager.createNotificationChannel(runningChannel)
            manager.createNotificationChannel(completeChannel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildOngoingNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForegroundActive = true
    }

    private fun updateOngoingNotification() {
        if (!isForegroundActive) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.notify(NOTIFICATION_ID, buildOngoingNotification())
    }

    private fun buildOngoingNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val state = _timerStateFlow.value
        val title = "${state.currentMode.title}: ${state.formattedTime}"
        val contentText = if (state.isRunning) "Session in progress" else "Paused"

        val toggleActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = if (state.isRunning) ACTION_PAUSE else ACTION_RESUME
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleTitle = if (state.isRunning) "Pause" else "Resume"

        val resetActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_RESET
        }
        val resetPendingIntent = PendingIntent.getService(
            this,
            2,
            resetActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_SKIP
        }
        val skipPendingIntent = PendingIntent.getService(
            this,
            3,
            skipActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, RUNNING_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_flow) // Monochrome padded vector
            .setLargeIcon(android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888))
            .setColor(0xFF8B5CF6.toInt())                 // Violet badge tint
            .setContentIntent(pendingIntent)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, toggleTitle, togglePendingIntent)
            .addAction(0, "Reset", resetPendingIntent)
            .addAction(0, "Skip", skipPendingIntent)
            .build()
    }

    private fun triggerCompletionAlert() {
        val state = _timerStateFlow.value
        val completedTitle = "${state.currentMode.title} Completed!"
        val completedText = if (state.currentMode == TimerMode.FOCUS) {
            "Great work! Time for a break."
        } else {
            "Break finished! Ready to focus?"
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, COMPLETE_CHANNEL_ID)
            .setContentTitle(completedTitle)
            .setContentText(completedText)
            .setSmallIcon(R.drawable.ic_notification_flow)
            .setColor(0xFF8B5CF6.toInt())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(COMPLETE_NOTIFICATION_ID, notification)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(500L)
            }
        } catch (e: Exception) {
            // Ignore vibration failure
        }
    }
}