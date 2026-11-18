package com.simats.focusflow.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.simats.focusflow.data.models.DistractionLogItem
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.models.TaskItem
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
        private const val DATABASE_VERSION = 2

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
        const val COLUMN_IS_SYNCED = "is_synced"

        const val TABLE_LOCAL_TASKS = "local_tasks"
        const val TABLE_DISTRACTION_LOGS = "distraction_logs"

        @Volatile
        private var instance: FocusSessionDatabase? = null

        private val _sessionTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val sessionTableChangedFlow: SharedFlow<Unit> = _sessionTableChangedFlow.asSharedFlow()

        private val _distractionTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val distractionTableChangedFlow: SharedFlow<Unit> = _distractionTableChangedFlow.asSharedFlow()

        private val _taskTableChangedFlow = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 10)
        val taskTableChangedFlow: SharedFlow<Unit> = _taskTableChangedFlow.asSharedFlow()

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

        fun notifyTaskTableChanged() {
            _taskTableChangedFlow.tryEmit(Unit)
        }
    }

    val sessionTableChangedFlow: SharedFlow<Unit>
        get() = FocusSessionDatabase.sessionTableChangedFlow

    val distractionTableChangedFlow: SharedFlow<Unit>
        get() = FocusSessionDatabase.distractionTableChangedFlow

    val taskTableChangedFlow: SharedFlow<Unit>
        get() = FocusSessionDatabase.taskTableChangedFlow

    override fun onCreate(db: SQLiteDatabase) {
        val createSessionsSql = """
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
                $COLUMN_SESSION_UUID TEXT UNIQUE,
                $COLUMN_IS_SYNCED INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(createSessionsSql)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_user_id ON $TABLE_FOCUS_SESSIONS($COLUMN_USER_ID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_created_at ON $TABLE_FOCUS_SESSIONS($COLUMN_CREATED_AT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_is_synced ON $TABLE_FOCUS_SESSIONS($COLUMN_IS_SYNCED)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DISTRACTION_LOGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                reason TEXT NOT NULL,
                category TEXT NOT NULL DEFAULT 'Other',
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 1,
                client_id TEXT UNIQUE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_user_id ON $TABLE_DISTRACTION_LOGS(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_is_synced ON $TABLE_DISTRACTION_LOGS(is_synced)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_LOCAL_TASKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_id INTEGER DEFAULT 0,
                client_id TEXT UNIQUE,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT DEFAULT '',
                category TEXT DEFAULT 'General',
                priority TEXT DEFAULT 'medium',
                due_date TEXT DEFAULT '',
                scheduled_time TEXT,
                deadline_timestamp INTEGER,
                is_completed INTEGER DEFAULT 0,
                is_synced INTEGER DEFAULT 1,
                created_at TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_local_tasks_user ON $TABLE_LOCAL_TASKS(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_local_tasks_synced ON $TABLE_LOCAL_TASKS(is_synced)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        ensureSchema(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureSchema(db)
    }

    private fun ensureSchema(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_FOCUS_SESSIONS ADD COLUMN $COLUMN_SESSION_UUID TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_focus_session_uuid ON $TABLE_FOCUS_SESSIONS($COLUMN_SESSION_UUID)")
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_FOCUS_SESSIONS ADD COLUMN $COLUMN_IS_SYNCED INTEGER DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_focus_is_synced ON $TABLE_FOCUS_SESSIONS($COLUMN_IS_SYNCED)")
        } catch (_: Exception) {}

        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_DISTRACTION_LOGS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    reason TEXT NOT NULL,
                    category TEXT NOT NULL DEFAULT 'Other',
                    duration_seconds INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL,
                    is_synced INTEGER NOT NULL DEFAULT 1,
                    client_id TEXT UNIQUE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_user_id ON $TABLE_DISTRACTION_LOGS(user_id)")
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_DISTRACTION_LOGS ADD COLUMN is_synced INTEGER DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_distraction_is_synced ON $TABLE_DISTRACTION_LOGS(is_synced)")
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_DISTRACTION_LOGS ADD COLUMN client_id TEXT")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_distraction_client_id ON $TABLE_DISTRACTION_LOGS(client_id)")
        } catch (_: Exception) {}

        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_LOCAL_TASKS (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    server_id INTEGER DEFAULT 0,
                    client_id TEXT UNIQUE,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT DEFAULT '',
                    category TEXT DEFAULT 'General',
                    priority TEXT DEFAULT 'medium',
                    due_date TEXT DEFAULT '',
                    scheduled_time TEXT,
                    deadline_timestamp INTEGER,
                    is_completed INTEGER DEFAULT 0,
                    is_synced INTEGER DEFAULT 1,
                    created_at TEXT NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_local_tasks_user ON $TABLE_LOCAL_TASKS(user_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_local_tasks_synced ON $TABLE_LOCAL_TASKS(is_synced)")
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_LOCAL_TASKS ADD COLUMN deadline_timestamp INTEGER")
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
        sessionUuid: String = java.util.UUID.randomUUID().toString(),
        isSynced: Boolean = true
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
            put(COLUMN_IS_SYNCED, if (isSynced) 1 else 0)
        }

        val rowId = writableDatabase.insertWithOnConflict(
            TABLE_FOCUS_SESSIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        notifyTableChanged()
        return rowId
    }

    fun markSessionSynced(sessionUuid: String) {
        if (sessionUuid.isBlank()) return
        val values = ContentValues().apply {
            put(COLUMN_IS_SYNCED, 1)
        }
        writableDatabase.update(TABLE_FOCUS_SESSIONS, values, "$COLUMN_SESSION_UUID = ?", arrayOf(sessionUuid))
        notifyTableChanged()
    }

    fun getUnsyncedSessions(userId: Int = 0): List<FocusSessionRecord> {
        val list = mutableListOf<FocusSessionRecord>()
        val selection = if (userId > 0) "$COLUMN_IS_SYNCED = 0 AND $COLUMN_USER_ID = ?" else "$COLUMN_IS_SYNCED = 0"
        val args = if (userId > 0) arrayOf(userId.toString()) else null
        val query = "SELECT * FROM $TABLE_FOCUS_SESSIONS WHERE $selection ORDER BY $COLUMN_ID ASC"
        val cursor = readableDatabase.rawQuery(query, args)
        cursor.use { c ->
            val idIdx = c.getColumnIndex(COLUMN_ID)
            val uidIdx = c.getColumnIndex(COLUMN_USER_ID)
            val tidIdx = c.getColumnIndex(COLUMN_TASK_ID)
            val durIdx = c.getColumnIndex(COLUMN_DURATION_MINUTES)
            val typeIdx = c.getColumnIndex(COLUMN_SESSION_TYPE)
            val statusIdx = c.getColumnIndex(COLUMN_STATUS)
            val startIdx = c.getColumnIndex(COLUMN_START_TIME)
            val endIdx = c.getColumnIndex(COLUMN_END_TIME)
            val titleIdx = c.getColumnIndex(COLUMN_TASK_TITLE)
            val catIdx = c.getColumnIndex(COLUMN_TASK_CATEGORY)
            val uuidIdx = c.getColumnIndex(COLUMN_SESSION_UUID)

            while (c.moveToNext()) {
                val tid = if (tidIdx >= 0 && !c.isNull(tidIdx)) c.getInt(tidIdx) else null
                list.add(
                    FocusSessionRecord(
                        id = if (idIdx >= 0) c.getInt(idIdx) else 0,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        taskId = tid,
                        durationMinutes = if (durIdx >= 0) c.getInt(durIdx) else 25,
                        sessionType = if (typeIdx >= 0) c.getString(typeIdx) ?: "pomodoro" else "pomodoro",
                        status = if (statusIdx >= 0) c.getString(statusIdx) ?: "completed" else "completed",
                        startedAt = if (startIdx >= 0) c.getString(startIdx) ?: "" else "",
                        completedAt = if (endIdx >= 0) c.getString(endIdx) ?: "" else "",
                        taskTitle = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else "",
                        taskCategory = if (catIdx >= 0) c.getString(catIdx) ?: "" else "",
                        sessionUuid = if (uuidIdx >= 0) c.getString(uuidIdx) ?: "" else "",
                        isSynced = false
                    )
                )
            }
        }
        return list
    }

    fun getRecentSessions(userId: Int, limit: Int = 50): List<FocusSessionRecord> {
        val list = mutableListOf<FocusSessionRecord>()
        val query = """
            SELECT $COLUMN_ID, $COLUMN_USER_ID, $COLUMN_TASK_ID, $COLUMN_DURATION_MINUTES,
                   $COLUMN_SESSION_TYPE, $COLUMN_STATUS, $COLUMN_START_TIME, $COLUMN_END_TIME,
                   $COLUMN_TASK_TITLE, $COLUMN_TASK_CATEGORY, $COLUMN_SESSION_UUID, $COLUMN_IS_SYNCED
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ?
            GROUP BY $COLUMN_START_TIME, $COLUMN_DURATION_MINUTES
            ORDER BY $COLUMN_ID DESC
            LIMIT ?
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), limit.toString()))
        cursor.use { c ->
            val idIdx = c.getColumnIndex(COLUMN_ID)
            val uidIdx = c.getColumnIndex(COLUMN_USER_ID)
            val tidIdx = c.getColumnIndex(COLUMN_TASK_ID)
            val durIdx = c.getColumnIndex(COLUMN_DURATION_MINUTES)
            val typeIdx = c.getColumnIndex(COLUMN_SESSION_TYPE)
            val statusIdx = c.getColumnIndex(COLUMN_STATUS)
            val startIdx = c.getColumnIndex(COLUMN_START_TIME)
            val endIdx = c.getColumnIndex(COLUMN_END_TIME)
            val titleIdx = c.getColumnIndex(COLUMN_TASK_TITLE)
            val catIdx = c.getColumnIndex(COLUMN_TASK_CATEGORY)
            val uuidIdx = c.getColumnIndex(COLUMN_SESSION_UUID)
            val syncIdx = c.getColumnIndex(COLUMN_IS_SYNCED)

            while (c.moveToNext()) {
                val tid = if (tidIdx >= 0 && !c.isNull(tidIdx)) c.getInt(tidIdx) else null
                val isSynced = if (syncIdx >= 0) c.getInt(syncIdx) == 1 else true
                list.add(
                    FocusSessionRecord(
                        id = if (idIdx >= 0) c.getInt(idIdx) else 0,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        taskId = tid,
                        durationMinutes = if (durIdx >= 0) c.getInt(durIdx) else 25,
                        sessionType = if (typeIdx >= 0) c.getString(typeIdx) ?: "pomodoro" else "pomodoro",
                        status = if (statusIdx >= 0) c.getString(statusIdx) ?: "completed" else "completed",
                        startedAt = if (startIdx >= 0) c.getString(startIdx) ?: "" else "",
                        completedAt = if (endIdx >= 0) c.getString(endIdx) ?: "" else "",
                        taskTitle = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else "",
                        taskCategory = if (catIdx >= 0) c.getString(catIdx) ?: "" else "",
                        sessionUuid = if (uuidIdx >= 0) c.getString(uuidIdx) ?: "" else "",
                        isSynced = isSynced
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
                   $COLUMN_TASK_TITLE, $COLUMN_TASK_CATEGORY, $COLUMN_SESSION_UUID, $COLUMN_IS_SYNCED
            FROM $TABLE_FOCUS_SESSIONS
            WHERE $COLUMN_USER_ID = ? AND (substr($COLUMN_START_TIME, 1, 10) = ? OR substr($COLUMN_END_TIME, 1, 10) = ?)
            ORDER BY $COLUMN_ID ASC
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), todayStr, todayStr))
        cursor.use { c ->
            val idIdx = c.getColumnIndex(COLUMN_ID)
            val uidIdx = c.getColumnIndex(COLUMN_USER_ID)
            val tidIdx = c.getColumnIndex(COLUMN_TASK_ID)
            val durIdx = c.getColumnIndex(COLUMN_DURATION_MINUTES)
            val typeIdx = c.getColumnIndex(COLUMN_SESSION_TYPE)
            val statusIdx = c.getColumnIndex(COLUMN_STATUS)
            val startIdx = c.getColumnIndex(COLUMN_START_TIME)
            val endIdx = c.getColumnIndex(COLUMN_END_TIME)
            val titleIdx = c.getColumnIndex(COLUMN_TASK_TITLE)
            val catIdx = c.getColumnIndex(COLUMN_TASK_CATEGORY)
            val uuidIdx = c.getColumnIndex(COLUMN_SESSION_UUID)
            val syncIdx = c.getColumnIndex(COLUMN_IS_SYNCED)

            while (c.moveToNext()) {
                val tid = if (tidIdx >= 0 && !c.isNull(tidIdx)) c.getInt(tidIdx) else null
                val isSynced = if (syncIdx >= 0) c.getInt(syncIdx) == 1 else true
                list.add(
                    FocusSessionRecord(
                        id = if (idIdx >= 0) c.getInt(idIdx) else 0,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        taskId = tid,
                        durationMinutes = if (durIdx >= 0) c.getInt(durIdx) else 25,
                        sessionType = if (typeIdx >= 0) c.getString(typeIdx) ?: "pomodoro" else "pomodoro",
                        status = if (statusIdx >= 0) c.getString(statusIdx) ?: "completed" else "completed",
                        startedAt = if (startIdx >= 0) c.getString(startIdx) ?: "" else "",
                        completedAt = if (endIdx >= 0) c.getString(endIdx) ?: "" else "",
                        taskTitle = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else "",
                        taskCategory = if (catIdx >= 0) c.getString(catIdx) ?: "" else "",
                        sessionUuid = if (uuidIdx >= 0) c.getString(uuidIdx) ?: "" else "",
                        isSynced = isSynced
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

    fun clearUserData(userId: Int) {
        writableDatabase.delete(TABLE_FOCUS_SESSIONS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        writableDatabase.delete(TABLE_LOCAL_TASKS, "user_id = ?", arrayOf(userId.toString()))
        writableDatabase.delete(TABLE_DISTRACTION_LOGS, "user_id = ?", arrayOf(userId.toString()))
        notifyTableChanged()
        notifyTaskTableChanged()
        notifyDistractionTableChanged()
    }

    fun insertDistraction(
        userId: Int,
        reason: String,
        category: String = "Other",
        durationSeconds: Int = 0,
        isSynced: Boolean = true,
        clientId: String = java.util.UUID.randomUUID().toString()
    ): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val nowStr = sdf.format(Date())
        val values = ContentValues().apply {
            put("user_id", userId)
            put("reason", reason)
            put("category", category)
            put("duration_seconds", durationSeconds)
            put("created_at", nowStr)
            put("is_synced", if (isSynced) 1 else 0)
            put("client_id", clientId)
        }
        val rowId = writableDatabase.insertWithOnConflict(
            TABLE_DISTRACTION_LOGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        notifyDistractionTableChanged()
        return rowId
    }

    fun markDistractionSynced(clientId: String) {
        if (clientId.isBlank()) return
        val cv = ContentValues().apply { put("is_synced", 1) }
        writableDatabase.update(TABLE_DISTRACTION_LOGS, cv, "client_id = ?", arrayOf(clientId))
        notifyDistractionTableChanged()
    }

    fun getUnsyncedDistractions(userId: Int = 0): List<DistractionLogItem> {
        val list = mutableListOf<DistractionLogItem>()
        val selection = if (userId > 0) "is_synced = 0 AND user_id = ?" else "is_synced = 0"
        val args = if (userId > 0) arrayOf(userId.toString()) else null
        val query = "SELECT * FROM $TABLE_DISTRACTION_LOGS WHERE $selection ORDER BY id ASC"
        val cursor = readableDatabase.rawQuery(query, args)
        cursor.use { c ->
            val idIdx = c.getColumnIndex("id")
            val uidIdx = c.getColumnIndex("user_id")
            val rIdx = c.getColumnIndex("reason")
            val catIdx = c.getColumnIndex("category")
            val durIdx = c.getColumnIndex("duration_seconds")
            val caIdx = c.getColumnIndex("created_at")
            val clientIdx = c.getColumnIndex("client_id")

            while (c.moveToNext()) {
                list.add(
                    DistractionLogItem(
                        id = if (idIdx >= 0) c.getInt(idIdx) else 0,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        reason = if (rIdx >= 0) c.getString(rIdx) ?: "" else "",
                        category = if (catIdx >= 0) c.getString(catIdx) ?: "Other" else "Other",
                        durationSeconds = if (durIdx >= 0) c.getInt(durIdx) else 0,
                        createdAt = if (caIdx >= 0) c.getString(caIdx) ?: "" else "",
                        isSynced = false,
                        clientId = if (clientIdx >= 0) c.getString(clientIdx) ?: "" else ""
                    )
                )
            }
        }
        return list
    }

    fun getDistractions(userId: Int, limit: Int = 50): List<DistractionLogItem> {
        val list = mutableListOf<DistractionLogItem>()
        val query = """
            SELECT id, user_id, reason, category, duration_seconds, created_at, is_synced, client_id
            FROM $TABLE_DISTRACTION_LOGS
            WHERE user_id = ?
            ORDER BY id DESC
            LIMIT ?
        """.trimIndent()
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString(), limit.toString()))
        cursor.use { c ->
            val idIdx = c.getColumnIndex("id")
            val uidIdx = c.getColumnIndex("user_id")
            val rIdx = c.getColumnIndex("reason")
            val catIdx = c.getColumnIndex("category")
            val durIdx = c.getColumnIndex("duration_seconds")
            val caIdx = c.getColumnIndex("created_at")
            val syncIdx = c.getColumnIndex("is_synced")
            val clientIdx = c.getColumnIndex("client_id")

            while (c.moveToNext()) {
                val isSynced = if (syncIdx >= 0) c.getInt(syncIdx) == 1 else true
                list.add(
                    DistractionLogItem(
                        id = if (idIdx >= 0) c.getInt(idIdx) else 0,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        reason = if (rIdx >= 0) c.getString(rIdx) ?: "" else "",
                        category = if (catIdx >= 0) c.getString(catIdx) ?: "Other" else "Other",
                        durationSeconds = if (durIdx >= 0) c.getInt(durIdx) else 0,
                        createdAt = if (caIdx >= 0) c.getString(caIdx) ?: "" else "",
                        isSynced = isSynced,
                        clientId = if (clientIdx >= 0) c.getString(clientIdx) ?: "" else ""
                    )
                )
            }
        }
        return list
    }

    fun insertTaskLocally(task: TaskItem, isSynced: Boolean = true): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = sdf.format(Date())
        val values = ContentValues().apply {
            put("server_id", task.id)
            put("client_id", task.clientId.ifBlank { java.util.UUID.randomUUID().toString() })
            put("user_id", task.userId)
            put("title", task.title)
            put("description", task.description)
            put("category", task.category)
            put("priority", task.priority)
            put("due_date", task.dueDate)
            put("scheduled_time", task.scheduledTime)
            if (task.deadlineTimestamp != null) {
                put("deadline_timestamp", task.deadlineTimestamp)
            }
            put("is_completed", if (task.isCompleted) 1 else 0)
            put("is_synced", if (isSynced) 1 else 0)
            put("created_at", task.createdAt ?: now)
        }
        val rowId = writableDatabase.insertWithOnConflict(
            TABLE_LOCAL_TASKS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        notifyTaskTableChanged()
        return rowId
    }

    fun updateTaskLocally(task: TaskItem, isSynced: Boolean = true) {
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("category", task.category)
            put("priority", task.priority)
            put("due_date", task.dueDate)
            put("scheduled_time", task.scheduledTime)
            if (task.deadlineTimestamp != null) {
                put("deadline_timestamp", task.deadlineTimestamp)
            } else {
                putNull("deadline_timestamp")
            }
            put("is_completed", if (task.isCompleted) 1 else 0)
            put("is_synced", if (isSynced) 1 else 0)
        }
        writableDatabase.update(
            TABLE_LOCAL_TASKS,
            values,
            "server_id = ? OR id = ? OR (client_id IS NOT NULL AND client_id != '' AND client_id = ?)",
            arrayOf(task.id.toString(), task.id.toString(), task.clientId)
        )
        notifyTaskTableChanged()
    }

    fun getLocalTasks(userId: Int): List<TaskItem> {
        val list = mutableListOf<TaskItem>()
        val query = "SELECT * FROM $TABLE_LOCAL_TASKS WHERE user_id = ? ORDER BY id DESC"
        val cursor = readableDatabase.rawQuery(query, arrayOf(userId.toString()))
        cursor.use { c ->
            val idIdx = c.getColumnIndex("id")
            val serverIdIdx = c.getColumnIndex("server_id")
            val clientIdx = c.getColumnIndex("client_id")
            val uidIdx = c.getColumnIndex("user_id")
            val titleIdx = c.getColumnIndex("title")
            val descIdx = c.getColumnIndex("description")
            val catIdx = c.getColumnIndex("category")
            val prioIdx = c.getColumnIndex("priority")
            val dueIdx = c.getColumnIndex("due_date")
            val timeIdx = c.getColumnIndex("scheduled_time")
            val dlIdx = c.getColumnIndex("deadline_timestamp")
            val compIdx = c.getColumnIndex("is_completed")
            val syncIdx = c.getColumnIndex("is_synced")
            val createdIdx = c.getColumnIndex("created_at")

            while (c.moveToNext()) {
                val serverId = if (serverIdIdx >= 0) c.getInt(serverIdIdx) else 0
                val localId = if (idIdx >= 0) c.getInt(idIdx) else 0
                val isSynced = if (syncIdx >= 0) c.getInt(syncIdx) == 1 else true
                val dl = if (dlIdx >= 0 && !c.isNull(dlIdx)) c.getLong(dlIdx) else null
                val rawDue = if (dueIdx >= 0) c.getString(dueIdx) ?: "" else ""
                val effectiveDue = if (rawDue.isNotBlank() && rawDue != "0000-00-00") {
                    rawDue
                } else if (dl != null && dl > 0) {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dl))
                } else {
                    rawDue
                }
                val rawTime = if (timeIdx >= 0) c.getString(timeIdx) else null
                val effectiveTime = if (!rawTime.isNullOrBlank()) {
                    rawTime
                } else if (dl != null && dl > 0) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dl))
                } else null
                list.add(
                    TaskItem(
                        id = if (serverId > 0) serverId else localId,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        title = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else "",
                        description = if (descIdx >= 0) c.getString(descIdx) ?: "" else "",
                        category = if (catIdx >= 0) c.getString(catIdx) ?: "General" else "General",
                        priority = if (prioIdx >= 0) c.getString(prioIdx) ?: "medium" else "medium",
                        dueDate = effectiveDue,
                        scheduledTime = effectiveTime,
                        deadlineTimestamp = dl,
                        isCompleted = if (compIdx >= 0) c.getInt(compIdx) == 1 else false,
                        createdAt = if (createdIdx >= 0) c.getString(createdIdx) else null,
                        isSynced = isSynced,
                        clientId = if (clientIdx >= 0) c.getString(clientIdx) ?: "" else ""
                    )
                )
            }
        }
        return list
    }

    fun getUnsyncedTasks(userId: Int = 0): List<TaskItem> {
        val list = mutableListOf<TaskItem>()
        val selection = if (userId > 0) "is_synced = 0 AND user_id = ?" else "is_synced = 0"
        val args = if (userId > 0) arrayOf(userId.toString()) else null
        val query = "SELECT * FROM $TABLE_LOCAL_TASKS WHERE $selection ORDER BY id ASC"
        val cursor = readableDatabase.rawQuery(query, args)
        cursor.use { c ->
            val idIdx = c.getColumnIndex("id")
            val serverIdIdx = c.getColumnIndex("server_id")
            val clientIdx = c.getColumnIndex("client_id")
            val uidIdx = c.getColumnIndex("user_id")
            val titleIdx = c.getColumnIndex("title")
            val descIdx = c.getColumnIndex("description")
            val catIdx = c.getColumnIndex("category")
            val prioIdx = c.getColumnIndex("priority")
            val dueIdx = c.getColumnIndex("due_date")
            val timeIdx = c.getColumnIndex("scheduled_time")
            val dlIdx = c.getColumnIndex("deadline_timestamp")
            val compIdx = c.getColumnIndex("is_completed")
            val createdIdx = c.getColumnIndex("created_at")

            while (c.moveToNext()) {
                val serverId = if (serverIdIdx >= 0) c.getInt(serverIdIdx) else 0
                val localId = if (idIdx >= 0) c.getInt(idIdx) else 0
                val dl = if (dlIdx >= 0 && !c.isNull(dlIdx)) c.getLong(dlIdx) else null
                val rawDue = if (dueIdx >= 0) c.getString(dueIdx) ?: "" else ""
                val effectiveDue = if (rawDue.isNotBlank() && rawDue != "0000-00-00") {
                    rawDue
                } else if (dl != null && dl > 0) {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dl))
                } else {
                    rawDue
                }
                val rawTime = if (timeIdx >= 0) c.getString(timeIdx) else null
                val effectiveTime = if (!rawTime.isNullOrBlank()) {
                    rawTime
                } else if (dl != null && dl > 0) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dl))
                } else null
                list.add(
                    TaskItem(
                        id = if (serverId > 0) serverId else localId,
                        userId = if (uidIdx >= 0) c.getInt(uidIdx) else 0,
                        title = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else "",
                        description = if (descIdx >= 0) c.getString(descIdx) ?: "" else "",
                        category = if (catIdx >= 0) c.getString(catIdx) ?: "General" else "General",
                        priority = if (prioIdx >= 0) c.getString(prioIdx) ?: "medium" else "medium",
                        dueDate = effectiveDue,
                        scheduledTime = effectiveTime,
                        deadlineTimestamp = dl,
                        isCompleted = if (compIdx >= 0) c.getInt(compIdx) == 1 else false,
                        createdAt = if (createdIdx >= 0) c.getString(createdIdx) else null,
                        isSynced = false,
                        clientId = if (clientIdx >= 0) c.getString(clientIdx) ?: "" else ""
                    )
                )
            }
        }
        return list
    }

    fun markTaskSynced(clientId: String, serverId: Int) {
        if (clientId.isBlank()) return
        val cv = ContentValues().apply {
            put("is_synced", 1)
            if (serverId > 0) put("server_id", serverId)
        }
        writableDatabase.update(TABLE_LOCAL_TASKS, cv, "client_id = ?", arrayOf(clientId))
        notifyTaskTableChanged()
    }

    fun updateTaskCompletionLocally(taskId: Int, isCompleted: Boolean) {
        val cv = ContentValues().apply {
            put("is_completed", if (isCompleted) 1 else 0)
        }
        writableDatabase.update(TABLE_LOCAL_TASKS, cv, "server_id = ? OR id = ?", arrayOf(taskId.toString(), taskId.toString()))
        notifyTaskTableChanged()
    }

    fun deleteTaskLocally(taskId: Int) {
        writableDatabase.delete(TABLE_LOCAL_TASKS, "server_id = ? OR id = ?", arrayOf(taskId.toString(), taskId.toString()))
        notifyTaskTableChanged()
    }

    fun mergeAndCacheServerTasks(userId: Int, serverTasks: List<TaskItem>): List<TaskItem> {
        val unsyncedLocal = getUnsyncedTasks(userId)
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete(TABLE_LOCAL_TASKS, "user_id = ? AND is_synced = 1", arrayOf(userId.toString()))
            for (st in serverTasks) {
                val effectiveDue = if (st.dueDate.isNotBlank() && st.dueDate != "0000-00-00") {
                    st.dueDate
                } else if (st.deadlineTimestamp != null && st.deadlineTimestamp > 0) {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(st.deadlineTimestamp))
                } else {
                    st.dueDate
                }
                val effectiveTime = if (!st.scheduledTime.isNullOrBlank()) {
                    st.scheduledTime
                } else if (st.deadlineTimestamp != null && st.deadlineTimestamp > 0) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(st.deadlineTimestamp))
                } else null

                val cv = ContentValues().apply {
                    put("server_id", st.id)
                    put("client_id", st.clientId.ifBlank { "server_${st.id}" })
                    put("user_id", userId)
                    put("title", st.title)
                    put("description", st.description)
                    put("category", st.category)
                    put("priority", st.priority)
                    put("due_date", effectiveDue)
                    put("scheduled_time", effectiveTime)
                    if (st.deadlineTimestamp != null) {
                        put("deadline_timestamp", st.deadlineTimestamp)
                    }
                    put("is_completed", if (st.isCompleted) 1 else 0)
                    put("is_synced", 1)
                    put("created_at", st.createdAt ?: "")
                }
                writableDatabase.insertWithOnConflict(TABLE_LOCAL_TASKS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
        val combined = (unsyncedLocal + serverTasks).distinctBy {
            if (!it.isSynced) {
                if (it.clientId.isNotBlank()) "client_${it.clientId}" else "local_${it.id}"
            } else {
                "server_${it.id}"
            }
        }
        return combined
    }
}
