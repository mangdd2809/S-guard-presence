package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SwitchGuardDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.GuardsBackendScreen
import com.example.ui.screens.OvertimeScreen
import com.example.ui.screens.PermitScreen
import com.example.ui.screens.ReportAndAiScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityGreen
import com.example.ui.viewmodel.SatpamViewModel

enum class NavigationItem(val title: String, val icon: ImageVector, val tag: String) {
    PRESENSI("Presensi", Icons.Default.Security, "nav_presensi"),
    PERIJINAN("Perijinan", Icons.AutoMirrored.Filled.Assignment, "nav_perijinan"),
    LEMBUR("Lembur", Icons.Default.MoreTime, "nav_lembur"),
    PETUGAS("Petugas", Icons.Default.Badge, "nav_petugas"),
    REPORT_AI("Report & AI", Icons.Default.Analytics, "nav_report_ai")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SatpamApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SatpamApp() {
    val viewModel: SatpamViewModel = viewModel()
    val guards by viewModel.guards.collectAsStateWithLifecycle()
    val currentGuard by viewModel.currentGuard.collectAsStateWithLifecycle()
    val activeAttendance by viewModel.activeAttendance.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSwitchGuardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button to always return to Presensi (Tab 0) before closing
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_syifaguard_square),
                            contentDescription = "Logo Syifa Guard",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "S-GUARD PRESENCE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "SYIFA GUARD • PREVENT & PROTECT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                color = CyanAccent,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick indicator of duty state
                    IconButton(
                        onClick = { showSwitchGuardDialog = true },
                        modifier = Modifier.testTag("top_switch_guard_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeAttendance != null) {
                                    Badge(containerColor = SecurityGreen)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Ganti Profil Petugas",
                                tint = if (activeAttendance != null) SecurityGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationItem.entries.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AttendanceScreen(
                    viewModel = viewModel,
                    onOpenSwitchGuard = { showSwitchGuardDialog = true }
                )
                1 -> PermitScreen(
                    viewModel = viewModel,
                    onOpenSwitchGuard = { showSwitchGuardDialog = true }
                )
                2 -> OvertimeScreen(
                    viewModel = viewModel,
                    onOpenSwitchGuard = { showSwitchGuardDialog = true }
                )
                3 -> GuardsBackendScreen(
                    viewModel = viewModel
                )
                4 -> ReportAndAiScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    if (showSwitchGuardDialog) {
        SwitchGuardDialog(
            guards = guards,
            currentGuard = currentGuard,
            onSelectGuard = { selected ->
                viewModel.selectGuard(selected)
            },
            onDismiss = { showSwitchGuardDialog = false }
        )
    }
}
