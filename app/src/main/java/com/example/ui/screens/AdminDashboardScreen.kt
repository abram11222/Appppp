package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.model.AttendanceTarget
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.HymnRecitation
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.ui.components.SaintStephenLogoEmblem
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryContainer
import com.example.ui.theme.ModernPrimaryDark
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.ModernSuccessContainer
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    currentServant: ServantAccount,
    deacons: List<Deacon>,
    attendanceRecords: List<AttendanceRecord>,
    recitations: List<HymnRecitation>,
    servants: List<ServantAccount>,
    target: AttendanceTarget,
    onNavigateToDeacons: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToCurriculum: () -> Unit,
    onNavigateToVisitation: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToIncompleteData: () -> Unit,
    onOpenServantsManagement: () -> Unit,
    onOpenFirebaseSettings: () -> Unit,
    onClearAllDeacons: () -> Unit,
    onShareReport: () -> Unit,
    onTestNotification: () -> Unit,
    onLogout: () -> Unit,
    onSelectGrade: (DeaconGrade) -> Unit = {},
    onOpenImportGrade: (DeaconGrade) -> Unit = {}
) {
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedGradeView by remember { mutableStateOf<DeaconGrade?>(null) }

    val totalDeacons = deacons.size
    val incompleteCount = deacons.count { it.isDataIncomplete }

    // Attendance calculation
    val totalServiceAttendance = attendanceRecords.count { it.isServicePresent }
    val totalMassAttendance = attendanceRecords.count { it.isMassPresent }
    val totalPossibleRecords = (attendanceRecords.size * 2).coerceAtLeast(1)
    val overallAttendancePercent = ((totalServiceAttendance + totalMassAttendance).toFloat() / totalPossibleRecords * 100).toInt()

    val completedRecitationsCount = recitations.count { it.isPassed }
    val pendingServantsCount = servants.count { !it.isApproved }

    val modernHeaderGradient = Brush.verticalGradient(
        colors = listOf(
            ModernPrimaryDark,
            ModernPrimary
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = ModernPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(modernHeaderGradient)
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SaintStephenLogoEmblem(
                                    size = 46.dp,
                                    showOuterRing = false
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "لوحة تحكم أمين الخدمة",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = "خدمة الشهيد استفانوس • مرحباً ${currentServant.name}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.testTag("dashboard_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "تسجيل خروج",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Modern status pill banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "١٣ فصلاً دراسياً",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pending Servants Requests Banner (if any)
        if (pendingServantsCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenServantsManagement() }
                        .testTag("pending_servants_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFDE68A),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "طلبات انضمام خدام جديدة ($pendingServantsCount)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                )
                                Text(
                                    text = "يوجد خدام بانتظار موافقتك وتحديد الفصل والصلاحيات لهم",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFB45309),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = onOpenServantsManagement,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("مراجعة الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Summary KPI Metrics
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "إجمالي الشمامسة",
                        value = "$totalDeacons",
                        subtext = "بجميع الفصول",
                        icon = Icons.Default.Groups,
                        containerColor = Color(0xFFEFF6FF),
                        tintColor = Color(0xFF1D4ED8),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDeacons
                    )
                    MetricCard(
                        title = "فريق الخدام",
                        value = "${servants.size}",
                        subtext = "خدام ومشرفين",
                        icon = Icons.Default.ManageAccounts,
                        containerColor = Color(0xFFF0FDF4),
                        tintColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenServantsManagement
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "نسبة الحضور",
                        value = "$overallAttendancePercent%",
                        subtext = "الخدمة والقداس",
                        icon = Icons.Default.FactCheck,
                        containerColor = Color(0xFFFAF5FF),
                        tintColor = Color(0xFF7E22CE),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance
                    )
                    MetricCard(
                        title = "بيانات ناقصة",
                        value = "$incompleteCount",
                        subtext = if (incompleteCount > 0) "بحاجة للاستكمال" else "ملفات مكتملة",
                        icon = Icons.Default.Warning,
                        containerColor = if (incompleteCount > 0) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
                        tintColor = if (incompleteCount > 0) Color(0xFFB45309) else Color(0xFF15803D),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToIncompleteData
                    )
                }
            }
        }

        // Section: Classes & Grades Breakdown (حضانة لحد ٣ ثانوي)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فصول ومراحل الخدمة (حضانة - ٣ ثانوي)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "بيانات المخدومين والخدام ونسب الحضور لكل فصل بمير",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Button(
                        onClick = { onOpenImportGrade(DeaconGrade.PRIMARY_1) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استيراد كشف", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // All 13 Classes Cards
        items(DeaconGrade.values()) { grade ->
            val gradeDeacons = deacons.filter { it.grade == grade }
            val gradeServants = servants.filter { it.assignedGrade == grade }
            val deaconIdsInGrade = gradeDeacons.map { it.id }.toSet()
            val gradeAttendance = attendanceRecords.filter { deaconIdsInGrade.contains(it.deaconId) }
            val gradeServiceAtt = gradeAttendance.count { it.isServicePresent }
            val gradeMassAtt = gradeAttendance.count { it.isMassPresent }
            val gradeTotalPossible = (gradeAttendance.size * 2).coerceAtLeast(1)
            val gradeAttPercent = if (gradeAttendance.isEmpty()) 0 else ((gradeServiceAtt + gradeMassAtt).toFloat() / gradeTotalPossible * 100).toInt()
            val gradeIncomplete = gradeDeacons.count { it.isDataIncomplete }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header of the Class
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ModernPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = ModernPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "فصل ${grade.arabicTitle}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = grade.stage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Deacon count badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (gradeDeacons.isNotEmpty()) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (gradeDeacons.isNotEmpty()) Color(0xFFBFDBFE) else Color(0xFFCBD5E1)
                            )
                        ) {
                            Text(
                                text = "${gradeDeacons.size} شماس",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (gradeDeacons.isNotEmpty()) Color(0xFF1D4ED8) else TextSecondary,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Servants assigned to this grade
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ModernPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (gradeServants.isNotEmpty()) {
                                "الخدام المسؤولون: " + gradeServants.joinToString("، ") { it.name }
                            } else {
                                "الخدام المسؤولون: لم يتم التعيين بعد"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = if (gradeServants.isNotEmpty()) MaterialTheme.colorScheme.onSurface else TextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats row for this class
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("نسبة الحضور", fontSize = 10.sp, color = TextSecondary)
                                Text("$gradeAttPercent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ModernPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("بيانات ناقصة", fontSize = 10.sp, color = TextSecondary)
                                Text("$gradeIncomplete", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (gradeIncomplete > 0) Color(0xFFB45309) else Color(0xFF15803D))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("حضور القداس", fontSize = 10.sp, color = TextSecondary)
                                Text("$gradeMassAtt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Modern Action Buttons for this class
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSelectGrade(grade)
                                onNavigateToDeacons()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text("عرض مخدومي الفصل", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { onOpenImportGrade(grade) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد كشف الفصل", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section: Servants and Supervisors Matrix
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "فريق الخدام والمشرفين بمير",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "توزيع المهام والفصول والصلاحيات",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = onOpenServantsManagement,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة الخدام", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    servants.forEach { servant ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (servant.role == ServantRole.ADMIN) ModernPrimaryContainer else Color(0xFFE2E8F0),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (servant.role == ServantRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (servant.role == ServantRole.ADMIN) ModernPrimary else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = servant.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (servant.assignedGrade != null) {
                                                "${servant.role.arabicName} • مسؤول: ${servant.assignedGrade.arabicTitle}"
                                            } else {
                                                servant.role.arabicName
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (servant.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = if (servant.isActive) "نشط" else "معطل",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (servant.isActive) Color(0xFF15803D) else Color(0xFFB91C1C)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Administrative Operations & Cloud Hub
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "عمليات الإدارة السحابية والتقارير",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "أدوات الربط والمزامنة واستخراج بيانات خدمة الشهيد استفانوس للشمامسة",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernPillButton(
                            title = "استيراد كشف عام",
                            icon = Icons.Default.GroupAdd,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenImportGrade(DeaconGrade.PRIMARY_1) }
                        )
                        ModernPillButton(
                            title = "إعدادات السحابة",
                            icon = Icons.Default.CloudSync,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenFirebaseSettings
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernPillButton(
                            title = "تصدير تقرير شامل",
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f),
                            onClick = onShareReport
                        )
                        ModernPillButton(
                            title = "إشعار تجريبي",
                            icon = Icons.Default.Notifications,
                            modifier = Modifier.weight(1f),
                            onClick = onTestNotification
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Dangerous Clear Action
                    OutlinedButton(
                        onClick = { showClearConfirmationDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تفريغ قاعدة البيانات (للأمين فقط)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = {
                Text(
                    text = "تأكيد مسح البيانات بالكامل",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في تفريغ سجلات الشمامسة بالكامل في خدمة مير؟ هذا الإجراء سيحذف البيانات سحابياً ولا يمكن التراجع عنه.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmationDialog = false
                        onClearAllDeacons()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، تفريغ السجل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    containerColor: Color,
    tintColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, tintColor.copy(alpha = 0.25f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = tintColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = tintColor,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = tintColor.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun ModernPillButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ModernPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ModernPrimary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
