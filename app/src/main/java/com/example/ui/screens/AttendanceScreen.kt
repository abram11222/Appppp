package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AttendanceRecord
import com.example.model.AttendanceTarget
import com.example.model.Deacon
import com.example.ui.components.DeaconAvatar
import com.example.ui.components.RankBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.LiturgicalBurgundy
import com.example.ui.theme.LiturgicalGold

@Composable
fun AttendanceScreen(
    deacons: List<Deacon>,
    attendanceList: List<AttendanceRecord>,
    target: AttendanceTarget,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onToggleAttendance: (deaconId: String, isService: Boolean, isMass: Boolean) -> Unit,
    onMarkAllPresent: (isService: Boolean, isMass: Boolean) -> Unit,
    onUpdateTargets: (serviceTarget: Int, massTarget: Int) -> Unit,
    onOpenBarcodeScanner: () -> Unit
) {
    var showTargetEditor by remember { mutableStateOf(false) }

    // Check if Friday
    val isFriday = try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance().apply { time = sdf.parse(selectedDate) ?: java.util.Date() }
        cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
    } catch (_: Exception) {
        true
    }

    // Attendance stats for selected date
    val dayRecords = attendanceList.filter { it.date == selectedDate }
    val totalDeacons = deacons.size.coerceAtLeast(1)
    val servicePresentCount = dayRecords.count { it.isServicePresent }
    val massPresentCount = dayRecords.count { it.isMassPresent }
    val serviceActualPercent = (servicePresentCount.toFloat() / totalDeacons * 100).toInt()
    val massActualPercent = (massPresentCount.toFloat() / totalDeacons * 100).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Date Selection Header with Friday Badge
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تاريخ التسجيل: $selectedDate",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // Friday Special Distinction Badge
                        if (isFriday) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "خدمة الجمعة الرسمية",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fast Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onMarkAllPresent(true, true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_all_present_button")
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل سريع (الكل حضور)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showTargetEditor = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(imageVector = Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الأهداف", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Targets vs Actual Progress Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Service Target Progress
                TargetProgressCard(
                    title = "حضور الخدمة",
                    actual = serviceActualPercent,
                    target = target.serviceTargetPercent,
                    count = "$servicePresentCount / ${deacons.size}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Mass Target Progress
                TargetProgressCard(
                    title = "حضور القداس",
                    actual = massActualPercent,
                    target = target.massTargetPercent,
                    count = "$massPresentCount / ${deacons.size}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deacons Attendance List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deacons, key = { it.id }) { deacon ->
                    val record = dayRecords.find { it.deaconId == deacon.id }
                    val isService = record?.isServicePresent ?: false
                    val isMass = record?.isMassPresent ?: false

                    AttendanceItemRow(
                        deacon = deacon,
                        isService = isService,
                        isMass = isMass,
                        onToggleService = { onToggleAttendance(deacon.id, !isService, isMass) },
                        onToggleMass = { onToggleAttendance(deacon.id, isService, !isMass) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // FAB to open Barcode Scanner
        FloatingActionButton(
            onClick = onOpenBarcodeScanner,
            containerColor = LiturgicalGold,
            contentColor = Color(0xFF2C1800),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("open_barcode_scanner_fab")
        ) {
            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "مسح الباركود")
        }
    }

    // Target Editor Dialog
    if (showTargetEditor) {
        Dialog(onDismissRequest = { showTargetEditor = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                var newServiceTarget by remember { mutableStateOf(target.serviceTargetPercent.toFloat()) }
                var newMassTarget by remember { mutableStateOf(target.massTargetPercent.toFloat()) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 تحديد أهداف الحضور المستهدفة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "نسبة حضور الخدمة المستهدفة: ${newServiceTarget.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = newServiceTarget,
                        onValueChange = { newServiceTarget = it },
                        valueRange = 40f..100f,
                        steps = 11
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "نسبة حضور القداسات المستهدفة: ${newMassTarget.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = newMassTarget,
                        onValueChange = { newMassTarget = it },
                        valueRange = 40f..100f,
                        steps = 11
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdateTargets(newServiceTarget.toInt(), newMassTarget.toInt())
                                showTargetEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ الأهداف")
                        }

                        Button(
                            onClick = { showTargetEditor = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TargetProgressCard(
    title: String,
    actual: Int,
    target: Int,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$actual%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                )
                Text(
                    text = "المستهدف $target%",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (actual / 100f).coerceIn(0f, 1f) },
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "الحاضرون: $count",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
            )
        }
    }
}

@Composable
fun AttendanceItemRow(
    deacon: Deacon,
    isService: Boolean,
    isMass: Boolean,
    onToggleService: () -> Unit,
    onToggleMass: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeaconAvatar(photoUrl = deacon.photoUrl, name = deacon.name, sizeDp = 44)
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deacon.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                RankBadge(rank = deacon.rank)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dual Toggle: Service vs Mass
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Service Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isService) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onToggleService() }
                        .border(
                            1.dp,
                            if (isService) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isService) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isService) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "الخدمة",
                            fontSize = 12.sp,
                            fontWeight = if (isService) FontWeight.Bold else FontWeight.Medium,
                            color = if (isService) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mass Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onToggleMass() }
                        .border(
                            1.dp,
                            if (isMass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isMass) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isMass) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "القداس",
                            fontSize = 12.sp,
                            fontWeight = if (isMass) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMass) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
