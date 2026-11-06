package com.simats.focusflow.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.simats.focusflow.data.models.FocusSessionRecord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Local SQLite Database managing the focus_sessions table.
 * Acts as the offline-first single source of truth for timer completions,
 * logged sessions, and reactive profile/analytics aggregations.
 */
class FocusSessionDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "focusflow_local.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_FOCUS_SESSIONS = "focus_sessions"
        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_TASK_ID = "task_id"
        const val COLUMN_DURATION_MINUTES = "duration_minutes"
        const val COLUMN_DURATION_SECONDS = "duration_seconds"
        const val COLUMN_SESSION_TYPE = "session_type"
        const val COLUMN_STATUS = "status"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_TASK_TITLE = "task_title"
        const val COLUMN_TASK_CATEGORY = "task_category"
        const val COLUMN_SESSION_UUID = "session_uuid"

        @Volatile
        private var instance: FocusSessionDatabase? = null

        private val _sessionTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val sessionTableChangedFlow: SharedFlow<Unit> = _sessionTableChangedFlow.asSharedFlow()

        fun getInstance(context: Context): FocusSessionDatabase {
            return instance ?: synchronized(this) {
                instance ?: FocusSessionDatabase(context).also { instance = it }
            }
        }

        fun notifyTableChanged() {
            _sessionTableChangedFlow.tryEmit(Unit)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_FOCUS_SESSIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_TASK_ID INTEGER,
                $COLUMN_DURATION_MINUTES INTEGER NOT NULL,
                $COLUMN_DURATION_SECONDS INTEGER NOT NULL DEFAULT 0,
                $COLUMN_SESSION_TYPE TEXT NOT NULL DEFAULT 'pomodoro',
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'completed',
                $COLUMN_START_TIME TEXT NOT NULL,
                $COLUMN_END_TIME TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_TASK_TITLE TEXT,
                $COLUMN_TASK_CATEGORY TEXT,
                $COLUMN_SESSION_UUID TEXT UNIQUE
            )
        """.trimIndent()
        db.execSQL(createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOCUS_SESSIONS")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("ALTER TABLE $TABLE_FOCUS_SESSIONS ADD COLUMN $COLUMN_SESSION_UUID TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_focus_session_uuid ON $TABLE_FOCUS_SESSIONS($COLUMN_SESSION_UUID)")
        } catch (_: Exception) {}
    }

    /**
     * Inserts a completed focus session record and notifies reactive observers.
     */
    fun insertSession(
        userId: Int,
        durationMinutes: Int,
        durationSeconds: Int = 0,
        sessionType: String = "pomodoro",
        taskId: Int? = null,
        startTime: String? = null,
        endTime: String? = null,
        taskTitle: String? = null,
        taskCategory: String? = null,
        sessionUuid: String = java.util.UUID.randomUUID().toString()
    ): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Date()
        val end = endTime ?: sdf.format(now)
        val start = startTime ?: run {
            val cal = Calendar.getInstance()
            cal.time = now
            cal.add(Calendar.SECOND, -(durationMinutes * 60 + durationSeconds))
            sdf.format(cal.time)
        }

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_DURATION_MINUTES, durationMinutes)
            put(COLUMN_DURATION_SECONDS, durationSeconds)
            put(COLUMN_SESSION_TYPE, sessionType)
            put(COLUMN_STATUS, "completed")
            put(COLUMN_START_TIME, start)
            put(COLUMN_END_TIME, end)
            put(COLUMN_CREATED_AT, end)
            if (taskId != null) put(COLUMN_TASK_ID, taskId)
            if (!taskTitle.isNullOrBlank()) put(COLUMN_TASK_TITLE, taskTitle)
            if (!taskCategory.isNullOrBlank()) put(COLUMN_TASK_CATEGORY, taskCategory)
            put(COLUMN_SESSION_UUID, sessionUuid)
        }

        val rowId = writableDatabase.insertWithOnConflict(
            TABLE_FOCUS_SESSIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
        notifyTableChanged()
        return rowId
    }

    /**
     * Queries recent sessions sorted by newest first.
     */
    fun getRecentSessions(userId: Int, limit: Int = 50): List<FocusSessionRecord> {
        val list = mutableListOf<FocusSessionRecord>()
        val query = """
            SELECT $COLUMN_ID, $COLUMN_USER_ID, $COLUMN_TASK_ID, $COLUMN_DURATION_MINUTES,
                   $COLUMN_SESSION_TYPE, $COLUMN_STATUS, $COLUMN_START_TIME, $COLUMN_END_TIME,
                   $COLUMN_TASK_TITLE, $COLUMN_TASK_CATEGORY
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ?
            ORDER BY $COLUMN_ID DESC
            LIMIT ?
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), limit.toString()))
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow(COLUMN_ID)
            val uidIdx = c.getColumnIndexOrThrow(COLUMN_USER_ID)
            val tidIdx = c.getColumnIndexOrThrow(COLUMN_TASK_ID)
            val durIdx = c.getColumnIndexOrThrow(COLUMN_DURATION_MINUTES)
            val typeIdx = c.getColumnIndexOrThrow(COLUMN_SESSION_TYPE)
            val statusIdx = c.getColumnIndexOrThrow(COLUMN_STATUS)
            val startIdx = c.getColumnIndexOrThrow(COLUMN_START_TIME)
            val endIdx = c.getColumnIndexOrThrow(COLUMN_END_TIME)
            val titleIdx = c.getColumnIndexOrThrow(COLUMN_TASK_TITLE)
            val catIdx = c.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)

            while (c.moveToNext()) {
                val tid = if (!c.isNull(tidIdx)) c.getInt(tidIdx) else null
                list.add(
                    FocusSessionRecord(
                        id = c.getInt(idIdx),
                        userId = c.getInt(uidIdx),
                        taskId = tid,
                        durationMinutes = c.getInt(durIdx),
                        sessionType = c.getString(typeIdx) ?: "pomodoro",
                        status = c.getString(statusIdx) ?: "completed",
                        startedAt = c.getString(startIdx) ?: "",
                        completedAt = c.getString(endIdx) ?: "",
                        taskTitle = c.getString(titleIdx) ?: "",
                        taskCategory = c.getString(catIdx) ?: ""
                    )
                )
            }
        }
        return list
    }

    /**
     * Computes total focus minutes for a given user across all recorded sessions.
     */
    fun getTotalFocusMinutes(userId: Int): Int {
        val query = "SELECT COALESCE(SUM($COLUMN_DURATION_MINUTES), 0) FROM $TABLE_FOCUS_SESSIONS WHERE $COLUMN_USER_ID = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    /**
     * Computes focus minutes logged today.
     */
    fun getMonthStartTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Accurately calculates total focus minutes for the current month strictly bounded
     * between the 1st day of the month 00:00:00.000 and current time with status = 'completed'.
     */
    fun getCurrentMonthFocusMinutes(userId: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStartStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(cal.time)
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val query = """
            SELECT COALESCE(SUM($COLUMN_DURATION_MINUTES), 0)
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ?
              AND $COLUMN_START_TIME >= ?
              AND $COLUMN_START_TIME <= ?
              AND $COLUMN_STATUS = 'completed'
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), monthStartStr, nowStr))
        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun getTodayFocusMinutes(userId: Int): Int {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val query = "SELECT COALESCE(SUM($COLUMN_DURATION_MINUTES), 0) FROM $TABLE_FOCUS_SESSIONS WHERE $COLUMN_USER_ID = ? AND $COLUMN_END_TIME LIKE ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), "$todayStr%"))
        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    /**
     * Returns focus minutes for the past 7 days grouped by date (YYYY-MM-DD).
     */
    fun getDailyMinutesPast7Days(userId: Int): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Initialize 7 days with 0
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val dateKeys = mutableListOf<String>()
        for (i in 0 until 7) {
            val key = sdf.format(cal.time)
            result[key] = 0
            dateKeys.add(key)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val startDateStr = dateKeys.first()
        val query = """
            SELECT substr($COLUMN_END_TIME, 1, 10) as session_date, SUM($COLUMN_DURATION_MINUTES)
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ? AND substr($COLUMN_END_TIME, 1, 10) >= ?
            GROUP BY substr($COLUMN_END_TIME, 1, 10)
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), startDateStr))
        cursor.use { c ->
            while (c.moveToNext()) {
                val dateStr = c.getString(0) ?: continue
                val mins = c.getInt(1)
                result[dateStr] = mins
            }
        }
        return result
    }

    /**
     * Verifies that all returned sessions strictly belong to the specified user,
     * ensuring complete data isolation between authenticated user sessions.
     */
    fun verifyUserSessionIsolation(userId: Int): Boolean {
        val userSessions = getRecentSessions(userId, 100)
        return userSessions.all { it.userId == userId }
    }

    /**
     * Clears all session data for a user (used during stats reset).
     */
    fun clearUserData(userId: Int) {
        writableDatabase.delete(TABLE_FOCUS_SESSIONS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        notifyTableChanged()
    }
}

