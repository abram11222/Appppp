package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Deacon
import com.example.model.ExamResult
import com.example.model.GospelSchedule
import com.example.model.HymnCurriculum
import com.example.model.HymnRecitation
import com.example.ui.components.DeaconAvatar
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.LiturgicalBurgundy
import com.example.ui.theme.LiturgicalGold

@Composable
fun CurriculumScreen(
    deacons: List<Deacon>,
    hymnCurriculums: List<HymnCurriculum>,
    recitations: List<HymnRecitation>,
    gospelSchedules: List<GospelSchedule>,
    examResults: List<ExamResult>,
    onSaveHymnCurriculum: (HymnCurriculum) -> Unit,
    onSaveRecitation: (HymnRecitation) -> Boolean,
    onSaveGospel: (GospelSchedule) -> Boolean,
    onSaveExam: (ExamResult) -> Boolean
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("🎶 الألحان", "📖 الإنجيل", "📝 الامتحانات", "📌 المطلوب هذا الشهر")

    var showAddRecitationDialog by remember { mutableStateOf(false) }
    var showAddGospelDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showAddCurriculumDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> HymnsTab(
                    deacons = deacons,
                    curriculums = hymnCurriculums,
                    recitations = recitations,
                    onOpenAddCurriculum = { showAddCurriculumDialog = true },
                    onOpenAddRecitation = { showAddRecitationDialog = true }
                )
                1 -> GospelTab(
                    deacons = deacons,
                    gospelSchedules = gospelSchedules,
                    onOpenAddGospel = { showAddGospelDialog = true }
                )
                2 -> ExamsTab(
                    deacons = deacons,
                    examResults = examResults,
                    onOpenAddExam = { showAddExamDialog = true }
                )
                3 -> MonthlySummaryTab(
                    deacons = deacons,
                    recitations = recitations,
                    gospelSchedules = gospelSchedules
                )
            }
        }
    }

    // Modal: Add Hymn Recitation
    if (showAddRecitationDialog) {
        AddRecitationDialog(
            deacons = deacons,
            curriculums = hymnCurriculums,
            onDismiss = { showAddRecitationDialog = false },
            onSave = { rec ->
                onSaveRecitation(rec)
                showAddRecitationDialog = false
            }
        )
    }

    // Modal: Add Gospel Schedule
    if (showAddGospelDialog) {
        AddGospelDialog(
            deacons = deacons,
            onDismiss = { showAddGospelDialog = false },
            onSave = { g ->
                onSaveGospel(g)
                showAddGospelDialog = false
            }
        )
    }

    // Modal: Add Exam Result
    if (showAddExamDialog) {
        AddExamDialog(
            deacons = deacons,
            onDismiss = { showAddExamDialog = false },
            onSave = { ex ->
                onSaveExam(ex)
                showAddExamDialog = false
            }
        )
    }

    // Modal: Add Hymn Curriculum
    if (showAddCurriculumDialog) {
        AddHymnCurriculumDialog(
            onDismiss = { showAddCurriculumDialog = false },
            onSave = { c ->
                onSaveHymnCurriculum(c)
                showAddCurriculumDialog = false
            }
        )
    }
}

@Composable
fun HymnsTab(
    deacons: List<Deacon>,
    curriculums: List<HymnCurriculum>,
    recitations: List<HymnRecitation>,
    onOpenAddCurriculum: () -> Unit,
    onOpenAddRecitation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onOpenAddRecitation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تسميع لحن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenAddCurriculum,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مقرر جديد", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recitations) { rec ->
                val deacon = deacons.find { it.id == rec.deaconId }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeaconAvatar(
                            photoUrl = deacon?.photoUrl ?: "",
                            name = deacon?.name ?: "شماس",
                            sizeDp = 44
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deacon?.name ?: "شماس",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "🎶 ${rec.hymnTitle}",
                                style = MaterialTheme.typography.bodySmall.copy(color = LiturgicalBurgundy)
                            )
                            Text(
                                text = "تاريخ التسميع: ${rec.recitationDate} • الخادم: ${rec.examinerName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
                            )
                            if (rec.notes.isNotBlank()) {
                                Text(
                                    text = "ملاحظة: ${rec.notes}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.DarkGray)
                                )
                            }
                        }

                        // Score Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (rec.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${rec.score} / ${rec.maxScore}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (rec.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                )
                                Text(
                                    text = if (rec.isPassed) "مجاز ✓" else "يحتاج إعادة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = if (rec.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GospelTab(
    deacons: List<Deacon>,
    gospelSchedules: List<GospelSchedule>,
    onOpenAddGospel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onOpenAddGospel,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("جدولة حفظ إنجيل لشماس", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gospelSchedules) { g ->
                val deacon = deacons.find { it.id == g.deaconId }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeaconAvatar(
                            photoUrl = deacon?.photoUrl ?: "",
                            name = deacon?.name ?: "",
                            sizeDp = 42
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deacon?.name ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "📖 ${g.passageRef}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "المهلة المحددة: ${g.deadlineDate}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (g.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (g.isCompleted) "تم الحفظ (${g.score}%)" else "قيد الحفظ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (g.isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamsTab(
    deacons: List<Deacon>,
    examResults: List<ExamResult>,
    onOpenAddExam: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onOpenAddExam,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("رصد نتيجة اختبار طقسي / شماسي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(examResults) { ex ->
                val deacon = deacons.find { it.id == ex.deaconId }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeaconAvatar(
                            photoUrl = deacon?.photoUrl ?: "",
                            name = deacon?.name ?: "",
                            sizeDp = 42
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = deacon?.name ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "📝 ${ex.examTitle}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "تاريخ الاختبار: ${ex.date} • المصحح: ${ex.examiner}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color.Gray)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LiturgicalGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${ex.score} / ${ex.maxScore}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryTab(
    deacons: List<Deacon>,
    recitations: List<HymnRecitation>,
    gospelSchedules: List<GospelSchedule>
) {
    val overdueRecitations = recitations.filter { !it.isPassed && it.deadlineDate < "2026-09-17" }
    val overdueGospels = gospelSchedules.filter { !it.isCompleted && it.deadlineDate < "2026-09-17" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⏱️ تنبيهات مدد التسميع المتأخرة لهذا الشهر",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يوجد ${overdueRecitations.size} لحن و ${overdueGospels.size} حفظ إنجيل تجاوزوا المهلة المحددة ويحتاجون تنبيهاً أثناء الخدمة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                }
            }
        }

        item {
            Text(
                text = "📌 المطلوب متابعته هذا الشهر (سبتمبر 2026):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(overdueRecitations) { rec ->
            val deacon = deacons.find { it.id == rec.deaconId }
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${deacon?.name}: متأخر في لحن [${rec.hymnTitle}]",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "المهلة المحددة كانت: ${rec.deadlineDate}",
                            style = MaterialTheme.typography.labelSmall.copy(color = AmberWarning)
                        )
                    }
                }
            }
        }

        items(overdueGospels) { g ->
            val deacon = deacons.find { it.id == g.deaconId }
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${deacon?.name}: متأخر في حفظ [${g.passageRef}]",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "المهلة المحددة كانت: ${g.deadlineDate}",
                            style = MaterialTheme.typography.labelSmall.copy(color = AmberWarning)
                        )
                    }
                }
            }
        }
    }
}

// Dialogs for Adding Recitation, Gospel, Exam, and Curriculum
@Composable
fun AddRecitationDialog(
    deacons: List<Deacon>,
    curriculums: List<HymnCurriculum>,
    onDismiss: () -> Unit,
    onSave: (HymnRecitation) -> Unit
) {
    var selectedDeacon by remember { mutableStateOf(deacons.firstOrNull()) }
    var selectedHymn by remember { mutableStateOf(curriculums.firstOrNull()) }
    var score by remember { mutableStateOf("90") }
    var notes by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("2026-09-30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🎶 تسميع لحن ورصد درجة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))

                // Deacon selector
                Text(text = "اختر الشماس:", style = MaterialTheme.typography.labelSmall)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var showDeaconMenu by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = selectedDeacon?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeaconMenu = true }
                    )
                    DropdownMenu(expanded = showDeaconMenu, onDismissRequest = { showDeaconMenu = false }) {
                        deacons.forEach { d ->
                            DropdownMenuItem(text = { Text(d.name) }, onClick = { selectedDeacon = d; showDeaconMenu = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hymn selector
                Text(text = "اختر اللحن:", style = MaterialTheme.typography.labelSmall)
                Box(modifier = Modifier.fillMaxWidth()) {
                    var showHymnMenu by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = selectedHymn?.title ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHymnMenu = true }
                    )
                    DropdownMenu(expanded = showHymnMenu, onDismissRequest = { showHymnMenu = false }) {
                        curriculums.forEach { h ->
                            DropdownMenuItem(text = { Text(h.title) }, onClick = { selectedHymn = h; showHymnMenu = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("الدرجة (من 100)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الأداء الطقسي والتصريف") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val sc = score.toIntOrNull() ?: 0
                            if (selectedDeacon != null && selectedHymn != null) {
                                onSave(
                                    HymnRecitation(
                                        deaconId = selectedDeacon!!.id,
                                        hymnId = selectedHymn!!.id,
                                        hymnTitle = selectedHymn!!.title,
                                        score = sc,
                                        isPassed = sc >= 70,
                                        deadlineDate = deadline,
                                        recitationDate = "2026-09-17",
                                        examinerName = "الخادم المسؤول",
                                        notes = notes
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("رصد الدرجة سحابياً")
                    }

                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun AddGospelDialog(
    deacons: List<Deacon>,
    onDismiss: () -> Unit,
    onSave: (GospelSchedule) -> Unit
) {
    var selectedDeacon by remember { mutableStateOf(deacons.firstOrNull()) }
    var passageRef by remember { mutableStateOf("إنجيل يوحنا 1: 1-14") }
    var deadline by remember { mutableStateOf("2026-09-30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📖 جدولة حفظ إنجيل", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = passageRef,
                    onValueChange = { passageRef = it },
                    label = { Text("الشاهد من الكتاب المقدس") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("المهلة المحددة (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (selectedDeacon != null && passageRef.isNotBlank()) {
                                onSave(
                                    GospelSchedule(
                                        deaconId = selectedDeacon!!.id,
                                        passageRef = passageRef,
                                        deadlineDate = deadline,
                                        isCompleted = false
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }

                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExamDialog(
    deacons: List<Deacon>,
    onDismiss: () -> Unit,
    onSave: (ExamResult) -> Unit
) {
    var selectedDeacon by remember { mutableStateOf(deacons.firstOrNull()) }
    var title by remember { mutableStateOf("اختبار طقس رفع بخور عشية وباكر") }
    var score by remember { mutableStateOf("95") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📝 رصد نتيجة اختبار", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الاختبار") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("الدرجة (من 100)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (selectedDeacon != null) {
                                onSave(
                                    ExamResult(
                                        deaconId = selectedDeacon!!.id,
                                        examTitle = title,
                                        score = score.toIntOrNull() ?: 0,
                                        date = "2026-09-17",
                                        examiner = "لجنة الشمامسة"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("رصد")
                    }

                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun AddHymnCurriculumDialog(
    onDismiss: () -> Unit,
    onSave: (HymnCurriculum) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("سنوي") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🎶 إضافة مقرر لحن جديد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم اللحن (مثل: لحن تين أو أو إشت)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("الموسم الطقسي (سنوي / كيهكي / صوم)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(HymnCurriculum(title = title, season = season))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة")
                    }

                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f)) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}
