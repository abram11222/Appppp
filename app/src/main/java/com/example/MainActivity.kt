package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.ServantRole
import com.example.ui.components.ChurchTopAppBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.BarcodeScannerDialog
import com.example.ui.screens.CloudSettingsDialog
import com.example.ui.screens.CurriculumScreen
import com.example.ui.screens.DeaconCardModal
import com.example.ui.screens.DeaconsScreen
import com.example.ui.screens.ImportGradeDeaconsDialog
import com.example.ui.screens.IncompleteDataDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MergeRestoreDialog
import com.example.ui.screens.ReportsAndCardsScreen
import com.example.ui.screens.ServantsManagementDialog
import com.example.ui.screens.VisitationScreen
import com.example.service.FirebaseInitService
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.util.NotificationHelper
import com.example.viewmodel.DeaconsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DeaconsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        FirebaseInitService.initialize(this)

        setContent {
            MyApplicationTheme {
                DeaconsMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DeaconsMainApp(viewModel: DeaconsViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Runtime Permission Request for Notifications (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.sendNotification(
                context,
                "⛪ خدمة الشمامسة",
                "تم تفعيل التنبيهات السحابية بنجاح! ستصلك تنبيهات الخدمة والافتقاد."
            )
        }
    }

    // ViewModel States
    val deacons by viewModel.filteredDeacons.collectAsState()
    val allDeacons by viewModel.deacons.collectAsState()
    val currentServant by viewModel.currentServant.collectAsState()
    val servants by viewModel.servants.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val cloudConfig by viewModel.cloudConfig.collectAsState()
    val attendanceList by viewModel.attendanceRecords.collectAsState()
    val attendanceTarget by viewModel.attendanceTarget.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val incompleteDeacons by viewModel.incompleteDeacons.collectAsState()
    val hymnCurriculums by viewModel.hymnCurriculums.collectAsState()
    val recitations by viewModel.recitations.collectAsState()
    val gospelSchedules by viewModel.gospelSchedules.collectAsState()
    val examResults by viewModel.examResults.collectAsState()
    val visitationGroups by viewModel.visitationGroups.collectAsState()
    val visitationLogs by viewModel.visitationLogs.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val selectedGradeFilter by viewModel.selectedGradeFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Navigation Tab state:
    // 0: Deacons, 1: Attendance, 2: Curriculum, 3: Visitation, 4: Reports, 5: Admin Dashboard
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog & Modal states
    var showServantsDialog by remember { mutableStateOf(false) }
    var showCloudSettingsDialog by remember { mutableStateOf(false) }
    var showIncompleteDialog by remember { mutableStateOf(false) }
    var showMergeRestoreDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var selectedDeaconForBadge by remember { mutableStateOf<Deacon?>(null) }
    var showImportGradeDialog by remember { mutableStateOf(false) }
    var importTargetGrade by remember { mutableStateOf(DeaconGrade.PRIMARY_1) }

    // Show toast / snackbar on user messages
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Default to Dashboard on first login if Admin
    LaunchedEffect(currentServant) {
        if (currentServant?.role == ServantRole.ADMIN && selectedTab == 0 && deacons.isEmpty()) {
            selectedTab = 5 // Go to Admin Dashboard
        }
    }

    // Authentication Guard: Show Login screen if currentServant is null
    if (currentServant == null) {
        LoginScreen(
            onLogin = { email, pass ->
                viewModel.loginWithResult(email, pass)
            },
            onRegisterServant = { name, phone, email, pass, grade, onComplete ->
                viewModel.registerServant(name, phone, email, pass, grade, onComplete)
            },
            onOpenFirebaseSettings = {
                showCloudSettingsDialog = true
            }
        )

        if (showCloudSettingsDialog) {
            CloudSettingsDialog(
                config = cloudConfig,
                syncState = syncState,
                lastSyncTime = lastSyncTime,
                onDismiss = { showCloudSettingsDialog = false },
                onSaveConfig = { viewModel.updateCloudConfig(it) }
            )
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChurchTopAppBar(
                currentServant = currentServant,
                syncState = syncState,
                onDashboardClick = { selectedTab = 5 },
                onSwitchServantClick = { showServantsDialog = true },
                onTestNotificationClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@ChurchTopAppBar
                        }
                    }
                    viewModel.testNotification()
                },
                onOpenCloudSettings = { showCloudSettingsDialog = true },
                onLogoutClick = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp
            ) {
                // If Admin, show Dashboard tab
                if (currentServant?.role == ServantRole.ADMIN) {
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "لوحة التحكم") },
                        label = { Text("اللوحة", fontSize = 11.sp, fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ModernPrimary,
                            selectedTextColor = ModernPrimary,
                            indicatorColor = ModernPrimaryContainer,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.People, contentDescription = "المخدومين") },
                    label = { Text("المخدومين", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernPrimary,
                        selectedTextColor = ModernPrimary,
                        indicatorColor = ModernPrimaryContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "الحضور") },
                    label = { Text("الحضور", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernPrimary,
                        selectedTextColor = ModernPrimary,
                        indicatorColor = ModernPrimaryContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "المناهج") },
                    label = { Text("المناهج", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernPrimary,
                        selectedTextColor = ModernPrimary,
                        indicatorColor = ModernPrimaryContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "الافتقاد") },
                    label = { Text("الافتقاد", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernPrimary,
                        selectedTextColor = ModernPrimary,
                        indicatorColor = ModernPrimaryContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "التقارير") },
                    label = { Text("التقارير", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernPrimary,
                        selectedTextColor = ModernPrimary,
                        indicatorColor = ModernPrimaryContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> DeaconsScreen(
                        deacons = deacons,
                        allDeacons = allDeacons,
                        attendanceList = attendanceList,
                        searchQuery = searchQuery,
                        sortOrder = sortOrder,
                        incompleteCount = incompleteDeacons.size,
                        selectedGrade = selectedGradeFilter,
                        isRefreshing = isRefreshing,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSortChange = { viewModel.setSortOrder(it) },
                        onGradeChange = { viewModel.setSelectedGrade(it) },
                        onRefresh = { viewModel.refreshData() },
                        onOpenImportDialog = { grade ->
                            importTargetGrade = grade
                            showImportGradeDialog = true
                        },
                        onSaveDeacon = { viewModel.saveDeacon(it) },
                        onDeleteDeacon = { viewModel.deleteDeacon(it) },
                        onOpenIncompleteDialog = { showIncompleteDialog = true },
                        onShowDeaconBadge = { selectedDeaconForBadge = it }
                    )

                    1 -> AttendanceScreen(
                        deacons = allDeacons,
                        attendanceList = attendanceList,
                        target = attendanceTarget,
                        selectedDate = selectedDate,
                        onDateChange = { viewModel.setSelectedDate(it) },
                        onToggleAttendance = { deaconId, isServ, isMass ->
                            viewModel.toggleAttendance(deaconId, selectedDate, isServ, isMass)
                        },
                        onMarkAllPresent = { isServ, isMass ->
                            viewModel.markAllPresentForDate(selectedDate, isServ, isMass)
                        },
                        onUpdateTargets = { serv, mass ->
                            viewModel.updateTarget(serv, mass)
                        },
                        onOpenBarcodeScanner = { showBarcodeScanner = true }
                    )

                    2 -> CurriculumScreen(
                        deacons = allDeacons,
                        hymnCurriculums = hymnCurriculums,
                        recitations = recitations,
                        gospelSchedules = gospelSchedules,
                        examResults = examResults,
                        onSaveHymnCurriculum = { viewModel.saveHymnCurriculum(it) },
                        onSaveRecitation = { viewModel.saveHymnRecitation(it) },
                        onSaveGospel = { viewModel.saveGospelSchedule(it) },
                        onSaveExam = { viewModel.saveExamResult(it) }
                    )

                    3 -> VisitationScreen(
                        visitationGroups = visitationGroups,
                        visitationLogs = visitationLogs,
                        onSaveVisitationLog = { viewModel.saveVisitationLog(it) }
                    )

                    4 -> ReportsAndCardsScreen(
                        deacons = allDeacons,
                        attendanceList = attendanceList,
                        currentServant = currentServant!!,
                        onSelectDeaconForCard = { selectedDeaconForBadge = it },
                        onOpenMergeRestore = { showMergeRestoreDialog = true },
                        onOpenServantsManagement = { showServantsDialog = true },
                        onTestNotification = { viewModel.testNotification() }
                    )

                    5 -> AdminDashboardScreen(
                        currentServant = currentServant!!,
                        deacons = allDeacons,
                        attendanceRecords = attendanceList,
                        recitations = recitations,
                        servants = servants,
                        target = attendanceTarget,
                        onNavigateToDeacons = { selectedTab = 0 },
                        onNavigateToAttendance = { selectedTab = 1 },
                        onNavigateToCurriculum = { selectedTab = 2 },
                        onNavigateToVisitation = { selectedTab = 3 },
                        onNavigateToReports = { selectedTab = 4 },
                        onNavigateToIncompleteData = { showIncompleteDialog = true },
                        onOpenServantsManagement = { showServantsDialog = true },
                        onOpenFirebaseSettings = { showCloudSettingsDialog = true },
                        onClearAllDeacons = { viewModel.clearAllDeacons() },
                        onShareReport = { viewModel.shareFullReport() },
                        onTestNotification = { viewModel.testNotification() },
                        onLogout = { viewModel.logout() },
                        onSelectGrade = { grade ->
                            viewModel.setSelectedGrade(grade)
                            selectedTab = 0
                        },
                        onOpenImportGrade = { grade ->
                            importTargetGrade = grade
                            showImportGradeDialog = true
                        }
                    )

                    else -> Unit
                }
            }
        }
    }

    // Modals & Sub-dialogs
    if (showServantsDialog) {
        ServantsManagementDialog(
            servants = servants,
            currentServant = currentServant!!,
            onDismiss = { showServantsDialog = false },
            onSwitchServant = {
                viewModel.switchActiveServant(it)
                showServantsDialog = false
            },
            onUpdatePermissions = {
                viewModel.updateServantPermissions(it)
            },
            onAddNewServant = {
                viewModel.addServant(it)
            },
            onApproveServant = { servantId, role, grade, edit, attend, cur, vis, exp, manage ->
                viewModel.approveServant(servantId, role, grade, edit, attend, cur, vis, exp, manage)
            },
            onRejectServant = { servantId ->
                viewModel.rejectServant(servantId)
            }
        )
    }

    if (showCloudSettingsDialog) {
        CloudSettingsDialog(
            config = cloudConfig,
            syncState = syncState,
            lastSyncTime = lastSyncTime,
            onDismiss = { showCloudSettingsDialog = false },
            onSaveConfig = { viewModel.updateCloudConfig(it) }
        )
    }

    if (showIncompleteDialog) {
        IncompleteDataDialog(
            incompleteDeacons = incompleteDeacons,
            onDismiss = { showIncompleteDialog = false },
            onEditDeacon = { deacon ->
                showIncompleteDialog = false
                selectedTab = 0
            }
        )
    }

    if (showMergeRestoreDialog) {
        MergeRestoreDialog(
            currentDeaconCount = allDeacons.size,
            onDismiss = { showMergeRestoreDialog = false },
            onPreviewDiff = { viewModel.previewMerge(it) },
            onExecuteMerge = { viewModel.executeSmartMerge(it) }
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            deacons = allDeacons,
            onDismiss = { showBarcodeScanner = false },
            onAttendanceMarked = { deaconId, isServ, isMass ->
                viewModel.toggleAttendance(
                    deaconId = deaconId,
                    date = selectedDate,
                    isService = isServ,
                    isMass = isMass
                )
            },
            onOpenProfile = { deacon ->
                selectedDeaconForBadge = deacon
                showBarcodeScanner = false
            }
        )
    }

    selectedDeaconForBadge?.let { deacon ->
        DeaconCardModal(
            deacon = deacon,
            onDismiss = { selectedDeaconForBadge = null }
        )
    }

    if (showImportGradeDialog) {
        ImportGradeDeaconsDialog(
            initialGrade = importTargetGrade,
            existingDeacons = allDeacons,
            onDismiss = { showImportGradeDialog = false },
            onImportDirectDeacons = { grade, deaconsList ->
                viewModel.importDeaconsDirectly(grade, deaconsList)
                showImportGradeDialog = false
            },
            onImportTextLines = { grade, lines ->
                viewModel.importDeaconsForGrade(grade, lines)
                showImportGradeDialog = false
            }
        )
    }
}
