package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DeaconRank
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.repository.SyncState
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.ModernPrimary
import com.example.ui.theme.ModernPrimaryContainer
import com.example.ui.theme.ModernSuccess
import com.example.ui.theme.ModernSuccessContainer
import com.example.ui.theme.ModernWarning
import com.example.ui.theme.ModernWarningContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchTopAppBar(
    currentServant: ServantAccount?,
    syncState: SyncState,
    onDashboardClick: () -> Unit,
    onSwitchServantClick: () -> Unit,
    onTestNotificationClick: () -> Unit,
    onOpenCloudSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = TextPrimary
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SaintStephenLogoEmblem(
                        size = 38.dp,
                        showOuterRing = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "خدمة الشهيد استفانوس",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Clean status pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (syncState == SyncState.CONNECTED_LIVE) ModernSuccessContainer else ModernWarningContainer,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (syncState == SyncState.CONNECTED_LIVE) Color(0xFFBBF7D0) else Color(0xFFFED7AA)
                                ),
                                modifier = Modifier.clickable { onOpenCloudSettings() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (syncState == SyncState.SYNCING) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                        contentDescription = "سحابة Firebase",
                                        tint = if (syncState == SyncState.CONNECTED_LIVE) ModernSuccess else ModernWarning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (syncState == SyncState.CONNECTED_LIVE) "Firebase متصل" else "مزامنة...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (syncState == SyncState.CONNECTED_LIVE) ModernSuccess else ModernWarning
                                    )
                                }
                            }
                        }
                        Text(
                            text = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = {
                // Dashboard button if Admin
                if (currentServant?.role == ServantRole.ADMIN) {
                    IconButton(
                        onClick = onDashboardClick,
                        modifier = Modifier.testTag("top_bar_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "لوحة التحكم",
                            tint = ModernPrimary
                        )
                    }
                }

                // Notification test button
                IconButton(
                    onClick = onTestNotificationClick,
                    modifier = Modifier.testTag("top_bar_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "تذكير الحضور",
                        tint = TextSecondary
                    )
                }

                // Active Servant badge (click to switch or view)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ModernPrimaryContainer,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clickable { onSwitchServantClick() }
                        .testTag("top_bar_servant_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (currentServant?.role == ServantRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            tint = ModernPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentServant?.name?.split(" ")?.firstOrNull() ?: "خادم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ModernPrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Logout button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("top_bar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "تسجيل خروج",
                        tint = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
fun DeaconAvatar(
    photoUrl: String,
    name: String,
    sizeDp: Int = 46,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(sizeDp.dp)
            .border(1.dp, CleanBorder, CircleShape),
        shape = CircleShape,
        color = ModernPrimaryContainer
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "ش",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ModernPrimary,
                        fontSize = (sizeDp * 0.42).sp
                    )
                )
            }
        }
    }
}

@Composable
fun RankBadge(rank: DeaconRank, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorder),
        modifier = modifier
    ) {
        Text(
            text = rank.arabicTitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = ModernPrimary,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun MissingDataBanner(
    incompleteCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = incompleteCount > 0) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ModernWarningContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .clickable { onClick() }
                .testTag("missing_data_banner")
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ModernWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "يوجد $incompleteCount مخدوم بياناتهم غير مكتملة (العنوان أو الهاتف). اضغط للاستكمال السريع.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF92400E),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
