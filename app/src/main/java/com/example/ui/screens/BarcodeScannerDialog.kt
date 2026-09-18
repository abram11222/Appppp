package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Deacon
import com.example.ui.components.DeaconAvatar
import com.example.ui.components.RankBadge
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.LiturgicalBurgundy
import com.example.ui.theme.LiturgicalGold

@Composable
fun BarcodeScannerDialog(
    deacons: List<Deacon>,
    onDismiss: () -> Unit,
    onAttendanceMarked: (deaconId: String, isService: Boolean, isMass: Boolean) -> Unit,
    onOpenProfile: (Deacon) -> Unit
) {
    var scannedCodeInput by remember { mutableStateOf("") }
    var matchedDeacon by remember { mutableStateOf<Deacon?>(null) }
    var actionDoneMessage by remember { mutableStateOf<String?>(null) }

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
                .testTag("barcode_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = LiturgicalBurgundy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📷 ماسح الباركود السريع (Code 128)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scanner Viewfinder Mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, LiturgicalGold.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "عدسة المسح",
                            tint = LiturgicalGold,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "وجّه الكاميرا إلى باركود الكارنيه لتسجيل الحضور فوراً",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Barcode Manual Input / Fast selector
                OutlinedTextField(
                    value = scannedCodeInput,
                    onValueChange = { input ->
                        scannedCodeInput = input
                        actionDoneMessage = null
                        matchedDeacon = deacons.find { it.code128.equals(input.trim(), ignoreCase = true) }
                    },
                    label = { Text("أدخل أو امسح كود الشماس (مثل: DK-2026-01)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Chips of registered barcodes
                Text(
                    text = "أو اختر شماس لاختبار القراءة السريعة:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(deacons.take(5)) { deacon ->
                        FilterChip(
                            selected = matchedDeacon?.id == deacon.id,
                            onClick = {
                                scannedCodeInput = deacon.code128
                                matchedDeacon = deacon
                                actionDoneMessage = null
                            },
                            label = { Text("${deacon.name.take(12)} (${deacon.code128})", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Matched Deacon Card
                matchedDeacon?.let { deacon ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DeaconAvatar(
                                    photoUrl = deacon.photoUrl,
                                    name = deacon.name,
                                    sizeDp = 48
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = deacon.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    RankBadge(rank = deacon.rank)
                                    Text(
                                        text = "الباركود: ${deacon.code128}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onAttendanceMarked(deacon.id, true, true)
                                        actionDoneMessage = "✅ تم تسجيل حضور القداس والخدمة للشماس ${deacon.name}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تسجيل حضور ⚡", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        onOpenProfile(deacon)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LiturgicalBurgundy),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("فتح الملف 👤", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                actionDoneMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GreenSuccess,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
