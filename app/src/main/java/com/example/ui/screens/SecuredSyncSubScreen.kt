package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.security.DriveSyncStatus
import com.example.ui.components.AutoShrinkButtonText
import com.example.ui.components.DriveSyncPurchaseDialog
import com.example.ui.components.ProBadge
import com.example.ui.components.SubscriptionPlansDialog
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceGreenLight
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.FinanceRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceInnerCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecuredSyncSubScreen(
    viewModel: LoanViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val driveState by viewModel.driveSyncState.collectAsStateWithLifecycle()
    val localInfo by viewModel.localSecuredSpaceInfo.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isDriveAccessible by viewModel.isDriveSyncAccessible.collectAsStateWithLifecycle()
    val isPro by viewModel.isProPlan.collectAsStateWithLifecycle()
    val drivePurchased by viewModel.driveSyncPurchasedInFree.collectAsStateWithLifecycle()
    val planTier by viewModel.currentPlanTier.collectAsStateWithLifecycle()
    val billingCycle by viewModel.currentBillingCycle.collectAsStateWithLifecycle()

    var showConfirmRestoreLocal by remember { mutableStateOf(false) }
    var showConfirmRestoreDrive by remember { mutableStateOf(false) }
    var showDrivePermissionDialog by remember { mutableStateOf(false) }
    var showDrivePurchaseDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var emailInput by remember(driveState.accountEmail) { mutableStateOf(driveState.accountEmail) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundClean)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("secured_sync_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Settings",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Secured Space & Cloud Sync",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hardware encryption & automated Google Drive backup",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. High-Security Shield Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_shield_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3348BB78)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF48BB78),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Anti-Data Loss Security Architecture",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (driveState.isDriveSyncActivated) {
                                        "Local Hardware Vault + Google Drive Sync Active"
                                    } else {
                                        "Phone Hardware Vault Active (Optional Google Drive Sync)"
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0x33FFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SecurityChip(
                                title = "AES-256-GCM",
                                subtitle = "Hardware Key",
                                modifier = Modifier.weight(1f)
                            )
                            SecurityChip(
                                title = "SHA-256",
                                subtitle = "Tamper Proof",
                                modifier = Modifier.weight(1f)
                            )
                            SecurityChip(
                                title = if (driveState.isDriveSyncActivated) "Cloud Synced" else "Optional Sync",
                                subtitle = if (driveState.isDriveSyncActivated) "Google Drive" else "Drive Inactive",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Section 1: Local Phone Secured Space Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_secured_space_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderCard))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Local Phone Secured Space",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Sandboxed in app-private storage",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FinanceGreenLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FinanceGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Details Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceInnerCard)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(
                            label = "Security Standard",
                            value = localInfo.encryptionStandard
                        )
                        DetailRow(
                            label = "Integrity Checksum",
                            value = localInfo.integrityStatus
                        )
                        DetailRow(
                            label = "Last Local Backup",
                            value = if (localInfo.lastBackupTimestamp > 0L) {
                                dateFormat.format(Date(localInfo.lastBackupTimestamp))
                            } else "Not backed up yet"
                        )
                        if (localInfo.backupFileExists) {
                            DetailRow(
                                label = "Encrypted Size",
                                value = "${(localInfo.backupSizeBytes / 1024) + 1} KB (${localInfo.totalRecordsBackedUp} records)"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons for Local
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.backupToLocalSecuredSpace { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("backup_local_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoShrinkButtonText(
                                text = "Backup Locally",
                                maxFontSize = 13.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { showConfirmRestoreLocal = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("restore_local_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderCard)
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            AutoShrinkButtonText(
                                text = "Restore Local",
                                maxFontSize = 13.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. Section 2: Google Drive Cloud Sync Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_drive_sync_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderCard))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Drive Cloud Sync",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Zero data loss cloud protection",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (driveState.isDriveSyncActivated) FinanceGreenLight else SurfaceInnerCard)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (driveState.isDriveSyncActivated) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (driveState.isDriveSyncActivated) FinanceGreen else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (driveState.isDriveSyncActivated) {
                                        "Sync Active"
                                    } else if (isPro) {
                                        "Included in Pro"
                                    } else if (drivePurchased) {
                                        "Unlocked (₹59 One-Time)"
                                    } else {
                                        "₹59 One-Time or Pro"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (driveState.isDriveSyncActivated) FinanceGreen else if (isPro || drivePurchased) BluePrimary else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isDriveAccessible) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Google Drive Safe Backup",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "₹59 One-time purchase in Free Tier, or included with continuous Auto-Sync in Pro Plan.",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showDrivePurchaseDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = FinanceGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Unlock for ₹59", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showSubscriptionDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("View Pro Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Optional Drive Sync Activation Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (driveState.isDriveSyncActivated) BlueLight else SurfaceInnerCard)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Activate Google Drive Sync",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (!isDriveAccessible) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ProBadge(text = "₹59 / PRO")
                                }
                            }
                            Text(
                                text = if (driveState.isDriveSyncActivated) {
                                    "Drive sync is active. Backups sync with your Google Drive AppData folder."
                                } else if (!isDriveAccessible) {
                                    "Requires ₹59 one-time unlock in Free Tier, or included in Pro Plan."
                                } else {
                                    "Optional cloud backup. Turn on to synchronize local storage with Google Drive."
                                },
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = driveState.isDriveSyncActivated,
                            onCheckedChange = { activated ->
                                if (activated) {
                                    if (!isDriveAccessible) {
                                        showDrivePurchaseDialog = true
                                    } else {
                                        showDrivePermissionDialog = true
                                    }
                                } else {
                                    viewModel.setDriveSyncActivated(false) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("activate_drive_sync_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1A73E8)
                            )
                        )
                    }

                    if (driveState.isDriveSyncActivated) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Connected Account Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceInnerCard)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Google Workspace / Drive Account", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = driveState.accountEmail,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Target: Google Drive AppData (Protected)",
                                    fontSize = 11.sp,
                                    color = BluePrimary
                                )
                            }
                            IconButton(
                                onClick = { showEditEmailDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Account", tint = BluePrimary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto Sync Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceInnerCard)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Sync to Google Drive",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Automatically upload encrypted snapshot whenever loans or payments are recorded",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = driveState.isAutoSyncEnabled,
                                onCheckedChange = { viewModel.toggleAutoSync(it) },
                                modifier = Modifier.testTag("auto_sync_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BluePrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sync Status Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (driveState.status) {
                                        DriveSyncStatus.ERROR -> FinanceRedLight
                                        DriveSyncStatus.SUCCESS -> FinanceGreenLight
                                        else -> SurfaceInnerCard
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = BluePrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = when (driveState.status) {
                                            DriveSyncStatus.SUCCESS -> Icons.Default.CheckCircle
                                            DriveSyncStatus.ERROR -> Icons.Default.Warning
                                            else -> Icons.Default.CloudSync
                                        },
                                        contentDescription = null,
                                        tint = when (driveState.status) {
                                            DriveSyncStatus.SUCCESS -> FinanceGreen
                                            DriveSyncStatus.ERROR -> FinanceRed
                                            else -> BluePrimary
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isSyncing) "Encrypting & Syncing to Google Drive..." else driveState.lastSyncMessage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (driveState.status) {
                                            DriveSyncStatus.SUCCESS -> FinanceGreen
                                            DriveSyncStatus.ERROR -> FinanceRed
                                            else -> TextPrimary
                                        }
                                    )
                                    if (driveState.lastSyncTimestamp > 0L) {
                                        Text(
                                            text = "Last synced: ${dateFormat.format(Date(driveState.lastSyncTimestamp))}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons for Google Drive
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.syncToGoogleDrive { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isSyncing,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("sync_google_drive_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoShrinkButtonText(
                                    text = "Sync to Drive",
                                    maxFontSize = 13.sp,
                                    minFontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { showConfirmRestoreDrive = true },
                                enabled = !isSyncing,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("restore_google_drive_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(BorderCard)
                                )
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoShrinkButtonText(
                                    text = "Restore Drive",
                                    maxFontSize = 13.sp,
                                    minFontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Notice when Drive sync is off
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceInnerCard)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = FinanceGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Your records are secured locally on this phone. Google Drive sync is optional — activate it anytime above to enable automated off-device backups.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                showDrivePermissionDialog = true
                            },
                            enabled = !isSyncing,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("activate_and_sync_drive_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoShrinkButtonText(
                                text = "Activate & Sync to Google Drive",
                                maxFontSize = 13.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 5. Explainer Card: High Security Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How FlowVault Protects Your Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Local-First Privacy: All borrower ledgers, loan schedules, and virtual accounts remain encrypted on your device inside an app-private sandbox.\n\n" +
                               "• Authenticated Encryption: Every snapshot is secured using AES/GCM/NoPadding-256 with an isolated random initialization vector for every backup.\n\n" +
                               "• Tamper-Proof Checksums: A cryptographic SHA-256 digest guarantees that corrupted or modified data will never overwrite your records.\n\n" +
                               "• Zero Cloud Plaintext: Google Drive only receives pre-encrypted cipher packages saved directly in your private AppData directory.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Dialog: Confirm Restore from Local
    if (showConfirmRestoreLocal) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreLocal = false },
            title = { Text("Restore Local Encrypted Backup?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will restore your loans, borrowers, accounts, and transactions from the phone's secured vault. Any unsaved modifications since the last backup will be replaced."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreLocal = false
                        viewModel.restoreFromLocalSecuredSpace { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmRestoreLocal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Confirm Restore from Google Drive
    if (showConfirmRestoreDrive) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDrive = false },
            title = { Text("Restore from Google Drive?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will download and decrypt the latest backup from your Google Drive AppData folder, verifying its SHA-256 checksum before updating your database."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreDrive = false
                        viewModel.restoreFromGoogleDrive { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                ) {
                    Text("Download & Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmRestoreDrive = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Google Drive Permission Request (Only shown when activating Drive sync)
    if (showDrivePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showDrivePermissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Google Drive Permission?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "FlowVault requires permission to create and store encrypted ledger backups in your Google Drive AppData folder.",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceInnerCard)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Restricted Scope: Drive AppData only", fontSize = 11.sp, color = FinanceGreen, fontWeight = FontWeight.SemiBold)
                            Text("• Zero access to your personal files, docs, or photos", fontSize = 11.sp, color = TextSecondary)
                            Text("• End-to-end AES-256 encrypted before leaving device", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Text(
                        "Target Account: ${driveState.accountEmail}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDrivePermissionDialog = false
                        viewModel.setDriveSyncActivated(true) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                ) {
                    Text("Grant & Activate")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDrivePermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Edit Google Account Email
    if (showEditEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            title = { Text("Google Account for Drive Sync", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the Google account email associated with your Google Drive backup:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("admin@example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderCard
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            viewModel.updateGoogleAccountEmail(emailInput)
                            showEditEmailDialog = false
                            Toast.makeText(context, "Google Drive account updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Drive Sync One-Time Purchase for Free Tier (Rs 59)
    if (showDrivePurchaseDialog) {
        DriveSyncPurchaseDialog(
            onDismiss = { showDrivePurchaseDialog = false },
            onConfirmPurchase = {
                viewModel.purchaseDriveSyncInFree {
                    showDrivePurchaseDialog = false
                    Toast.makeText(context, "Google Drive Safe Backup unlocked for ₹59!", Toast.LENGTH_SHORT).show()
                    showDrivePermissionDialog = true
                }
            },
            onUpgradeToPro = {
                showDrivePurchaseDialog = false
                showSubscriptionDialog = true
            }
        )
    }

    // Dialog: Full Subscription Plans
    if (showSubscriptionDialog) {
        SubscriptionPlansDialog(
            currentPlanTier = planTier,
            currentBillingCycle = billingCycle,
            driveSyncPurchasedInFree = drivePurchased,
            onDismiss = { showSubscriptionDialog = false },
            onSelectPro = { cycle ->
                viewModel.upgradeToPro(cycle)
                Toast.makeText(context, "Upgraded to FlowVault Pro (${cycle.cycleName})!", Toast.LENGTH_SHORT).show()
            },
            onSelectFree = {
                viewModel.downgradeToFree()
                Toast.makeText(context, "Switched to Free Tier", Toast.LENGTH_SHORT).show()
            },
            onPurchaseDriveSync = {
                viewModel.purchaseDriveSyncInFree {
                    Toast.makeText(context, "Google Drive Safe Backup unlocked for ₹59!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun SecurityChip(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FFFFFF))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF68D391),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color(0xFFCBD5E0),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
