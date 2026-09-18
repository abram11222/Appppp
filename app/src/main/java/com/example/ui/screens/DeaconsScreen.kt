package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AttendanceRecord
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.DeaconRank
import com.example.ui.components.DeaconAvatar
import com.example.ui.components.MissingDataBanner
import com.example.ui.components.RankBadge
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryContainer
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.ModernSuccessContainer
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DeaconSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeaconsScreen(
    deacons: List<Deacon>,
    allDeacons: List<Deacon> = deacons,
    attendanceList: List<AttendanceRecord>,
    searchQuery: String,
    sortOrder: DeaconSortOrder,
    incompleteCount: Int,
    selectedGrade: DeaconGrade? = null,
    isRefreshing: Boolean = false,
    onSearchChange: (String) -> Unit,
    onSortChange: (DeaconSortOrder) -> Unit,
    onGradeChange: (DeaconGrade?) -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenImportDialog: (DeaconGrade) -> Unit = {},
    onSaveDeacon: (Deacon) -> Boolean,
    onDeleteDeacon: (String) -> Boolean,
    onOpenIncompleteDialog: () -> Unit,
    onShowDeaconBadge: (Deacon) -> Unit
) {
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var deaconToEdit by remember { mutableStateOf<Deacon?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search Bar and Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("بحث بالاسم، الباركود، الهاتف، الشارع...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("deacon_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "ترتيب",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DeaconSortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.title,
                                        fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                                        color = if (order == sortOrder) ModernPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    onSortChange(order)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Grade / Class Filter Chips Row (حضانة إلى ٣ ثانوي)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "All" chip
                    item {
                        val isAllSelected = selectedGrade == null
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllSelected) ModernPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAllSelected) ModernPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clickable { onGradeChange(null) }
                                .testTag("filter_all_grades")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "كل الفصول (${allDeacons.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Chips for each grade
                    items(DeaconGrade.values()) { grade ->
                        val isSelected = selectedGrade == grade
                        val countInGrade = allDeacons.count { it.grade == grade }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ModernPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ModernPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .clickable { onGradeChange(grade) }
                                .testTag("filter_grade_${grade.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${grade.arabicTitle} ($countInGrade)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Import Class Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ModernPrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ModernPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clickable { onOpenImportDialog(selectedGrade ?: DeaconGrade.PRIMARY_1) }
                        .testTag("import_class_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = ModernPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "استيراد كشف",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ModernPrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Sync / Pull-to-refresh Indicator Header Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isRefreshing) ModernPrimaryContainer else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clickable { onRefresh() }
                    .testTag("manual_refresh_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = ModernPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRefreshing) "جارٍ التحديث ومزامنة السحابة..." else "اسحب لأسفل أو اضغط هنا للتحديث والمزامنة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = ModernPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = ModernPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }

            // Missing Data Alert Banner
            MissingDataBanner(
                incompleteCount = incompleteCount,
                onClick = onOpenIncompleteDialog
            )

            // Result Count and Active Sort Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المعروض: ${deacons.size} شماس" + if (selectedGrade != null) " (فصل ${selectedGrade.arabicTitle})" else "",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "الترتيب: ${sortOrder.title}",
                    style = MaterialTheme.typography.labelSmall.copy(color = ModernPrimary)
                )
            }

            // Deacons List with PullToRefreshBox
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (deacons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = ModernPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (selectedGrade != null) "لا يوجد مخدومين مسجلين في فصل ${selectedGrade.arabicTitle}" else "لا يوجد مخدومين مطابقين للبحث",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يمكنك استيراد كشف الأسماء فوراً أو إضافة شماس جديد",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onOpenImportDialog(selectedGrade ?: DeaconGrade.PRIMARY_1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ModernPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("استيراد كشف الفصل")
                                }

                                OutlinedButton(
                                    onClick = { isCreatingNew = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة شماس")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(deacons, key = { it.id }) { deacon ->
                            DeaconItemCard(
                                deacon = deacon,
                                attendanceList = attendanceList,
                                onCardClick = { onShowDeaconBadge(deacon) },
                                onCallClick = {
                                    val phoneToCall = deacon.phone.ifBlank { deacon.parentPhone }
                                    if (phoneToCall.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                                        context.startActivity(intent)
                                    }
                                },
                                onBadgeClick = { onShowDeaconBadge(deacon) },
                                onEditClick = { deaconToEdit = deacon },
                                onDeleteClick = { onDeleteDeacon(deacon.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Deacon
        FloatingActionButton(
            onClick = { isCreatingNew = true },
            containerColor = ModernPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_deacon_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مخدوم")
        }
    }

    // Add / Edit Modal Dialog
    if (isCreatingNew || deaconToEdit != null) {
        val initial = deaconToEdit ?: Deacon(grade = selectedGrade ?: DeaconGrade.PRIMARY_1)
        DeaconEditorDialog(
            deacon = initial,
            isNew = isCreatingNew,
            onDismiss = {
                isCreatingNew = false
                deaconToEdit = null
            },
            onSave = { saved ->
                onSaveDeacon(saved)
                isCreatingNew = false
                deaconToEdit = null
            }
        )
    }
}

@Composable
fun DeaconItemCard(
    deacon: Deacon,
    attendanceList: List<AttendanceRecord>,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onBadgeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val records = attendanceList.filter { it.deaconId == deacon.id }
    val serviceAttended = records.count { it.isServicePresent }
    val massAttended = records.count { it.isMassPresent }
    val totalPoss = records.size.coerceAtLeast(1)
    val attendanceRate = ((serviceAttended + massAttended).toFloat() / (totalPoss * 2) * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("deacon_card_${deacon.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name & Badges, Attendance Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                DeaconAvatar(
                    photoUrl = deacon.photoUrl,
                    name = deacon.name,
                    sizeDp = 48
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deacon.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RankBadge(rank = deacon.rank)

                        // Grade Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = deacon.grade.arabicTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "• ${deacon.code128}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Attendance Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Text(
                        text = "$attendanceRate% حضور",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Optional Address or Phone Preview
            if (deacon.fullAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = deacon.fullAddress,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Modern, clean and beautifully aligned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call Button
                    if (deacon.phone.isNotBlank() || deacon.parentPhone.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onCallClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "اتصال",
                                    tint = ModernPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // ID Badge Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onBadgeClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "كارنيه الشماس",
                                tint = ModernPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Edit Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onEditClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = ModernPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Delete Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDeleteClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeaconEditorDialog(
    deacon: Deacon,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Deacon) -> Unit
) {
    var name by remember { mutableStateOf(deacon.name) }
    var birthDate by remember { mutableStateOf(deacon.dateOfBirth) }
    var rank by remember { mutableStateOf(deacon.rank) }
    var grade by remember { mutableStateOf(deacon.grade) }
    var phone by remember { mutableStateOf(deacon.phone) }
    var parentPhone by remember { mutableStateOf(deacon.parentPhone) }
    var governorate by remember { mutableStateOf(deacon.governorate) }
    var district by remember { mutableStateOf(deacon.district) }
    var area by remember { mutableStateOf(deacon.area) }
    var street by remember { mutableStateOf(deacon.street) }
    var building by remember { mutableStateOf(deacon.buildingNumber) }
    var floor by remember { mutableStateOf(deacon.floor) }
    var photoUrl by remember { mutableStateOf(deacon.photoUrl) }
    var notes by remember { mutableStateOf(deacon.pastoralNotes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
                .testTag("deacon_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "➕ إضافة مخدوم جديد" else "✏️ تعديل بيانات الشماس",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ModernPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل الرباعي *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Grade / Class Selection Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    var showGradeDropdown by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = "${grade.arabicTitle} (${grade.stage})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الفصل الدراسي / المرحلة *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGradeDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showGradeDropdown,
                        onDismissRequest = { showGradeDropdown = false }
                    ) {
                        DeaconGrade.values().forEach { g ->
                            DropdownMenuItem(
                                text = { Text("${g.arabicTitle} - ${g.stage}") },
                                onClick = {
                                    grade = g
                                    showGradeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("تاريخ الميلاد (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )

                    // Rank Selection
                    Box(modifier = Modifier.weight(1f)) {
                        var showRankDropdown by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = rank.arabicTitle,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الرتبة") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRankDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showRankDropdown,
                            onDismissRequest = { showRankDropdown = false }
                        ) {
                            DeaconRank.values().forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.arabicTitle) },
                                    onClick = {
                                        rank = r
                                        showRankDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("هاتف الشماس") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("هاتف ولي الأمر *") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "العنوان التفصيلي للافتقاد الميداني بمير:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = governorate,
                        onValueChange = { governorate = it },
                        label = { Text("المحافظة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("المركز / المدينة") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("القرية / الحي") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("الشارع أو الحارة *") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("رقم العقار") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text("الدور") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("رابط الصورة الشخصية (URL)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات رعوية وخاصة") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    deacon.copy(
                                        name = name,
                                        grade = grade,
                                        dateOfBirth = birthDate,
                                        rank = rank,
                                        phone = phone,
                                        parentPhone = parentPhone,
                                        governorate = governorate,
                                        district = district,
                                        area = area,
                                        street = street,
                                        buildingNumber = building,
                                        floor = floor,
                                        photoUrl = photoUrl,
                                        pastoralNotes = notes
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModernPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isNew) "حفظ وإضافة" else "تحديث البيانات")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}
