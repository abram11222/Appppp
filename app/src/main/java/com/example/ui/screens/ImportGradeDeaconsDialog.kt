package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.ModernAccent
import com.example.ui.theme.ModernAccentContainer
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryContainer
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.ModernSuccessContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ExcelParserHelper
import com.example.util.ExcelRowData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportGradeDeaconsDialog(
    initialGrade: DeaconGrade = DeaconGrade.PRIMARY_1,
    existingDeacons: List<Deacon> = emptyList(),
    onDismiss: () -> Unit,
    onImportDirectDeacons: (DeaconGrade, List<Deacon>) -> Unit,
    onImportTextLines: ((DeaconGrade, List<String>) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedGrade by remember { mutableStateOf(initialGrade) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var parsedRows by remember { mutableStateOf<List<ExcelRowData>>(emptyList()) }
    var isProcessingFile by remember { mutableStateOf(false) }
    var showManualTextInput by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }
    var showHelpInfo by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Modern 2026 File Picker for Excel (.xlsx, .xls) and CSV
    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingFile = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    val rows = withContext(Dispatchers.IO) {
                        ExcelParserHelper.parseFile(context, uri)
                    }
                    parsedRows = rows
                    selectedFileName = uri.lastPathSegment ?: "ملف إكسيل"
                    if (rows.isEmpty()) {
                        errorMessage = "لم نتمكن من قراءة أسماء من هذا الملف، تأكد من وجود عمود يحتوي على الأسماء"
                    }
                } catch (e: Exception) {
                    errorMessage = "حدث خطأ أثناء قراءة الملف: ${e.localizedMessage}"
                } finally {
                    isProcessingFile = false
                }
            }
        }
    }

    // Convert rows to actual Deacon models
    val convertedDeacons = remember(parsedRows, selectedGrade, existingDeacons) {
        ExcelParserHelper.convertToDeacons(parsedRows, selectedGrade, existingDeacons)
    }

    // Manual fallback lines if user chooses to type
    val manualLines = remember(manualText) {
        manualText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("//") }
    }

    val totalToImport = if (parsedRows.isNotEmpty()) convertedDeacons.size else manualLines.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp)
                .testTag("import_excel_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (2026 clean styling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ModernAccentContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = ModernAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "استيراد كشف إكسيل (Excel)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "رفع ملف كشف الشمامسة مباشرة (.xlsx / .csv)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. Grade Selection Section
                Text(
                    text = "١. حدد الفصل الدراسي للكشف المستورد:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DeaconGrade.values().forEach { grade ->
                        val isSelected = grade == selectedGrade
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ModernPrimary else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ModernPrimary else CleanBorder
                            ),
                            modifier = Modifier
                                .clickable { selectedGrade = grade }
                                .testTag("select_grade_${grade.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = grade.arabicTitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Upload Excel File Card (2026 Primary Action)
                Text(
                    text = "٢. رفع ملف الإكسيل (Excel):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (parsedRows.isNotEmpty()) ModernSuccess else CleanBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            excelLauncher.launch("*/*")
                        }
                        .testTag("upload_excel_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isProcessingFile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = ModernAccent,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "جارٍ قراءة وفحص ملف الإكسيل...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            )
                        } else if (parsedRows.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = ModernSuccessContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ModernSuccess,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تم تحميل ملف الإكسيل بنجاح!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            )
                            Text(
                                text = selectedFileName ?: "الملف المحدد",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تم استخراج ${parsedRows.size} مخدوم من الملف (جاهز للإضافة: ${convertedDeacons.size})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernPrimary,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { excelLauncher.launch("*/*") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تغيير الملف", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = null,
                                        tint = ModernAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "اضغط هنا لاختيار ملف الإكسيل من جهازك",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "يدعم ملفات .xlsx و .xls و .csv بكل سهولة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Preview Table of Parsed Excel Rows
                if (parsedRows.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "معاينة الأسماء المستخرجة من الإكسيل (${parsedRows.size}):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFAFAFA),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            parsedRows.take(5).forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${idx + 1}.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                            modifier = Modifier.width(20.dp)
                                        )
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (item.phone.isNotBlank()) {
                                        Text(
                                            text = item.phone,
                                            style = MaterialTheme.typography.labelSmall.copy(color = ModernAccent),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            if (parsedRows.size > 5) {
                                Text(
                                    text = "... والمزيد (${parsedRows.size - 5} اسم إضافي جاهز للاستيراد)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Optional fallback: manual text input
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showManualTextInput) "إخفاء الكتابة اليدوية" else "أو كتابة / لصق الأسماء يدوياً",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ModernAccent,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { showManualTextInput = !showManualTextInput }
                    )

                    IconButton(onClick = { showHelpInfo = !showHelpInfo }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "مساعدة",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showManualTextInput) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = { manualText = it },
                            placeholder = {
                                Text(
                                    text = "اكتب الاسم في كل سطر:\nمينا جرجس إبراهيم\nكيرلس ممدوح فايز, 01200000000",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ModernPrimary,
                                unfocusedBorderColor = CleanBorder
                            )
                        )
                    }
                }

                AnimatedVisibility(visible = showHelpInfo) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 نصيحة لملف الإكسيل:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "يكفي أن يحتوي ملف الإكسيل على عمود باسم الشماس (مثل: 'الاسم' أو 'اسم الشماس'). إن وجد عمود للتليفون أو الرتبة فسيتم قراءته تلقائياً.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (parsedRows.isNotEmpty() && convertedDeacons.isNotEmpty()) {
                                onImportDirectDeacons(selectedGrade, convertedDeacons)
                                onDismiss()
                            } else if (manualLines.isNotEmpty()) {
                                onImportTextLines?.invoke(selectedGrade, manualLines)
                                onDismiss()
                            }
                        },
                        enabled = totalToImport > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModernPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp)
                            .testTag("confirm_excel_import_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalToImport > 0) "تأكيد استيراد ($totalToImport) شماس" else "استيراد الكشف",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
