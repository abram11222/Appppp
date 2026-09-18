package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DeaconGrade
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServantsManagementDialog(
    servants: List<ServantAccount>,
    currentServant: ServantAccount,
    onDismiss: () -> Unit,
    onSwitchServant: (String) -> Unit,
    onUpdatePermissions: (ServantAccount) -> Unit,
    onAddNewServant: (ServantAccount) -> Unit,
    onApproveServant: (
        servantId: String,
        role: ServantRole,
        assignedGrade: DeaconGrade?,
        canEditDeacons: Boolean,
        canTakeAttendance: Boolean,
        canScoreCurriculum: Boolean,
        canConductVisitation: Boolean,
        canExportReports: Boolean,
        canManagePermissions: Boolean
    ) -> Unit,
    onRejectServant: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending Requests, 1: Approved Servants
    var approvingServant by remember { mutableStateOf<ServantAccount?>(null) }
    var selectedServantForEdit by remember { mutableStateOf<ServantAccount?>(null) }
    var servantToReject by remember { mutableStateOf<ServantAccount?>(null) }

    val pendingList = servants.filter { !it.isApproved }
    val approvedList = servants.filter { it.isApproved }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
                .testTag("servants_management_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ModernPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إدارة طلبات الخدام والصلاحيات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "مربوطة حياً بـ Firebase Firestore للجميع",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Pending Requests vs Approved
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ModernPrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "الطلبات المعلقة",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == 0) ModernPrimary else TextSecondary
                                )
                                if (pendingList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${pendingList.size}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "الخدام المعتمدون (${approvedList.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) ModernPrimary else TextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                if (selectedTab == 0) {
                    // ================= PENDING REQUESTS TAB =================
                    if (pendingList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ModernSuccess,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "لا توجد طلبات انضمام معلقة حالياً",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "عند قيام أي خادم بإنشاء حساب جديد، سيظهر طلبه هنا فورياً للموافقة عليه وتحديد فصله.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pendingList) { servant ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFFFEF3C7),
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.HourglassTop,
                                                            contentDescription = null,
                                                            tint = Color(0xFFD97706),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = servant.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF92400E)
                                                        )
                                                    )
                                                    Text(
                                                        text = "طلب انضمام جديد",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFB45309),
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFDE68A)
                                            ) {
                                                Text(
                                                    text = "قيد المراجعة",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF92400E),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Details: Phone, Email, Requested Grade
                                        if (servant.phone.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = servant.phone, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = TextPrimary))
                                            }
                                        }

                                        if (servant.email.isNotBlank()) {
                                            Text(
                                                text = "البريد / الحساب: ${servant.email}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                            )
                                        }

                                        val reqGrade = servant.requestedGrade ?: servant.assignedGrade
                                        if (reqGrade != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                                Icon(Icons.Default.School, contentDescription = null, tint = ModernPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "الفصل المطلوب: ${reqGrade.arabicTitle}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = ModernPrimary
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action buttons: Approve and Reject
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { approvingServant = servant },
                                                colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("الموافقة وتحديد الصلاحيات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { servantToReject = servant },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("رفض", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ================= APPROVED SERVANTS TAB =================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(approvedList) { servant ->
                            val isCurrent = servant.id == currentServant.id
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) ModernPrimary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = servant.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimary
                                                )
                                                if (isCurrent) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = ModernPrimary
                                                    ) {
                                                        Text(
                                                            text = "أنت",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "${servant.role.arabicName} • ${servant.assignedGrade?.arabicTitle ?: "كافة الفصول"}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = ModernPrimary, fontWeight = FontWeight.SemiBold)
                                            )
                                            if (servant.phone.isNotBlank()) {
                                                Text(
                                                    text = "📞 ${servant.phone}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!isCurrent) {
                                                OutlinedButton(
                                                    onClick = { onSwitchServant(servant.id) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("دخول", fontSize = 11.sp)
                                                }
                                            }

                                            if (currentServant.role == ServantRole.ADMIN) {
                                                IconButton(
                                                    onClick = { selectedServantForEdit = servant },
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل الصلاحيات",
                                                        tint = ModernPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Permission Badges
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ModernPermissionChip("تعديل", servant.canEditDeacons)
                                        ModernPermissionChip("حضور", servant.canTakeAttendance)
                                        ModernPermissionChip("ألحان", servant.canScoreCurriculum)
                                        ModernPermissionChip("افتقاد", servant.canConductVisitation)
                                        ModernPermissionChip("تقارير", servant.canExportReports)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to Approve Pending Servant and Assign Grade & Permissions
    approvingServant?.let { servant ->
        Dialog(onDismissRequest = { approvingServant = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                var selectedRole by remember { mutableStateOf(servant.role) }
                var selectedGrade by remember { mutableStateOf<DeaconGrade?>(servant.requestedGrade ?: servant.assignedGrade ?: DeaconGrade.PRIMARY_4) }
                var isGradeMenuOpen by remember { mutableStateOf(false) }

                var canEdit by remember { mutableStateOf(servant.canEditDeacons) }
                var canAttend by remember { mutableStateOf(servant.canTakeAttendance) }
                var canCurriculum by remember { mutableStateOf(servant.canScoreCurriculum) }
                var canVisit by remember { mutableStateOf(servant.canConductVisitation) }
                var canExport by remember { mutableStateOf(servant.canExportReports) }
                var canManagePermissions by remember { mutableStateOf(servant.canManagePermissions) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "اعتماد الخادم: ${servant.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "حدد دور الخادم، الفصل المسؤول عنه، وصلاحياته في النظام",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Role selector
                    Text(text = "رتبة الخادم:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val rolesList = listOf(
                            Pair(ServantRole.SERVANT, "خادم فصل"),
                            Pair(ServantRole.ASSISTANT, "خادم مساعد"),
                            Pair(ServantRole.ADMIN, "أمين خدمة")
                        )
                        rolesList.forEach { (role, label) ->
                            val isSel = selectedRole == role
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ModernPrimary else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 4.dp),
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grade selector
                    Text(text = "تحديد فصل الخدمة:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    ExposedDropdownMenuBox(
                        expanded = isGradeMenuOpen,
                        onExpandedChange = { isGradeMenuOpen = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedGrade?.arabicTitle ?: "كافة الفصول (مشرف عام)",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = ModernPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGradeMenuOpen) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isGradeMenuOpen,
                            onDismissRequest = { isGradeMenuOpen = false }
                        ) {
                            DeaconGrade.values().forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade.arabicTitle) },
                                    onClick = {
                                        selectedGrade = grade
                                        isGradeMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Permissions Checklist
                    Text(text = "صلاحيات الخادم الممنوحة:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    PermissionToggleRow("تعديل وإضافة مخدومين في الفصل", canEdit) { canEdit = it }
                    PermissionToggleRow("رصد الحضور والغياب والقداسات", canAttend) { canAttend = it }
                    PermissionToggleRow("تقييم الألحان والمنهج والامتحانات", canCurriculum) { canCurriculum = it }
                    PermissionToggleRow("تسجيل الافتقاد الميداني وملاحظاته", canVisit) { canVisit = it }
                    PermissionToggleRow("تصدير ومشاركة وطباعة التقارير", canExport) { canExport = it }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onApproveServant(
                                    servant.id,
                                    selectedRole,
                                    selectedGrade,
                                    canEdit,
                                    canAttend,
                                    canCurriculum,
                                    canVisit,
                                    canExport,
                                    canManagePermissions
                                )
                                approvingServant = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("اعتماد رسمي وتفعيل الحساب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { approvingServant = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }
    }

    // Modal to Edit Permissions of an Approved Servant
    selectedServantForEdit?.let { servant ->
        Dialog(onDismissRequest = { selectedServantForEdit = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                var canEdit by remember { mutableStateOf(servant.canEditDeacons) }
                var canAttend by remember { mutableStateOf(servant.canTakeAttendance) }
                var canCurriculum by remember { mutableStateOf(servant.canScoreCurriculum) }
                var canVisit by remember { mutableStateOf(servant.canConductVisitation) }
                var canExport by remember { mutableStateOf(servant.canExportReports) }
                var selectedRole by remember { mutableStateOf(servant.role) }
                var selectedGrade by remember { mutableStateOf(servant.assignedGrade ?: DeaconGrade.PRIMARY_4) }
                var isGradeMenuOpen by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "تعديل صلاحيات الخادم: ${servant.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Grade selector
                    Text(text = "الفصل المسؤول عنه:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    ExposedDropdownMenuBox(
                        expanded = isGradeMenuOpen,
                        onExpandedChange = { isGradeMenuOpen = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedGrade.arabicTitle,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = ModernPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGradeMenuOpen) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = isGradeMenuOpen,
                            onDismissRequest = { isGradeMenuOpen = false }
                        ) {
                            DeaconGrade.values().forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade.arabicTitle) },
                                    onClick = {
                                        selectedGrade = grade
                                        isGradeMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PermissionToggleRow("تعديل وإضافة مخدومين", canEdit) { canEdit = it }
                    PermissionToggleRow("رصد الحضور والقداسات", canAttend) { canAttend = it }
                    PermissionToggleRow("رصد الألحان والامتحانات", canCurriculum) { canCurriculum = it }
                    PermissionToggleRow("تسجيل الافتقاد الميداني", canVisit) { canVisit = it }
                    PermissionToggleRow("تصدير وطباعة التقارير", canExport) { canExport = it }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdatePermissions(
                                    servant.copy(
                                        role = selectedRole,
                                        assignedGrade = selectedGrade,
                                        canEditDeacons = canEdit,
                                        canTakeAttendance = canAttend,
                                        canScoreCurriculum = canCurriculum,
                                        canConductVisitation = canVisit,
                                        canExportReports = canExport
                                    )
                                )
                                selectedServantForEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ التغييرات")
                        }

                        OutlinedButton(
                            onClick = { selectedServantForEdit = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for rejection
    servantToReject?.let { servant ->
        AlertDialog(
            onDismissRequest = { servantToReject = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("رفض طلب الخادم", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رفض وحذف طلب انضمام الخادم (${servant.name})؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectServant(servant.id)
                        servantToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("نعم، رفض الطلب")
                }
            },
            dismissButton = {
                TextButton(onClick = { servantToReject = null }) {
                    Text("تراجع")
                }
            }
        )
    }
}

@Composable
fun ModernPermissionChip(title: String, granted: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (granted) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (granted) Color(0xFFA7F3D0) else Color(0xFFFECACA))
    ) {
        Text(
            text = "$title ${if (granted) "✓" else "✕"}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (granted) Color(0xFF065F46) else Color(0xFF991B1B),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PermissionToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
