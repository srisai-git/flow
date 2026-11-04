package com.simats.focusflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.service.NotificationHelper
import com.simats.focusflow.service.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.FocusFlowBottomBar
import com.simats.focusflow.ui.navigation.Screen
import com.simats.focusflow.ui.theme.FOCUSFLOWTheme
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.screens.AboutFlowScreen
import com.simats.focusflow.ui.screens.AccountRecoveryScreen
import com.simats.focusflow.ui.screens.ActivitiesScreen
import com.simats.focusflow.ui.screens.ActiveSessionScreen
import com.simats.focusflow.ui.screens.AiReportScreen
import com.simats.focusflow.ui.screens.AvatarSelectionScreen
import com.simats.focusflow.ui.screens.CreateAccountScreen
import com.simats.focusflow.ui.screens.DailyFocusGoalScreen
import com.simats.focusflow.ui.screens.DashboardScreen
import com.simats.focusflow.ui.screens.DataAiPreferencesScreen
import com.simats.focusflow.ui.screens.DistractionAnalysisScreen
import com.simats.focusflow.ui.screens.EditProfileScreen
import com.simats.focusflow.ui.screens.EditTaskScreen
import com.simats.focusflow.ui.screens.ExportDataScreen
import com.simats.focusflow.ui.screens.FocusTimeAnalysisScreen
import com.simats.focusflow.ui.screens.ForgotPasswordScreen
import com.simats.focusflow.ui.screens.GrootAiScreen
import com.simats.focusflow.ui.screens.HabitsScreen
import com.simats.focusflow.ui.screens.InsightsScreen
import com.simats.focusflow.ui.screens.LogDistractionScreen
import com.simats.focusflow.ui.screens.LoginScreen
import com.simats.focusflow.ui.screens.MakeItYoursScreen
import com.simats.focusflow.ui.screens.NotificationsScreen
import com.simats.focusflow.ui.screens.OnboardingScreen
import com.simats.focusflow.ui.screens.PlanNextDayScreen
import com.simats.focusflow.ui.screens.ProfileScreen
import com.simats.focusflow.ui.screens.ProgressConsistencyScreen
import com.simats.focusflow.ui.screens.ReadySetupCompleteScreen
import com.simats.focusflow.ui.screens.RecommendationsScreen
import com.simats.focusflow.ui.screens.SearchFiltersScreen
import com.simats.focusflow.ui.screens.SessionDetailsScreen
import com.simats.focusflow.ui.screens.SessionHistoryScreen
import com.simats.focusflow.ui.screens.SessionSummaryScreen
import com.simats.focusflow.ui.screens.SetIntentionScreen
import com.simats.focusflow.ui.screens.SetupProfileScreen
import com.simats.focusflow.ui.screens.SetupScreen
import com.simats.focusflow.ui.screens.SignupScreen
import com.simats.focusflow.ui.screens.SplashScreen
import com.simats.focusflow.ui.screens.TakeBreatherScreen
import com.simats.focusflow.ui.screens.TaskDetailsScreen
import com.simats.focusflow.ui.screens.TasksScreen
import com.simats.focusflow.ui.screens.TimerScreen
import com.simats.focusflow.ui.screens.WeeklyReportDetailsScreen
import com.simats.focusflow.ui.screens.WelcomeStartScreen
import com.simats.focusflow.ui.screens.WhyThisSuggestionScreen
import com.simats.focusflow.ui.theme.FOCUSFLOWTheme

class MainActivity : ComponentActivity() {
    private val pendingRoute = MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("route")?.let { route ->
            pendingRoute.value = route
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.simats.focusflow.data.repository.ProductivityRepository.init(applicationContext)

        // Create standard notification channels on app startup
        NotificationHelper.createNotificationChannels(applicationContext)

        // Ensure persistent daily focus reminder is actively scheduled in AlarmManager
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager.getInstance(applicationContext)
                val notifEnabled = sessionManager.notificationsEnabledFlow.first()
                val dailyEnabled = sessionManager.dailyReminderEnabledFlow.first()
                val reminderTime = sessionManager.reminderTimeFlow.first()
                ReminderScheduler.scheduleDailyReminder(
                    applicationContext,
                    reminderTime,
                    notifEnabled && dailyEnabled
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to schedule daily reminder on startup: ${e.message}")
            }
        }

        intent.getStringExtra("route")?.let { route ->
            pendingRoute.value = route
        }

        // Firebase Cloud Messaging Token Registration & Logging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FlowFCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FlowFCM", "Device Token: $token")
            token?.let { fcmToken ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val sessionManager = SessionManager.getInstance(applicationContext)
                    sessionManager.userSessionFlow.collect { session ->
                        session.userId?.let { uid ->
                            if (uid > 0) {
                                TokenSyncHelper.syncFcmToken(fcmToken, uid)
                            }
                        }
                    }
                }
            }
        }

        setContent {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager.getInstance(context) }
            val themeMode by sessionManager.themeModeFlow.collectAsState(initial = "light")

            // Android 13+ (API 33+) Runtime Notification Permission Request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled */ }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            FOCUSFLOWTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isMainScreen = Screen.isMainScreen(currentRoute)

                val targetRoute by pendingRoute.collectAsState()
                LaunchedEffect(targetRoute) {
                    targetRoute?.let { route ->
                        try {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to navigate to route: $route", e)
                        }
                        pendingRoute.value = null
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = FocusFlowTheme.colors.background,
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = {
                        if (isMainScreen) {
                            FocusFlowBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onNavigate = { destination ->
                                    navController.navigate(destination) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("welcome_start") {
                            WelcomeStartScreen(
                                onBack = { navController.popBackStack() },
                                onCreateAccount = { navController.navigate("signup") },
                                onLogin = { navController.navigate("login") }
                            )
                        }
                        composable("onboarding") {
                            OnboardingScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onBack = { navController.popBackStack() },
                                onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onForgotPassword = {
                                    navController.navigate("forgot_password")
                                },
                                onNavigateToSignup = {
                                    navController.navigate("signup")
                                },
                                onCreateAccount = {
                                    navController.navigate("signup")
                                }
                            )
                        }
                        composable("signup") {
                            SignupScreen(
                                onBack = { navController.popBackStack() },
                                onSignupSuccess = { userId ->
                                    navController.navigate("make_it_yours/$userId") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("create_account") {
                            SignupScreen(
                                onBack = { navController.popBackStack() },
                                onSignupSuccess = { userId ->
                                    navController.navigate("make_it_yours/$userId") {
                                        popUpTo("create_account") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("make_it_yours") {
                            MakeItYoursScreen(
                                userId = 1,
                                onEnterDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            route = "make_it_yours/{userId}",
                            arguments = listOf(
                                navArgument("userId") {
                                    type = NavType.IntType
                                    defaultValue = 1
                                }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
                            MakeItYoursScreen(
                                userId = userId,
                                onEnterDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("setup") {
                            SetupScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateNext = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                onNavigateToTimer = { navController.navigate(Screen.Timer.route) },
                                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToSetIntention = { navController.navigate(Screen.Timer.route) },
                                onNavigateToSessionHistory = { navController.navigate("session_history") },
                                onNavigateToSearchFilters = { navController.navigate("search_filters") },
                                onNavigateToDataAi = { navController.navigate("data_ai_preferences") },
                                onNavigateToGrootAi = { navController.navigate("groot_ai") },
                                onNavigateToFocusAnalysis = { navController.navigate("focus_time_analysis") },
                                onNavigateToDistraction = { navController.navigate("distraction_analysis") },
                                onNavigateToAiReport = { navController.navigate("ai_report") },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") }
                            )
                        }
                        composable("activities") {
                            ActivitiesScreen(
                                initialTab = "Tasks",
                                onNavigateToHome = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") },
                                onNavigateToEditTask = { navController.navigate("edit_task") },
                                onNavigateToGrootAi = { navController.navigate("groot_ai") }
                            )
                        }
                        composable(Screen.Tasks.route) {
                            ActivitiesScreen(
                                initialTab = "Tasks",
                                onNavigateToHome = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") },
                                onNavigateToEditTask = { navController.navigate("edit_task") },
                                onNavigateToGrootAi = { navController.navigate("groot_ai") }
                            )
                        }
                        composable(Screen.Timer.route) {
                            TimerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Habits.route) {
                            ActivitiesScreen(
                                initialTab = "Habits",
                                onNavigateToHome = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") },
                                onNavigateToEditTask = { navController.navigate("edit_task") },
                                onNavigateToGrootAi = { navController.navigate("groot_ai") }
                            )
                        }
                        composable(Screen.Profile.route) {
                            val context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            val sessionManager = remember { SessionManager.getInstance(context) }

                            ProfileScreen(
                                onNavigateToHome = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Dashboard.route) { inclusive = true } } },
                                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToDataAi = { navController.navigate("data_ai_preferences") },
                                onNavigateToDailyGoal = { navController.navigate("daily_focus_goal") },
                                onNavigateToExport = { navController.navigate("export_data") },
                                onNavigateToAboutFlow = { navController.navigate("about_flow") },
                                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                                onLogout = {
                                    coroutineScope.launch {
                                        sessionManager.clearSession()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("edit_task") {
                            EditTaskScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("set_intention") {
                            SetIntentionScreen(
                                onBack = { navController.popBackStack() },
                                onBeginFocus = { navController.navigate("active_session") }
                            )
                        }
                        composable("active_session") {
                            ActiveSessionScreen(
                                onBack = { navController.popBackStack() },
                                onEndSession = { navController.navigate("session_summary") },
                                onLogDistraction = { navController.navigate("log_distraction") },
                                onTakeBreak = { navController.navigate("take_breather") }
                            )
                        }
                        composable("take_breather") {
                            TakeBreatherScreen(
                                onResumeFocus = { navController.popBackStack() },
                                onSkipBreak = { navController.popBackStack() }
                            )
                        }
                        composable("log_distraction") {
                            LogDistractionScreen(
                                onBack = { navController.popBackStack() },
                                onResume = { navController.popBackStack() }
                            )
                        }
                        composable("session_summary") {
                            SessionSummaryScreen(
                                onBack = { navController.popBackStack() },
                                onSave = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                },
                                onViewInsights = { navController.navigate("insights") },
                                onSessionDetails = { navController.navigate("session_details") },
                                onSessionHistory = { navController.navigate("session_history") }
                            )
                        }
                        composable("session_history") {
                            SessionHistoryScreen(
                                onBack = { navController.popBackStack() },
                                onSearch = { navController.navigate("search_filters") },
                                onSessionClick = { navController.navigate("session_details") }
                            )
                        }
                        composable("session_details") {
                            SessionDetailsScreen(
                                onBack = { navController.popBackStack() },
                                onViewHistory = { navController.navigate("session_history") }
                            )
                        }
                        composable("search_filters") {
                            SearchFiltersScreen(
                                onBack = { navController.popBackStack() },
                                onApply = { navController.popBackStack() }
                            )
                        }
                        composable("insights") {
                            InsightsScreen(
                                onNavigateToHome = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            inclusive = false
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onNavigateToFocusAnalysis = { navController.navigate("focus_time_analysis") },
                                onNavigateToDistraction = { navController.navigate("distraction_analysis") },
                                onNavigateToAiReport = { navController.navigate("ai_report") },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") }
                            )
                        }
                        composable("focus_time_analysis") {
                            FocusTimeAnalysisScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("distraction_analysis") {
                            DistractionAnalysisScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("ai_report") {
                            AiReportScreen(
                                onBack = { navController.popBackStack() },
                                onViewRecommendations = { navController.navigate("recommendations") },
                                onViewReportDetails = { navController.navigate("weekly_report_details") }
                            )
                        }
                        composable("weekly_report_details") {
                            WeeklyReportDetailsScreen(
                                onBack = { navController.popBackStack() },
                                onViewRecommendations = { navController.navigate("recommendations") }
                            )
                        }
                        composable("recommendations") {
                            RecommendationsScreen(
                                onBack = { navController.popBackStack() },
                                onPlanPrediction = { navController.navigate("plan_next_day") },
                                onWhySuggestion = { navController.navigate("why_suggestion") }
                            )
                        }
                        composable("why_suggestion") {
                            WhyThisSuggestionScreen(
                                onBack = { navController.popBackStack() },
                                onApply = { navController.popBackStack() }
                            )
                        }
                        composable("plan_next_day") {
                            PlanNextDayScreen(
                                onBack = { navController.popBackStack() },
                                onReviewTasks = { navController.navigate(Screen.Tasks.route) }
                            )
                        }
                        composable("progress_consistency") {
                            ProgressConsistencyScreen(onBack = { navController.popBackStack() })
                        }
                        composable("edit_profile") {
                            EditProfileScreen(
                                onBack = { navController.popBackStack() },
                                onSaveSuccess = { navController.popBackStack() }
                            )
                        }
                        composable("about_flow") {
                            AboutFlowScreen(onBack = { navController.popBackStack() })
                        }
                        composable("daily_focus_goal") {
                            DailyFocusGoalScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { navController.popBackStack() }
                            )
                        }
                        composable("export_data") {
                            ExportDataScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("notifications") {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }
                        composable("data_ai_preferences") {
                            DataAiPreferencesScreen(onBack = { navController.popBackStack() })
                        }
                        composable("setup_profile") {
                            SetupProfileScreen(
                                onBack = { navController.popBackStack() },
                                onContinue = { navController.navigate("setup") },
                                onSkip = { navController.navigate("setup") }
                            )
                        }
                        composable("avatar_selection") {
                            AvatarSelectionScreen(
                                onBack = { navController.popBackStack() },
                                onContinue = { navController.navigate("setup") },
                                onSkip = { navController.navigate("setup") }
                            )
                        }
                        composable("ready_setup_complete") {
                            ReadySetupCompleteScreen(
                                onBack = { navController.popBackStack() },
                                onGoToDashboard = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onEditPreferences = { navController.navigate("setup") }
                            )
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onBack = { navController.popBackStack() },
                                onResetSuccess = {
                                    navController.popBackStack("login", inclusive = false)
                                }
                            )
                        }
                        composable("account_recovery") {
                            AccountRecoveryScreen(
                                onBack = { navController.popBackStack() },
                                onBackToLogin = { navController.popBackStack("login", inclusive = false) }
                            )
                        }
                        composable("task_details") {
                            TaskDetailsScreen(
                                onBack = { navController.popBackStack() },
                                onStartFocus = { navController.navigate("set_intention") },
                                onEditTask = { navController.navigate("edit_task") }
                            )
                        }
                        composable("groot_ai") {
                            GrootAiScreen(
                                onBack = { navController.popBackStack() },
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }
                    }
                }
            }
        }
    }
}
