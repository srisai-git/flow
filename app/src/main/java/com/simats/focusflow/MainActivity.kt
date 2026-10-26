package com.simats.focusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.focusflow.ui.screens.AboutFlowScreen
import com.simats.focusflow.ui.screens.AccountRecoveryScreen
import com.simats.focusflow.ui.screens.ActiveSessionScreen
import com.simats.focusflow.ui.screens.AiReportScreen
import com.simats.focusflow.ui.screens.CreateAccountScreen
import com.simats.focusflow.ui.screens.DailyFocusGoalScreen
import com.simats.focusflow.ui.screens.DashboardScreen
import com.simats.focusflow.ui.screens.DataAiPreferencesScreen
import com.simats.focusflow.ui.screens.DistractionAnalysisScreen
import com.simats.focusflow.ui.screens.EditProfileScreen
import com.simats.focusflow.ui.screens.EditTaskScreen
import com.simats.focusflow.ui.screens.FocusTimeAnalysisScreen
import com.simats.focusflow.ui.screens.ForgotPasswordScreen
import com.simats.focusflow.ui.screens.InsightsScreen
import com.simats.focusflow.ui.screens.LogDistractionScreen
import com.simats.focusflow.ui.screens.LoginScreen
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
import com.simats.focusflow.ui.screens.SetupScreen
import com.simats.focusflow.ui.screens.SplashScreen
import com.simats.focusflow.ui.screens.TakeBreatherScreen
import com.simats.focusflow.ui.screens.TaskDetailsScreen
import com.simats.focusflow.ui.screens.TasksScreen
import com.simats.focusflow.ui.screens.VerifyPhoneScreen
import com.simats.focusflow.ui.screens.WelcomeStartScreen
import com.simats.focusflow.ui.screens.WhyThisSuggestionScreen
import com.simats.focusflow.ui.theme.FOCUSFLOWTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FOCUSFLOWTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF070510)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("welcome_start") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("welcome_start") {
                            WelcomeStartScreen(
                                onBack = { navController.popBackStack() },
                                onCreateAccount = { navController.navigate("create_account") },
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
                                onNavigateToSetup = {
                                    navController.navigate("setup")
                                },
                                onForgotPassword = {
                                    navController.navigate("forgot_password")
                                },
                                onCreateAccount = {
                                    navController.navigate("create_account")
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
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToTasks = { navController.navigate("tasks") },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToSetIntention = { navController.navigate("set_intention") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToSessionHistory = { navController.navigate("session_history") },
                                onNavigateToSearchFilters = { navController.navigate("search_filters") },
                                onNavigateToDataAi = { navController.navigate("data_ai_preferences") }
                            )
                        }
                        composable("tasks") {
                            TasksScreen(
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToProgressConsistency = { navController.navigate("progress_consistency") },
                                onNavigateToEditTask = { navController.navigate("edit_task") }
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
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
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
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToTasks = { navController.navigate("tasks") },
                                onNavigateToProfile = { navController.navigate("profile") },
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
                            com.simats.focusflow.ui.screens.WeeklyReportDetailsScreen(
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
                                onReviewTasks = { navController.navigate("tasks") }
                            )
                        }
                        composable("progress_consistency") {
                            ProgressConsistencyScreen(onBack = { navController.popBackStack() })
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateToHome = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                                onNavigateToTasks = { navController.navigate("tasks") },
                                onNavigateToInsights = { navController.navigate("insights") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToDataAi = { navController.navigate("data_ai_preferences") },
                                onNavigateToDailyGoal = { navController.navigate("daily_focus_goal") },
                                onNavigateToExport = { navController.navigate("export_data") },
                                onNavigateToAboutFlow = { navController.navigate("about_flow") },
                                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
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
                            com.simats.focusflow.ui.screens.ExportDataScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("notifications") {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }
                        composable("data_ai_preferences") {
                            DataAiPreferencesScreen(onBack = { navController.popBackStack() })
                        }
                        composable("create_account") {
                            CreateAccountScreen(
                                onBack = { navController.popBackStack() },
                                onContinue = { navController.navigate("verify_phone") },
                                onLogin = { navController.navigate("login") }
                            )
                        }
                        composable("verify_phone") {
                            VerifyPhoneScreen(
                                onBack = { navController.popBackStack() },
                                onVerify = { navController.navigate("ready_setup_complete") },
                                onChangePhone = { navController.popBackStack() }
                            )
                        }
                        composable("ready_setup_complete") {
                            ReadySetupCompleteScreen(
                                onBack = { navController.popBackStack() },
                                onGoToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onEditPreferences = { navController.navigate("setup") }
                            )
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onBack = { navController.popBackStack() },
                                onSend = { navController.navigate("account_recovery") },
                                onBackToLogin = { navController.popBackStack("login", inclusive = false) }
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
                    }
                }
            }
        }
    }
}