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

        private val _distractionTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val distractionTableChangedFlow: SharedFlow<Unit> = _distractionTableChangedFlow.asSharedFlow()

        fun getInstance(context: Context): FocusSessionDatabase {
            return instance ?: synchronized(this) {
                instance ?: FocusSessionDatabase(context).also { instance = it }
            }
        }

        fun notifyTableChanged() {
            _sessionTableChangedFlow.tryEmit(Unit)
        }

        fun notifyDistractionTableChanged() {
            _distractionTableChangedFlow.tryEmit(Unit)
        }
    }

    val sessionTableChangedFlow: SharedFlow<Unit>
        get() = FocusSessionDatabase.sessionTableChangedFlow

    val distractionTableChangedFlow: SharedFlow<Unit>
        get() = FocusSessionDatabase.distractionTableChangedFlow

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
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_user_id ON $TABLE_FOCUS_SESSIONS($COLUMN_USER_ID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_created_at ON $TABLE_FOCUS_SESSIONS($COLUMN_CREATED_AT)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS distraction_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                reason TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'Other',
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_user_id ON distraction_logs(user_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOCUS_SESSIONS")
        db.execSQL("DROP TABLE IF EXISTS distraction_logs")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("ALTER TABLE $TABLE_FOCUS_SESSIONS ADD COLUMN $COLUMN_SESSION_UUID TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_focus_session_uuid ON $TABLE_FOCUS_SESSIONS($COLUMN_SESSION_UUID)")
        } catch (_: Exception) {}
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_user_id ON $TABLE_FOCUS_SESSIONS($COLUMN_USER_ID)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_created_at ON $TABLE_FOCUS_SESSIONS($COLUMN_CREATED_AT)")
        } catch (_: Exception) {}
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS distraction_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    reason TEXT NOT NULL,
                    category TEXT NOT NULL DEFAULT 'Other',
                    duration_seconds INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_user_id ON distraction_logs(user_id)")
        } catch (_: Exception) {}
    }

    

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

    

    fun getRecentSessions(userId: Int, limit: Int = 50): List<FocusSessionRecord> {
        val list = mutableListOf<FocusSessionRecord>()
        val query = """
            SELECT $COLUMN_ID, $COLUMN_USER_ID, $COLUMN_TASK_ID, $COLUMN_DURATION_MINUTES,
                   $COLUMN_SESSION_TYPE, $COLUMN_STATUS, $COLUMN_START_TIME, $COLUMN_END_TIME,
                   $COLUMN_TASK_TITLE, $COLUMN_TASK_CATEGORY
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ?
            GROUP BY $COLUMN_START_TIME, $COLUMN_DURATION_MINUTES
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

    

    fun getTotalFocusMinutes(userId: Int): Int {
        val query = "SELECT COALESCE(SUM($COLUMN_DURATION_MINUTES), 0) FROM $TABLE_FOCUS_SESSIONS WHERE $COLUMN_USER_ID = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    

    fun getMonthStartTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    

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

    

    fun getDailyMinutesPast7Days(userId: Int): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

    

    fun getTodaySessions(userId: Int): List<FocusSessionRecord> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val list = mutableListOf<FocusSessionRecord>()
        val query = """
            SELECT $COLUMN_ID, $COLUMN_USER_ID, $COLUMN_TASK_ID, $COLUMN_DURATION_MINUTES,
                   $COLUMN_SESSION_TYPE, $COLUMN_STATUS, $COLUMN_START_TIME, $COLUMN_END_TIME,
                   $COLUMN_TASK_TITLE, $COLUMN_TASK_CATEGORY
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ? AND (substr($COLUMN_START_TIME, 1, 10) = ? OR substr($COLUMN_END_TIME, 1, 10) = ?)
            ORDER BY $COLUMN_ID ASC
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), todayStr, todayStr))
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

    

    fun getMonthWeeklyMinutes(userId: Int): List<Int> {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val monthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)

        val weekMinutes = IntArray(4)

        val query = """
            SELECT substr(COALESCE($COLUMN_START_TIME, $COLUMN_END_TIME), 9, 2) as day_num, $COLUMN_DURATION_MINUTES
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ?
              AND substr(COALESCE($COLUMN_START_TIME, $COLUMN_END_TIME), 1, 7) = ?
              AND $COLUMN_STATUS = 'completed'
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), monthPrefix))
        cursor.use { c ->
            while (c.moveToNext()) {
                val dayStr = c.getString(0) ?: continue
                val day = dayStr.toIntOrNull() ?: continue
                val mins = c.getInt(1)
                val weekIndex = when {
                    day <= 7 -> 0
                    day <= 14 -> 1
                    day <= 21 -> 2
                    else -> 3
                }
                weekMinutes[weekIndex] += mins
            }
        }
        return weekMinutes.toList()
    }

    

    fun verifyUserSessionIsolation(userId: Int): Boolean {
        val userSessions = getRecentSessions(userId, 100)
        return userSessions.all { it.userId == userId }
    }

    

    fun clearUserData(userId: Int) {
        writableDatabase.delete(TABLE_FOCUS_SESSIONS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        notifyTableChanged()
    }

    fun insertDistraction(
        userId: Int,
        reason: String,
        category: String = "Other",
        durationSeconds: Int = 0
    ): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val nowStr = sdf.format(Date())
        val values = ContentValues().apply {
            put("user_id", userId)
            put("reason", reason)
            put("category", category)
            put("duration_seconds", durationSeconds)
            put("created_at", nowStr)
        }
        val rowId = writableDatabase.insert("distraction_logs", null, values)
        notifyDistractionTableChanged()
        return rowId
    }

    fun getDistractions(userId: Int, limit: Int = 50): List<com.simats.focusflow.data.models.DistractionLogItem> {
        val list = mutableListOf<com.simats.focusflow.data.models.DistractionLogItem>()
        val query = """
            SELECT id, user_id, reason, category, duration_seconds, created_at
            FROM distraction_logs
            WHERE user_id = ?
            ORDER BY id DESC
            LIMIT ?
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), limit.toString()))
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val uidIdx = c.getColumnIndexOrThrow("user_id")
            val rIdx = c.getColumnIndexOrThrow("reason")
            val catIdx = c.getColumnIndexOrThrow("category")
            val durIdx = c.getColumnIndexOrThrow("duration_seconds")
            val caIdx = c.getColumnIndexOrThrow("created_at")

            while (c.moveToNext()) {
                list.add(
                    com.simats.focusflow.data.models.DistractionLogItem(
                        id = c.getInt(idIdx),
                        userId = c.getInt(uidIdx),
                        reason = c.getString(rIdx) ?: "",
                        category = c.getString(catIdx) ?: "Other",
                        durationSeconds = c.getInt(durIdx),
                        createdAt = c.getString(caIdx) ?: ""
                    )
                )
            }
        }
        return list
    }
}

