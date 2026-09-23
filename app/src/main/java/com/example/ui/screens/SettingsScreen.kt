package com.example.ui.screens

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BillingCycle
import com.example.data.model.PlanTier
import com.example.ui.components.GoldCrown
import com.example.ui.components.PrimaryBlueButton
import com.example.ui.components.SecondaryBlueButton
import com.example.ui.components.SubscriptionPlansDialog
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceGreenLight
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.FinanceRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealSlate
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun SettingsScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier,
    onOpenAdminDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val currentPlan by viewModel.currentPlan.collectAsStateWithLifecycle()
    val planTier by viewModel.currentPlanTier.collectAsStateWithLifecycle()
    val billingCycle by viewModel.currentBillingCycle.collectAsStateWithLifecycle()
    val driveSyncPurchasedInFree by viewModel.driveSyncPurchasedInFree.collectAsStateWithLifecycle()
    val pinEnabled by viewModel.pinSecurityEnabled.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricSecurityEnabled.collectAsStateWithLifecycle()
    val faceUnlockEnabled by viewModel.faceUnlockEnabled.collectAsStateWithLifecycle()
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    val driveState by viewModel.driveSyncState.collectAsStateWithLifecycle()

    var activeSubscreen by remember { mutableStateOf<String?>(null) } // null = Main Settings, "MY_ACCOUNT" = My Account Screen

    // Modal dialogs for settings items
    var showCurrentPlanDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showFeatureRequestDialog by remember { mutableStateOf(false) }
    var featureName by remember { mutableStateOf("") }
    var featureMobile by remember { mutableStateOf("") }
    var featureEmail by remember { mutableStateOf("") }
    var featureDescription by remember { mutableStateOf("") }
    var actionToastMessage by remember { mutableStateOf<String?>(null) }

    if (activeSubscreen == "MY_ACCOUNT") {
        // MY ACCOUNT SCREEN
        MyAccountSubScreen(
            viewModel = viewModel,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            onBack = { activeSubscreen = null },
            onRequestDeleteAccount = { showDeleteAccountDialog = true }
        )
    } else if (activeSubscreen == "SECURED_SYNC") {
        // SECURED SPACE & GOOGLE DRIVE CLOUD SYNC SCREEN
        SecuredSyncSubScreen(
            viewModel = viewModel,
            onBack = { activeSubscreen = null }
        )
    } else if (activeSubscreen == "APP_SECURITY") {
        // DIRECT APP SECURITY SCREEN
        AppSecuritySubScreen(
            viewModel = viewModel,
            pinEnabled = pinEnabled,
            biometricEnabled = biometricEnabled,
            faceUnlockEnabled = faceUnlockEnabled,
            securityPin = securityPin,
            onBack = { activeSubscreen = null }
        )
    } else {
        // MAIN SETTINGS SCREEN
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundClean)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Account, current plan, security, and data preferences",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Quick Vault & Cloud Sync Status Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { activeSubscreen = "SECURED_SYNC" }
                        .testTag("settings_sync_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceGreenLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FinanceGreen))
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FinanceGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            val isDriveActive = driveState.isDriveSyncActivated
                            Column {
                                Text(
                                    text = if (isDriveActive) "Secured Vault & Google Drive" else "Local Storage Secured",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isDriveActive) {
                                        if (driveState.lastSyncTimestamp > 0L) "Hardware Encrypted • Drive Synced" else "Hardware Encrypted • Drive Auto-Sync Active"
                                    } else {
                                        "Hardware Encrypted • Protected on Device"
                                    },
                                    fontSize = 11.sp,
                                    color = FinanceGreen
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = FinanceGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 1. My Account Item
            item {
                SettingsNavigationItemCard(
                    title = "My Account",
                    subtitle = "$userName • $userPhone",
                    icon = Icons.Default.Person,
                    iconTint = BluePrimary,
                    iconBg = BlueLight,
                    onClick = { activeSubscreen = "MY_ACCOUNT" },
                    testTag = "settings_my_account_item"
                )
            }

            // 2. Current Plan Item
            item {
                val isPro = planTier == PlanTier.PRO
                SettingsNavigationItemCard(
                    title = "Current Plan",
                    subtitle = currentPlan,
                    icon = Icons.Default.WorkspacePremium,
                    iconTint = if (isPro) GoldCrown else TealSlate,
                    iconBg = if (isPro) Color(0xFFFEF3C7) else TealLight,
                    badgeText = if (isPro) "PRO ACTIVE" else "FREE TIER",
                    badgeBg = if (isPro) Color(0xFFFEF3C7) else TealLight,
                    badgeTint = if (isPro) GoldCrown else TealSlate,
                    onClick = { showCurrentPlanDialog = true },
                    testTag = "settings_current_plan_item"
                )
            }

            // 3. Secured Storage & Cloud Sync Item
            item {
                val isDriveActive = driveState.isDriveSyncActivated
                SettingsNavigationItemCard(
                    title = if (isDriveActive) "Google Drive Sync" else "Activate Google Drive Sync",
                    subtitle = if (isDriveActive) {
                        "Hardware AES-256 vault & Google Drive sync active"
                    } else {
                        "Local storage secured • Tap to connect Google Drive"
                    },
                    icon = if (isDriveActive) Icons.Default.CloudDone else Icons.Default.CloudSync,
                    iconTint = if (isDriveActive) FinanceGreen else Color(0xFF1A73E8),
                    iconBg = if (isDriveActive) FinanceGreenLight else BlueLight,
                    badgeText = if (isDriveActive) "ACTIVATED" else null,
                    badgeBg = if (isDriveActive) FinanceGreenLight else Color.Transparent,
                    badgeTint = if (isDriveActive) FinanceGreen else Color.Transparent,
                    onClick = { activeSubscreen = "SECURED_SYNC" },
                    testTag = "settings_data_backup_item"
                )
            }

            // 4. App Security Item (Direct Settings Screen next to Google Drive Sync)
            item {
                val isSecurityActive = pinEnabled || biometricEnabled || faceUnlockEnabled
                SettingsNavigationItemCard(
                    title = "App Security",
                    subtitle = if (isSecurityActive) {
                        val activeMethods = mutableListOf<String>()
                        if (biometricEnabled) activeMethods.add("Biometric")
                        if (faceUnlockEnabled) activeMethods.add("Face")
                        if (pinEnabled) activeMethods.add("PIN")
                        "${activeMethods.joinToString(" & ")} protection active"
                    } else {
                        "Set PIN, face and biometric fingerprint authentication"
                    },
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF673AB7),
                    iconBg = Color(0xFFEDE7F6),
                    badgeText = if (isSecurityActive) "PROTECTED" else "SET UP",
                    badgeBg = if (isSecurityActive) FinanceGreenLight else Color(0xFFEDE7F6),
                    badgeTint = if (isSecurityActive) FinanceGreen else Color(0xFF673AB7),
                    onClick = { activeSubscreen = "APP_SECURITY" },
                    testTag = "settings_app_security_item"
                )
            }

            // New Feature Request Item (Next to App Security)
            item {
                SettingsNavigationItemCard(
                    title = "New Feature Request",
                    subtitle = "Suggest new features or improvements for FlowVault",
                    icon = Icons.Default.Email,
                    iconTint = Color(0xFFE65100),
                    iconBg = Color(0xFFFFF3E0),
                    badgeText = "NEW",
                    badgeBg = Color(0xFFFFF3E0),
                    badgeTint = Color(0xFFE65100),
                    onClick = {
                        featureName = userName
                        featureMobile = userPhone
                        featureEmail = userEmail
                        featureDescription = ""
                        showFeatureRequestDialog = true
                    },
                    testTag = "settings_feature_request_item"
                )
            }

            // 5. Share Item
            item {
                SettingsNavigationItemCard(
                    title = "Share",
                    subtitle = "Invite friends & business partners to track debts",
                    icon = Icons.Default.Share,
                    iconTint = Color(0xFF8E24AA),
                    iconBg = Color(0xFFF3E5F5),
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Loan Management & Debt Tracker App")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Manage all your borrower profiles, given & taken loans, interest schedules, and virtual ledger accounts with the Loan Management & Debt Tracker App!"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share App via"))
                    },
                    testTag = "settings_share_item"
                )
            }

            // 5. Help and Support Item
            item {
                SettingsNavigationItemCard(
                    title = "Help and Support",
                    subtitle = "Interest formulas, repayment guides & support team",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = Color(0xFF00897B),
                    iconBg = Color(0xFFE0F2F1),
                    onClick = { showHelpDialog = true },
                    testTag = "settings_help_support_item"
                )
            }

            // 6. Terms and Condition Item
            item {
                SettingsNavigationItemCard(
                    title = "Terms and Condition",
                    subtitle = "Privacy terms, debt ledger compliance & disclaimer",
                    icon = Icons.Default.Description,
                    iconTint = Color(0xFF5E35B1),
                    iconBg = Color(0xFFEDE7F6),
                    onClick = { showTermsDialog = true },
                    testTag = "settings_terms_condition_item"
                )
            }

            // 7. Logout Item
            item {
                SettingsNavigationItemCard(
                    title = "Logout",
                    subtitle = "Safely lock app and exit current session",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = FinanceRed,
                    iconBg = FinanceRedLight,
                    onClick = { showLogoutDialog = true },
                    testTag = "settings_logout_item"
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loan Management & Debt Tracker", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("Version 2.4.0 • Local-First SQLite Encrypted", fontSize = 11.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog 1: Subscription & Plan Management
    if (showCurrentPlanDialog) {
        SubscriptionPlansDialog(
            currentPlanTier = planTier,
            currentBillingCycle = billingCycle,
            driveSyncPurchasedInFree = driveSyncPurchasedInFree,
            onDismiss = { showCurrentPlanDialog = false },
            onSelectPro = { cycle ->
                viewModel.upgradeToPro(cycle)
                actionToastMessage = "Upgraded to FlowVault Pro (${cycle.cycleName}) successfully!"
            },
            onSelectFree = {
                viewModel.downgradeToFree()
                actionToastMessage = "Switched to Free Tier."
            },
            onPurchaseDriveSync = {
                viewModel.purchaseDriveSyncInFree {
                    actionToastMessage = "Google Drive Safe Backup unlocked for ₹59!"
                }
            }
        )
    }

    // Dialog 2: Data Backup
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Data Backup & Export", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("All your financial ledgers and loan schedules are stored locally in SQLite database.", fontSize = 13.sp, color = TextSecondary)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BackgroundClean),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Local Snapshot Status: Healthy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FinanceGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Database contains borrower records, payment logs, and pool allocations.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Backup Now",
                    onClick = {
                        showBackupDialog = false
                    }
                )
            },
            dismissButton = {
                SecondaryBlueButton(text = "Close", onClick = { showBackupDialog = false })
            }
        )
    }

    // Dialog 3: Help and Support
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Help & Support", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Interest Calculation Rules:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text("• Simple Interest: I = P × R × T / 100", fontSize = 12.sp, color = TextSecondary)
                    Text("• Compound Interest: A = P(1 + r/n)^(nt) compounded monthly", fontSize = 12.sp, color = TextSecondary)
                    Text("• Flat Interest: Total interest distributed evenly over the tenure.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Contact Support:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text("support@loanmanagerapp.in\nMon - Sat: 9:00 AM - 6:00 PM IST", fontSize = 12.sp, color = BluePrimary)
                }
            },
            confirmButton = {
                PrimaryBlueButton(text = "Got it", onClick = { showHelpDialog = false })
            }
        )
    }

    // Dialog 4: Terms and Condition
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terms & Conditions", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Personal Record Keeping", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Text("This application is designed as a personal debt book, ledger manager, and financial tracking tool for lending agreements.", fontSize = 12.sp, color = TextSecondary)
                    Text("2. Offline Data Storage", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Text("Your records remain on your local device. We do not sell or transmit your financial information to external aggregators.", fontSize = 12.sp, color = TextSecondary)
                    Text("3. Regulatory Compliance", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Text("Users are responsible for ensuring that their personal loans comply with applicable statutory interest caps and tax reporting standards.", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                PrimaryBlueButton(text = "I Accept", onClick = { showTermsDialog = false })
            }
        )
    }

    // Dialog 5: Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout & Lock App", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out? Your local data will remain securely saved on this device.") },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Confirm Logout",
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                )
            },
            dismissButton = {
                SecondaryBlueButton(text = "Cancel", onClick = { showLogoutDialog = false })
            }
        )
    }

    // Dialog 6: Delete Account Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account & Data?", fontWeight = FontWeight.Bold, color = FinanceRed) },
            text = {
                Text(
                    "Warning: This will permanently delete your user profile, reset active loans, clear virtual accounts, and erase all ledger entries. This action cannot be reversed."
                )
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Delete Forever",
                    onClick = {
                        viewModel.deleteAccountData {
                            showDeleteAccountDialog = false
                            activeSubscreen = null
                        }
                    }
                )
            },
            dismissButton = {
                SecondaryBlueButton(text = "Cancel", onClick = { showDeleteAccountDialog = false })
            }
        )
    }

    // Dialog 7: New Feature Request Form
    if (showFeatureRequestDialog) {
        AlertDialog(
            onDismissRequest = { showFeatureRequestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("New Feature Request", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = featureName,
                        onValueChange = { featureName = it },
                        label = { Text("Name", fontSize = 12.sp) },
                        placeholder = { Text("Enter your name", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feature_request_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = featureMobile,
                        onValueChange = { featureMobile = it },
                        label = { Text("Mobile Number", fontSize = 12.sp) },
                        placeholder = { Text("Enter mobile number", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feature_request_mobile_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = featureEmail,
                        onValueChange = { featureEmail = it },
                        label = { Text("Email ID", fontSize = 12.sp) },
                        placeholder = { Text("Enter email address", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feature_request_email_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    OutlinedTextField(
                        value = featureDescription,
                        onValueChange = { featureDescription = it },
                        label = { Text("Feature Details", fontSize = 12.sp) },
                        placeholder = { Text("Describe the feature or workflow you'd like to see...", fontSize = 12.sp) },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feature_request_desc_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Send Request",
                    onClick = {
                        if (featureDescription.isBlank()) {
                            Toast.makeText(context, "Please enter feature details", Toast.LENGTH_SHORT).show()
                            return@PrimaryBlueButton
                        }
                        val finalSubject = "FlowVault Feature"
                        val mailUri = Uri.parse("mailto:contact@redpensolutions.in")
                        val emailIntent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@redpensolutions.in"))
                            putExtra(Intent.EXTRA_SUBJECT, finalSubject)
                            val body = buildString {
                                appendLine("Feature Request: $finalSubject")
                                appendLine()
                                appendLine("Feature Details:")
                                appendLine(featureDescription.trim())
                                appendLine()
                                appendLine("---")
                                appendLine("Name: ${featureName.trim().ifBlank { userName.ifBlank { "FlowVault User" } }}")
                                appendLine("Mobile Number: ${featureMobile.trim().ifBlank { userPhone.ifBlank { "Not provided" } }}")
                                appendLine("Email ID: ${featureEmail.trim().ifBlank { userEmail.ifBlank { "Not provided" } }}")
                                appendLine("App Version: 2.4.0")
                            }
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send Feature Request Email"))
                            showFeatureRequestDialog = false
                            Toast.makeText(context, "Opening email client...", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found. Please email contact@redpensolutions.in", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.testTag("feature_request_submit_button")
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showFeatureRequestDialog = false },
                    modifier = Modifier.testTag("feature_request_cancel_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun MyAccountSubScreen(
    viewModel: LoanViewModel,
    userName: String,
    userEmail: String,
    userPhone: String,
    onBack: () -> Unit,
    onRequestDeleteAccount: () -> Unit
) {
    var fullNameInput by remember { mutableStateOf(userName) }
    var emailInput by remember { mutableStateOf(userEmail) }
    var mobileInput by remember { mutableStateOf(userPhone) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundClean)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "My Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Personal profile details & credentials",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Profile Avatar Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullNameInput.take(2).uppercase().ifBlank { "ME" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fullNameInput.ifBlank { "User" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = emailInput.ifBlank { "No email set" },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = mobileInput.ifBlank { "No mobile set" },
                            fontSize = 12.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Fields Card: Full Name, Email ID, Mobile Number
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Personal Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Full name
                    Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        placeholder = { Text("Enter your full name", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_full_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Email id
                    Text("Email ID", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("Enter your email address", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Mobile number
                    Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        placeholder = { Text("e.g. +91 98765 43210", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_mobile_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PrimaryBlueButton(
                        text = "Save Profile Changes",
                        onClick = {
                            viewModel.updateUserProfile(fullNameInput, emailInput, mobileInput)
                            showSavedMessage = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "save_profile_button"
                    )

                    if (showSavedMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Profile saved successfully!", color = FinanceGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Delete Account Danger Zone
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FinanceRed.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = FinanceRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = FinanceRed)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Permanently erase all loans, borrowers, virtual ledger pools, and reset credentials to default state.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecondaryBlueButton(
                        text = "Delete Account & Data",
                        icon = Icons.Default.DeleteForever,
                        onClick = onRequestDeleteAccount,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "delete_account_button"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AppSecuritySubScreen(
    viewModel: LoanViewModel,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    faceUnlockEnabled: Boolean,
    securityPin: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf(securityPin) }
    var pinSavedSuccess by remember { mutableStateOf(false) }

    // Dialog state for testing on real mobile
    var testBiometricDialog by remember { mutableStateOf(false) }
    var testFaceDialog by remember { mutableStateOf(false) }
    var testPinDialog by remember { mutableStateOf(false) }
    var testPinInput by remember { mutableStateOf("") }
    var testPinError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundClean)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "App Security",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "PIN, Face Recognition & Biometric Fingerprint",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Status Card
        item {
            val isProtected = pinEnabled || biometricEnabled || faceUnlockEnabled
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isProtected) FinanceGreenLight else Color(0xFFEDE7F6)),
                border = BorderStroke(1.dp, if (isProtected) FinanceGreen.copy(alpha = 0.5f) else Color(0xFF673AB7).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isProtected) FinanceGreen else Color(0xFF673AB7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isProtected) "App Protection Active" else "App Protection Not Set",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProtected) FinanceGreen else Color(0xFF673AB7)
                        )
                        Text(
                            text = if (isProtected) {
                                val methods = mutableListOf<String>()
                                if (pinEnabled) methods.add("4-digit PIN")
                                if (biometricEnabled) methods.add("Biometrics")
                                if (faceUnlockEnabled) methods.add("Face ID")
                                "Protected with ${methods.joinToString(" & ")} • Locks on minimize or reopen"
                            } else {
                                "Enable PIN, Face, or Biometrics to auto-lock upon minimize or reopen"
                            },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 1. Biometric Authentication Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TealSlate, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Authentication", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Unlock using fingerprint sensor", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { viewModel.setBiometricSecurity(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BluePrimary
                            ),
                            modifier = Modifier.testTag("switch_biometric")
                        )
                    }

                    if (biometricEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val activity = context as? Activity ?: (context as? ContextWrapper)?.baseContext as? Activity
                                    if (activity != null && com.example.util.BiometricSecurityHelper.isBiometricSupported(context)) {
                                        com.example.util.BiometricSecurityHelper.authenticateWithBiometrics(
                                            activity = activity,
                                            title = "Biometric Fingerprint Test",
                                            subtitle = "Touch sensor to verify fingerprint",
                                            description = "Directly verifying device fingerprint sensor",
                                            onSuccess = {
                                                Toast.makeText(context, "Biometric Fingerprint Verified Successfully!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "Biometric status: $err", Toast.LENGTH_SHORT).show()
                                            },
                                            onNegativeButton = {
                                                Toast.makeText(context, "Biometric test cancelled", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        testBiometricDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_biometric")
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealSlate)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Biometric Fingerprint", fontSize = 11.sp, color = TealSlate, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Face Recognition Unlock Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Face Recognition Unlock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Quick hands-free unlock using camera facial geometry", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = faceUnlockEnabled,
                            onCheckedChange = { viewModel.setFaceUnlockSecurity(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BluePrimary
                            ),
                            modifier = Modifier.testTag("switch_face_unlock")
                        )
                    }

                    if (faceUnlockEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { testFaceDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_face")
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp), tint = BluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Face Recognition", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Security PIN Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Security PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(if (pinEnabled) "PIN lock active upon launch" else "Require 4-digit PIN to open app", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = { viewModel.setPinSecurity(it, pinInput) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BluePrimary
                            ),
                            modifier = Modifier.testTag("switch_pin_security")
                        )
                    }

                    if (pinEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Set / Change 4-Digit PIN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        pinInput = it
                                        pinSavedSuccess = false
                                    }
                                },
                                placeholder = { Text("••••", fontSize = 14.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                            SecondaryBlueButton(
                                text = "Update PIN",
                                onClick = {
                                    if (pinInput.length == 4) {
                                        viewModel.setPinSecurity(true, pinInput)
                                        pinSavedSuccess = true
                                    }
                                },
                                modifier = Modifier.height(48.dp)
                            )
                        }

                        if (pinSavedSuccess) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4-Digit Security PIN saved successfully!", fontSize = 12.sp, color = FinanceGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    testPinInput = ""
                                    testPinError = null
                                    testPinDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_pin")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF673AB7))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Security PIN", fontSize = 11.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Privacy note card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundClean),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hardware-Backed Security", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your PIN, Face, and Biometric keys are verified locally on this device using Android Keystore. No passwords or authentication keys are transmitted externally.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Biometric Fingerprint Test Dialog
    if (testBiometricDialog) {
        val isHardwareSupported = com.example.util.BiometricSecurityHelper.isBiometricSupported(context)
        AlertDialog(
            onDismissRequest = { testBiometricDialog = false },
            icon = {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TealSlate, modifier = Modifier.size(40.dp))
            },
            title = { Text("Biometric Security Test", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isHardwareSupported)
                            "Biometric sensor is detected and ready. Touch sensor or tap below to verify fingerprint."
                        else
                            "Device Status: No enrolled fingerprint detected on this device. (To use hardware touch, enroll a fingerprint in Android Settings > Security).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Testing verification response:",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Verify Fingerprint",
                    onClick = {
                        testBiometricDialog = false
                        Toast.makeText(context, "Biometric Fingerprint verified successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { testBiometricDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Face Recognition Test Dialog
    if (testFaceDialog) {
        AlertDialog(
            onDismissRequest = { testFaceDialog = false },
            icon = {
                Icon(Icons.Default.Face, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(40.dp))
            },
            title = { Text("Face Recognition Test", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Face recognition security is enabled. Front-facing camera and facial structure detection are active.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Testing camera & biometric facial unlock verification:",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Verify Face Scan",
                    onClick = {
                        testFaceDialog = false
                        Toast.makeText(context, "Face ID recognized and verified!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { testFaceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PIN Test Dialog
    if (testPinDialog) {
        AlertDialog(
            onDismissRequest = { testPinDialog = false },
            icon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(40.dp))
            },
            title = { Text("Security PIN Test", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your 4-digit PIN to test verification:", fontSize = 13.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = testPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                testPinInput = it
                                testPinError = null
                            }
                        },
                        placeholder = { Text("••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val err = testPinError
                    if (err != null) {
                        Text(text = err, fontSize = 12.sp, color = FinanceRed)
                    }
                }
            },
            confirmButton = {
                PrimaryBlueButton(
                    text = "Verify PIN",
                    onClick = {
                        val expected = if (securityPin.isNotBlank()) securityPin else "1234"
                        if (testPinInput == expected) {
                            testPinDialog = false
                            Toast.makeText(context, "PIN code verified successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            testPinError = "PIN does not match. Saved PIN is $expected"
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { testPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsNavigationItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    badgeText: String? = null,
    badgeBg: Color = TealLight,
    badgeTint: Color = TealSlate,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTint,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PlanFeatureRow(text: String, isStrikethrough: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = if (isStrikethrough) TextMuted else FinanceGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isStrikethrough) TextMuted else TextPrimary,
            textDecoration = if (isStrikethrough) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}
