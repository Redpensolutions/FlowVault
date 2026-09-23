package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BillingCycle
import com.example.data.model.PlanTier
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

val GoldCrown = Color(0xFFD97706)
val GoldGradient = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
val ProCardBg = Color(0xFF1E293B)
val ProCardHighlight = Color(0xFF3B82F6)

@Composable
fun ProBadge(
    modifier: Modifier = Modifier,
    text: String = "PRO",
    showIcon: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GoldGradient)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Dialog shown when a Free Tier user attempts to access a Pro feature:
 * - Photo attachments on transactions/loans
 * - Export loan data to PDF & WhatsApp card
 * - Compound interest calculations
 * - Exceeding 5 active loans
 * - Exceeding 3 virtual accounts
 */
@Composable
fun ProFeatureLockedDialog(
    featureTitle: String,
    featureDescription: String,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GoldCrown,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = featureTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "FlowVault Pro Feature",
                        fontSize = 11.sp,
                        color = GoldCrown,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = featureDescription,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Included with FlowVault Pro:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        LockedFeatureCheckItem("59 Active Loans (vs 5 in Free)")
                        LockedFeatureCheckItem("Simple, Compound & Flat interest")
                        LockedFeatureCheckItem("Photo attachments on transactions")
                        LockedFeatureCheckItem("Export loan data to PDF & WhatsApp card")
                        LockedFeatureCheckItem("Google Drive Safe Backup (Auto Sync)")
                        LockedFeatureCheckItem("Email Support")
                    }
                }

                Text(
                    text = "Plans start at only ₹99/month (Yearly) or ₹159/month.",
                    fontSize = 11.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onUpgrade()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_upgrade_to_pro_from_dialog")
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upgrade to Pro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun LockedFeatureCheckItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = FinanceGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = TextSecondary)
    }
}

/**
 * Full Subscription Plans Dialog displaying Free Tier vs Pro Plan.
 */
@Composable
fun SubscriptionPlansDialog(
    currentPlanTier: PlanTier,
    currentBillingCycle: BillingCycle,
    driveSyncPurchasedInFree: Boolean,
    onDismiss: () -> Unit,
    onSelectPro: (BillingCycle) -> Unit,
    onSelectFree: () -> Unit,
    onPurchaseDriveSync: () -> Unit
) {
    var selectedCycle by remember { mutableStateOf(currentBillingCycle) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("subscription_plans_dialog"),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FlowVault Plans",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Choose the right plan for your debt ledger",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Billing Cycle Toggle (Monthly vs Yearly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BillingCycle.values().forEach { cycle ->
                        val isSel = selectedCycle == cycle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color.White else Color.Transparent)
                                .clickable { selectedCycle = cycle }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (cycle == BillingCycle.YEARLY) "Yearly (₹99/mo)" else "Monthly (₹159/mo)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) BluePrimary else TextSecondary
                                )
                                if (cycle.discountBadge != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FinanceGreen)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = cycle.discountBadge,
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PLAN 1: PRO PLAN (RECOMMENDED)
                val isProActive = currentPlanTier == PlanTier.PRO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardBg),
                    border = BorderStroke(2.dp, if (isProActive) GoldCrown else ProCardHighlight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pro Plan",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ProBadge(text = if (isProActive) "ACTIVE PLAN" else "RECOMMENDED")
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (selectedCycle == BillingCycle.YEARLY) "₹99" else "₹159",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (selectedCycle == BillingCycle.YEARLY) "/month (billed ₹1,188/yr)" else "/month",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Features List matching user prompt
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            PlanCheckRow("59 Active Loans", Color.White, true)
                            PlanCheckRow("Simple, Compound & Flat interest calculations", Color.White, true)
                            PlanCheckRow("Virtual Accounts & Ledger Pools", Color.White, true)
                            PlanCheckRow("100% Private & Offline", Color.White, true)
                            PlanCheckRow("App Security (Pin & Biometrics & Face ID)", Color.White, true)
                            PlanCheckRow("Google Drive Safe Backup (Auto Sync)", Color.White, true)
                            PlanCheckRow("Photo attachments on transactions", Color.White, true)
                            PlanCheckRow("Export loan data to PDF & Whatsapp card", Color.White, true)
                            PlanCheckRow("Email Support", Color.White, true)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onSelectPro(selectedCycle)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isProActive) FinanceGreen else BluePrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_select_pro_plan")
                        ) {
                            Icon(
                                imageVector = if (isProActive) Icons.Default.Check else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isProActive) "Active Plan (${selectedCycle.cycleName})" else "Upgrade to Pro (${if (selectedCycle == BillingCycle.YEARLY) "₹99/mo" else "₹159/mo"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PLAN 2: FREE TIER
                val isFreeActive = currentPlanTier == PlanTier.FREE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (isFreeActive) BluePrimary else BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Free Tier",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (isFreeActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BlueLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("CURRENT PLAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                    }
                                }
                            }

                            Text(
                                text = "₹0 Free",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Features List matching user prompt
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            PlanCheckRow("Track up to 5 Active loans", TextPrimary, true)
                            PlanCheckRow("Simple & Flat interest calculations", TextPrimary, true)
                            PlanCheckRow("Track up to 3 Virtual Accounts & Ledger Pools", TextPrimary, true)
                            PlanCheckRow("100% Private & Offline", TextPrimary, true)
                            PlanCheckRow("App Security (Pin & Biometrics & Face ID)", TextPrimary, true)
                            PlanCheckRow(
                                text = if (driveSyncPurchasedInFree) "Google Drive Safe Backup (Purchased - Active)" else "Google Drive Safe Backup (Rs 59 One time)",
                                color = TextPrimary,
                                isIncluded = true
                            )
                            PlanCheckRow("Compound interest calculations", TextMuted, false)
                            PlanCheckRow("Photo attachments on transactions", TextMuted, false)
                            PlanCheckRow("Export loan data to PDF & Whatsapp card", TextMuted, false)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isFreeActive) {
                            OutlinedButton(
                                onClick = {
                                    onSelectFree()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_switch_to_free")
                            ) {
                                Text("Switch to Free Tier", color = TextSecondary, fontSize = 12.sp)
                            }
                        } else if (!driveSyncPurchasedInFree) {
                            Button(
                                onClick = {
                                    onPurchaseDriveSync()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_buy_drive_sync_59")
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unlock Drive Safe Backup (₹59 One-Time)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCheckRow(
    text: String,
    color: Color,
    isIncluded: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isIncluded) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isIncluded) FinanceGreen else FinanceRed.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = color,
            fontWeight = if (isIncluded) FontWeight.Normal else FontWeight.Light
        )
    }
}

/**
 * Dialog when Free Tier user wants Google Drive Safe Backup.
 * Can purchase with Rs 59 one-time or upgrade to Pro.
 */
@Composable
fun DriveSyncPurchaseDialog(
    onDismiss: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onUpgradeToPro: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F0FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Google Drive Safe Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Off-Device Encrypted Cloud Sync", fontSize = 11.sp, color = Color(0xFF1A73E8))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Secure your encrypted debt ledger and virtual account logs in your personal Google Drive AppData folder.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Free Tier Safe Backup", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("One-time lifetime unlock", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("₹59", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = FinanceGreen)
                        }
                    }
                }

                Text(
                    text = "Tip: Upgrading to Pro (₹99/mo) includes Google Drive Safe Backup with continuous Auto-Sync, 59 active loans, photo receipts, and PDF exports.",
                    fontSize = 11.sp,
                    color = BluePrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPurchase,
                colors = ButtonDefaults.buttonColors(containerColor = FinanceGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_drive_purchase_59")
            ) {
                Text("Unlock for ₹59", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                TextButton(onClick = {
                    onDismiss()
                    onUpgradeToPro()
                }) {
                    Text("View Pro Plan", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
