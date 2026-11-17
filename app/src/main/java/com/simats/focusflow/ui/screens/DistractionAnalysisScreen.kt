package com.simats.focusflow.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.DistractionLogItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.utils.AppUsageInfo
import com.simats.focusflow.utils.DistractionUsageSummary
import com.simats.focusflow.utils.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private val SegmentColors = listOf(
    Color(0xFF8B5CF6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFFEC4899),
    Color(0xFF06B6D4),
    Color(0xFF6366F1),
    Color(0xFF84CC16)
)

private fun getAppBrandColor(packageName: String?, index: Int): Color {
    if (packageName == null) return Color(0xFF6B7280)
    val pkg = packageName.lowercase()
    return when {
        pkg.contains("instagram") -> Color(0xFFE1306C)
        pkg.contains("youtube") -> Color(0xFFFF0000)
        pkg.contains("whatsapp") -> Color(0xFF25D366)
        pkg.contains("chrome") -> Color(0xFF4285F4)
        pkg.contains("google") -> Color(0xFF34A853)
        pkg.contains("snapchat") -> Color(0xFFFFB800)
        pkg.contains("reddit") -> Color(0xFFFF4500)
        pkg.contains("telegram") -> Color(0xFF24A1DE)
        pkg.contains("twitter") || pkg.contains("x.com") -> Color(0xFF1DA1F2)
        pkg.contains("spotify") -> Color(0xFF1DB954)
        pkg.contains("netflix") -> Color(0xFFE50914)
        pkg.contains("discord") -> Color(0xFF5865F2)
        else -> SegmentColors[index % SegmentColors.size]
    }
}


@Composable
fun UsagePermissionPromptCard(
    onGrantAccess: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Usage Access Required",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FocusFlow needs usage access to read your screen time and analyze app distractions, just like Digital Wellbeing.",
                fontSize = 13.5.sp,
                color = colors.textMuted,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onGrantAccess,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick()
            ) {
                Text(
                    text = "Grant Access",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DistractionAnalysisScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    val coroutineScope = rememberCoroutineScope()
    var usageSummary by remember { mutableStateOf(DistractionUsageSummary()) }
    var hasPermission by remember { mutableStateOf(UsageStatsHelper.hasUsageStatsPermission(context)) }
    var isLoading by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }
    var loggedDistractions by remember { mutableStateOf<List<DistractionLogItem>>(emptyList()) }

    fun refreshLoggedDistractions() {
        coroutineScope.launch(Dispatchers.IO) {
            val sessionMgr = SessionManager.getInstance(context)
            val userId = try {
                sessionMgr.userSessionFlow.first().userId ?: 1
            } catch (_: Exception) { 1 }

            val local = try {
                FocusSessionDatabase.getInstance(context).getDistractions(userId)
            } catch (_: Exception) { emptyList() }

            withContext(Dispatchers.Main) {
                loggedDistractions = local
            }

            try {
                val repoResult = ProductivityRepository(RetrofitClient.flowApiService).getDistractions(userId)
                if (repoResult.isSuccess) {
                    val merged = (local + (repoResult.getOrNull() ?: emptyList())).distinctBy {
                        if (it.id > 0) "id_${it.id}" else "${it.createdAt}_${it.reason}"
                    }
                    withContext(Dispatchers.Main) {
                        loggedDistractions = merged
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun refreshTodayUsageStats() {
        val perm = UsageStatsHelper.hasUsageStatsPermission(context)
        hasPermission = perm
        if (perm) {
            coroutineScope.launch(Dispatchers.IO) {
                val stats = UsageStatsHelper.queryTodayDistractionStats(context)
                withContext(Dispatchers.Main) {
                    usageSummary = stats
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshLoggedDistractions()
                val perm = UsageStatsHelper.hasUsageStatsPermission(context)
                val wasMissing = !hasPermission
                hasPermission = perm
                if (perm && (wasMissing || usageSummary.apps.isEmpty())) {
                    isLoading = true
                    refreshTodayUsageStats()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        refreshTodayUsageStats()
        refreshLoggedDistractions()
        FocusSessionDatabase.getInstance(context).distractionTableChangedFlow.collect {
            refreshLoggedDistractions()
        }
    }

    val todayDateFormatted = remember {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        sdf.format(Date())
    }

    val ringSegments = remember(usageSummary) {
        usageSummary.computeRingSegments(maxTopApps = 4)
    }

    val activeApps = remember(usageSummary) {
        usageSummary.apps.filter { it.usageMinutes > 0 }
    }

    val displayedApps = remember(activeApps, isExpanded) {
        if (isExpanded) activeApps else activeApps.take(3)
    }

    val totalMins = usageSummary.totalScreenMinutes
    val totalHoursStr = if (totalMins >= 60) {
        val h = totalMins / 60
        val m = totalMins % 60
        if (m > 0) "${h}h ${m}m" else "${h}h"
    } else {
        "${totalMins}m"
    }

    val grootInsight = remember(usageSummary) {
        UsageStatsHelper.generateGrootDistractionInsight(usageSummary)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060D))
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Distraction analysis", null, onBack)

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                    .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Today's Usage · $todayDateFormatted",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!hasPermission) {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                UsagePermissionPromptCard(
                    onGrantAccess = {
                        UsageStatsHelper.openUsageAccessSettings(context)
                    }
                )
            }
        }

        if (hasPermission) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val chartDiameter = minOf(maxWidth * 0.6f, 210.dp).coerceAtLeast(160.dp)
                Box(
                    modifier = Modifier.size(chartDiameter),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val diameter = size.minDimension - strokeWidth - 8.dp.toPx()
                        val topLeftOffset = androidx.compose.ui.geometry.Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        drawCircle(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.2f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = diameter / 1.4f
                            ),
                            radius = diameter / 1.4f,
                            center = Offset(cx, cy)
                        )

                        drawArc(
                            color = Color(0xFF1C1830),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = Color(0xFF282245),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (totalMins > 0 && ringSegments.isNotEmpty()) {
                            if (ringSegments.size <= 1) {
                                val single = ringSegments.first()
                                val singleColor = getAppBrandColor(single.packageName, 0)
                                drawArc(
                                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                        listOf(singleColor, singleColor.copy(alpha = 0.7f), singleColor)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeftOffset,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            } else {
                                var currentAngle = -90f
                                val gapDegrees = 6f
                                val availableSweep = 360f - (ringSegments.size * gapDegrees)

                                ringSegments.forEachIndexed { index, segment ->
                                    val segmentSweep = (segment.percentage * availableSweep).coerceAtLeast(6f)
                                    val segmentColor = getAppBrandColor(segment.packageName, index)

                                    drawArc(
                                        brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                            listOf(segmentColor, segmentColor.copy(alpha = 0.75f), segmentColor)
                                        ),
                                        startAngle = currentAngle,
                                        sweepAngle = segmentSweep,
                                        useCenter = false,
                                        topLeft = topLeftOffset,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += segmentSweep + gapDegrees
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (totalMins > 0) totalHoursStr else "0m",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lost to Distractions",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF472B6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GROOT AI INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = grootInsight,
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                ShimmerLoadingPlaceholder(colors)
            } else if (activeApps.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No data logged for this period.",
                            fontSize = 15.sp,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(20.dp).animateContentSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S APP BREAKDOWN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$totalHoursStr total",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        displayedApps.forEachIndexed { index, app ->
                            val segmentColor = SegmentColors[index % SegmentColors.size]
                            val durationText = if (app.usageMinutes >= 60) {
                                "${app.usageMinutes / 60}h ${app.usageMinutes % 60}m"
                            } else {
                                "${app.usageMinutes}m"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIconImage(
                                        drawable = app.icon,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = app.category,
                                            fontSize = 12.sp,
                                            color = colors.textMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = durationText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val progressFraction = if (totalMins > 0) {
                                    (app.usageMinutes.toFloat() / totalMins.toFloat()).coerceIn(0.03f, 1f)
                                } else 0f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .background(colors.border.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = progressFraction)
                                            .height(5.dp)
                                            .background(segmentColor, RoundedCornerShape(3.dp))
                                    )
                                }
                            }

                            if (index < displayedApps.size - 1) {
                                HorizontalDivider(
                                    color = colors.border.copy(alpha = 0.4f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        if (activeApps.size > 3) {
                            val remainingCount = activeApps.size - 3
                            TextButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .bounceClick(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isExpanded) "Show less" else "Show more ($remainingCount)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryLight
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colors.primaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOGGED INTERRUPTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${loggedDistractions.size} logged",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (loggedDistractions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No focus interruptions logged yet.",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        loggedDistractions.forEach { item ->
                            DistractionItemRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistractionItemRow(item: DistractionLogItem) {
    val colors = FocusFlowTheme.colors
    val durationText = when {
        item.durationSeconds in 1..59 -> "< 1 min"
        item.durationSeconds in 60..299 -> "1-5 min"
        item.durationSeconds >= 300 -> "> 5 min"
        else -> "< 1 min"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = item.category.ifBlank { "Other" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.reason.ifBlank { "Focus interruption" },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 2
            )
            if (item.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.createdAt,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = durationText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textMuted
        )
    }
}

@Composable
private fun AppIconImage(drawable: Drawable?, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) {
        if (drawable != null) {
            try {
                drawable.toBitmap(width = 96, height = 96).asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                FocusFlowTheme.colors.primary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = FocusFlowTheme.colors.primaryLight,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


@Composable
private fun ShimmerLoadingPlaceholder(colors: com.simats.focusflow.ui.theme.FocusFlowColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.primaryLight,
            modifier = Modifier.size(38.dp),
            strokeWidth = 3.dp
        )
    }
}
