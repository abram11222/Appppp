package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.DialogProperties
import com.example.repository.MergeDiff
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.LiturgicalBurgundy
import com.example.ui.theme.LiturgicalGold

@Composable
fun MergeRestoreDialog(
    currentDeaconCount: Int,
    onDismiss: () -> Unit,
    onPreviewDiff: (String) -> MergeDiff?,
    onExecuteMerge: (String) -> Boolean
) {
    var backupJsonInput by remember {
        mutableStateOf(
            """{
  "churchName": "كنيسة السيدة العذراء والشهيد مارجرجس",
  "deacons": [
    {
      "code128": "DK-2026-06",
      "name": "يوسف أشرف نبيل",
      "rank": "EPSALTOS",
      "phone": "01289654123",
      "governorate": "القاهرة",
      "district": "شبرا",
      "area": "الترعة البولاقية",
      "street": "شارع شكري",
      "buildingNumber": "14"
    },
    {
      "code128": "DK-2026-07",
      "name": "طوماس جورج فخري",
      "rank": "ANAGNOSTES",
      "phone": "01007894561",
      "governorate": "القاهرة",
      "district": "شبرا",
      "area": "الترعة البولاقية",
      "street": "شارع خلوصي",
      "buildingNumber": "9"
    }
  ]
}"""
        )
    }

    var diffResult by remember { mutableStateOf<MergeDiff?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successDone by remember { mutableStateOf(false) }

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
                .testTag("merge_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Merge,
                            contentDescription = null,
                            tint = LiturgicalBurgundy,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🔄 الدمج الذكي (Merge Restore)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استرجاع سحابي آمن دون حذف السجلات الحالية",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ميزة الدمج الذكي تضمن دمج المخدومين الجدد وتحديث الموجودين بالباركود مع الحفاظ التام على الحضور والدرجات السابقة (بياناتك + النسخة = دمج).",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = backupJsonInput,
                    onValueChange = {
                        backupJsonInput = it
                        diffResult = null
                        errorMessage = null
                    },
                    label = { Text("بيانات النسخة الاحتياطية (JSON)") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action: Preview Diff
                Button(
                    onClick = {
                        val diff = onPreviewDiff(backupJsonInput)
                        if (diff != null) {
                            diffResult = diff
                            errorMessage = null
                        } else {
                            errorMessage = "صيغة البيانات غير صحيحة، يرجى مراجعة نص الـ JSON."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("👁️ معاينة الفروقات قبل الاسترجاع")
                }

                // Diff Preview Card
                diffResult?.let { diff ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📊 نتيجة الفحص والمعاينة:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• السجلات الحالية بالنظام: ${diff.existingCount} مخدوم",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF166534))
                            )
                            Text(
                                text = "• مخدومين جدد سيتم إضافتهم: ${diff.newRecordsCount} مخدوم",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GreenSuccess)
                            )
                            Text(
                                text = "• سجلات مشتركة سيتم تحديثها: ${diff.updatedRecordsCount}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF166534))
                            )

                            if (diff.sampleNewNames.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "أمثلة: ${diff.sampleNewNames.joinToString("، ")}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Execute Merge button
                    Button(
                        onClick = {
                            val ok = onExecuteMerge(backupJsonInput)
                            if (ok) {
                                successDone = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Merge, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعتماد وتنفيذ الدمج الذكي الآن")
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                    )
                }

                if (successDone) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎉 تم الدمج بنجاح وتحديث القاعدة السحابية!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GreenSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
