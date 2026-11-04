package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simats.focusflow.ui.components.SegmentedControl
import com.simats.focusflow.ui.theme.FocusFlowTheme

/**
 * Consolidated Activities Screen combining Task Management and Habit Tracking
 * with a dual-tab toggle at the top (Requirement 11).
 */
@Composable
fun ActivitiesScreen(
    initialTab: String = "Tasks",
    onNavigateToHome: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {},
    onNavigateToEditTask: () -> Unit = {},
    onNavigateToGrootAi: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            SegmentedControl(
                options = listOf("Tasks", "Habits"),
                selectedOption = selectedTab,
                onOptionSelected = { selectedTab = it }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == "Tasks") {
                TasksScreen(
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToInsights = onNavigateToInsights,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToProgressConsistency = onNavigateToProgressConsistency,
                    onNavigateToEditTask = onNavigateToEditTask
                )
            } else {
                HabitsScreen(
                    onNavigateBack = { selectedTab = "Tasks" },
                    onNavigateToGrootAi = onNavigateToGrootAi
                )
            }
        }
    }
}

