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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Immutable
import androidx.core.app.NotificationCompat
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.ui.viewmodel.TimerMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
 * Maintains persistent timer countdown, updates ongoing notifications,
 * calculates wall-clock accuracy, and logs sessions upon completion.
 */
class FocusTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "focusflow_timer_channel"
        const val NOTIFICATION_ID = 1001

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
                // Ignore service start if app background restrictions apply
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
        createNotificationChannel()
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

        val targetEnd = System.currentTimeMillis() + (state.remainingSeconds * 1000L)
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

        val targetEnd = System.currentTimeMillis() + (_timerStateFlow.value.remainingSeconds * 1000L)
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
        val f = focus.coerceIn(10, 90)
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
                val now = System.currentTimeMillis()
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

            // Dispatch session to local PHP backend
            serviceScope.launch {
                repository.logSession(
                    userId = current.activeUserId,
                    durationMinutes = sessionDuration,
                    sessionType = "pomodoro"
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

        updateOngoingNotification(isComplete = true)
    }

    private fun triggerCompletionAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350), -1)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 350, 150, 350), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 350, 150, 350), -1)
            }
        } catch (e: Exception) {
            // Ignore vibration errors on emulators or unsupported hardware
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusFlow Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification for Pomodoro concentration countdown"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(isComplete: Boolean = false): Notification {
        val state = _timerStateFlow.value

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isComplete -> "Interval Complete"
            state.currentMode == TimerMode.FOCUS -> "Focus Session"
            state.currentMode == TimerMode.SHORT_BREAK -> "Short Break"
            else -> "Long Break"
        }

        val contentText = if (isComplete) {
            "Interval finished! Session logged to your records."
        } else {
            "${state.formattedTime} remaining • Cycle ${state.completedSessions + 1}"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(openAppIntent)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (state.isRunning) {
            val pauseIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, FocusTimerService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
        } else if (!isComplete) {
            val resumeIntent = PendingIntent.getService(
                this,
                2,
                Intent(this, FocusTimerService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Resume", resumeIntent)
        }

        val resetIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, FocusTimerService::class.java).apply { action = ACTION_RESET },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_menu_revert, "Reset", resetIntent)

        return builder.build()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForegroundActive = true
    }

    private fun updateOngoingNotification(isComplete: Boolean = false) {
        val notification = buildNotification(isComplete)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

