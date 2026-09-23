package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Loan
import com.example.ui.components.BorrowerAvatar
import com.example.ui.components.ProBadge
import com.example.ui.components.ProFeatureLockedDialog
import com.example.ui.components.SubscriptionPlansDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.dialogs.TopUpLoanDialog
import com.example.ui.theme.BackgroundClean
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
import com.example.util.PdfExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BorrowerHistoryMonthGroup(
    val monthYear: String,
    val loans: List<Loan>,
    val totalAmount: Double,
    val netAmount: Double
)

@Composable
fun BorrowerDetailScreen(
    borrowerName: String,
    viewModel: LoanViewModel,
    onBack: () -> Unit,
    onOpenAddLoanForBorrower: (String) -> Unit,
    onOpenPaymentForLoan: (Loan) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val virtualAccounts by viewModel.allVirtualAccounts.collectAsStateWithLifecycle()
    val currency by viewModel.defaultCurrency.collectAsStateWithLifecycle()
    val allLedgerEntries by viewModel.allLedgerEntries.collectAsStateWithLifecycle()
    val isPro by viewModel.isProPlan.collectAsStateWithLifecycle()
    val planTier by viewModel.currentPlanTier.collectAsStateWithLifecycle()
    val billingCycle by viewModel.currentBillingCycle.collectAsStateWithLifecycle()
    val drivePurchased by viewModel.driveSyncPurchasedInFree.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val borrowerLoans = remember(allLoans, borrowerName) {
        allLoans.filter { it.borrowerName.equals(borrowerName, ignoreCase = true) }
    }

    val borrowerLoanIds = remember(borrowerLoans) { borrowerLoans.map { it.id }.toSet() }
    val borrowerLedgerEntries = remember(allLedgerEntries, borrowerLoanIds) {
        allLedgerEntries.filter { it.loanId != null && it.loanId in borrowerLoanIds }
    }

    var selectedTab by remember { mutableStateOf("PENDING") } // "PENDING" or "HISTORY"
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showProLockedDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    // Dialog state for loan actions and topup
    var selectedLoanForActions by remember { mutableStateOf<Loan?>(null) }
    var loanForTopUp by remember { mutableStateOf<Loan?>(null) }

    val pendingLoans = borrowerLoans.filter { it.status != "SETTLED" }
    val settledLoans = borrowerLoans.filter { it.status == "SETTLED" }

    var isPendingExpanded by remember { mutableStateOf(true) }
    var isSettledExpanded by remember { mutableStateOf(true) }

    val currentList = if (selectedTab == "PENDING") {
        pendingLoans
    } else {
        borrowerLoans.sortedByDescending { it.startDate }
    }

    // Financial balance calculations
    val totalGiven = if (pendingLoans.isNotEmpty()) {
        pendingLoans.filter { it.direction == "GIVEN" }.sumOf { it.balanceRemaining }
    } else {
        borrowerLoans.filter { it.direction == "GIVEN" }.sumOf { it.principalAmount }
    }
    val totalTaken = if (pendingLoans.isNotEmpty()) {
        pendingLoans.filter { it.direction == "TAKEN" }.sumOf { it.balanceRemaining }
    } else {
        borrowerLoans.filter { it.direction == "TAKEN" }.sumOf { it.principalAmount }
    }
    val netBalance = totalGiven - totalTaken

    if (selectedLoanForActions != null) {
        BackHandler {
            selectedLoanForActions = null
        }
        LoanParticularDetailScreen(
            loan = selectedLoanForActions!!,
            currencySymbol = currency,
            viewModel = viewModel,
            onBack = { selectedLoanForActions = null },
            onRecordPayment = {
                val targetLoan = selectedLoanForActions
                if (targetLoan != null) {
                    onOpenPaymentForLoan(targetLoan)
                }
            },
            onTopUp = {
                loanForTopUp = selectedLoanForActions
            },
            modifier = modifier
        )
    } else {
        Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundClean,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenAddLoanForBorrower(borrowerName) },
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_add_borrower_loan")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Loan for $borrowerName",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top App Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_borrower_detail")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2BB6A3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = borrowerName.take(1).uppercase(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = borrowerName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add New Loan") },
                                onClick = {
                                    showOptionsMenu = false
                                    onOpenAddLoanForBorrower(borrowerName)
                                }
                            )
                            if (pendingLoans.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Settle Loan Payment") },
                                    onClick = {
                                        showOptionsMenu = false
                                        pendingLoans.firstOrNull()?.let { onOpenPaymentForLoan(it) }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Export to PDF")
                                        if (!isPro) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            ProBadge()
                                        }
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    if (!isPro) {
                                        showProLockedDialog = true
                                    } else {
                                        val pdf = PdfExportHelper.exportBorrowerPdf(
                                            context = context,
                                            borrowerName = borrowerName,
                                            loans = borrowerLoans,
                                            ledgerEntries = borrowerLedgerEntries,
                                            currencySymbol = currency
                                        )
                                        PdfExportHelper.sharePdfFile(
                                            context = context,
                                            pdfFile = pdf,
                                            subject = "Customer Ledger Statement - $borrowerName"
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = FinanceRed)
                                }
                            )
                        }
                    }
                }
            }

            // Dark Hero Loan Balance Card (matching screenshot)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_loan_balance_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3545)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Loan Balance with $borrowerName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Loan Given
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = formatCurrency(totalGiven, currency),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2BB6A3)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Loan Given",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            // Math Operator: -
                            Text(
                                text = " - ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            // Middle: Loan Taken
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatCurrency(totalTaken, currency),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7A7A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Loan Taken",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            // Math Operator: =
                            Text(
                                text = " = ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            // Right: Balance
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(netBalance, currency),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2BB6A3)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Balance",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Pending | History Tabs (matching screenshot)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Pending Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = "PENDING" }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pending",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == "PENDING") FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == "PENDING") Color(0xFF2BB6A3) else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .height(2.5.dp)
                                .background(if (selectedTab == "PENDING") Color(0xFF2BB6A3) else Color.Transparent)
                        )
                    }

                    // History Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = "HISTORY" }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "History",
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == "HISTORY") FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == "HISTORY") Color(0xFF2BB6A3) else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .height(2.5.dp)
                                .background(if (selectedTab == "HISTORY") Color(0xFF2BB6A3) else Color.Transparent)
                        )
                    }
                }
            }

            // Loan List Content matching screenshot
            if (selectedTab == "PENDING") {
                // Section 1: Pending Loans
                item {
                    LoanSectionHeader(
                        title = "Pending Loans (${pendingLoans.size})",
                        isExpanded = isPendingExpanded,
                        onToggle = { isPendingExpanded = !isPendingExpanded }
                    )
                }

                if (isPendingExpanded) {
                    if (pendingLoans.isEmpty()) {
                        item {
                            Text(
                                text = "No pending loans for $borrowerName",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        items(pendingLoans, key = { "pending_${it.id}" }) { loan ->
                            DetailedLoanCard(
                                loan = loan,
                                currencySymbol = currency,
                                onClick = { selectedLoanForActions = loan }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Section 2: Settled Loans (collapsible section displayed right below pending loans as in screenshot)
                item {
                    LoanSectionHeader(
                        title = "Settled Loans (${settledLoans.size})",
                        isExpanded = isSettledExpanded,
                        onToggle = { isSettledExpanded = !isSettledExpanded }
                    )
                }

                if (isSettledExpanded) {
                    if (settledLoans.isEmpty()) {
                        item {
                            Text(
                                text = "No settled loans for $borrowerName",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        items(settledLoans, key = { "settled_${it.id}" }) { loan ->
                            DetailedLoanCard(
                                loan = loan,
                                currencySymbol = currency,
                                onClick = { selectedLoanForActions = loan }
                            )
                        }
                    }
                }
            } else {
                // HISTORY TAB: Show all settled or all loans history with the detailed cards
                item {
                    LoanSectionHeader(
                        title = "All Loans History (${borrowerLoans.size})",
                        isExpanded = true,
                        onToggle = {}
                    )
                }

                if (borrowerLoans.isEmpty()) {
                    item {
                        Text(
                            text = "No loan history found for $borrowerName",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    items(borrowerLoans.sortedByDescending { it.startDate }, key = { "history_${it.id}" }) { loan ->
                        DetailedLoanCard(
                            loan = loan,
                            currencySymbol = currency,
                            onClick = { selectedLoanForActions = loan }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    }

    // Top-up Dialog
    loanForTopUp?.let { loan ->
        TopUpLoanDialog(
            loan = loan,
            virtualAccounts = virtualAccounts,
            currencySymbol = currency,
            onDismiss = { loanForTopUp = null },
            onConfirmTopUp = { amount, disbursementAccountId, calculateInterest, note ->
                viewModel.topUpLoan(
                    loanId = loan.id,
                    amount = amount,
                    disbursementAccountId = disbursementAccountId,
                    calculateInterest = calculateInterest,
                    note = note
                ) {
                    loanForTopUp = null
                }
            }
        )
    }

    if (showProLockedDialog) {
        ProFeatureLockedDialog(
            featureTitle = "Export Ledger to PDF",
            featureDescription = "Generating customer ledger statements and PDFs with transaction history is a FlowVault Pro feature.\n\nIn Free Tier, PDF export is disabled. Upgrade to Pro (₹99/mo) to export and share branded ledger statements.",
            onDismiss = { showProLockedDialog = false },
            onUpgrade = {
                showProLockedDialog = false
                showSubscriptionDialog = true
            }
        )
    }

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

/**
 * Header row for expandable loan sections (Pending Loans, Settled Loans)
 */
@Composable
fun LoanSectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = Color(0xFF4B5563),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Detailed loan card matching the reference image
 */
@Composable
fun DetailedLoanCard(
    loan: Loan,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGiven = loan.direction == "GIVEN"
    val isSettled = loan.status == "SETTLED"

    val titleText = if (isGiven) "You gave a loan" else "You took a loan"
    val subtitleText = if (isSettled) {
        val settledDate = if (loan.dueDate > 0) loan.dueDate else loan.startDate
        "Settled on  ${formatOrdinalDate(settledDate)}"
    } else {
        val duration = formatDurationElapsed(loan.startDate)
        val prefix = if (isGiven) "Given on" else "Taken on"
        "$prefix  ${formatOrdinalDate(loan.startDate)} ($duration)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("loan_card_${loan.loanNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F2F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Top row: Icon badge + Title + Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular badge with upload/arrow-up-out-of-tray icon in teal
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isGiven) Color(0xFF2BB6A3) else Color(0xFFFF7A7A)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val w = size.width
                        val h = size.height
                        val stroke = 2.2.dp.toPx()
                        val color = Color.White

                        // Tray at bottom
                        val trayY = h * 0.85f
                        val trayLeft = w * 0.15f
                        val trayRight = w * 0.85f
                        val trayUp = h * 0.65f

                        drawLine(color, Offset(trayLeft, trayUp), Offset(trayLeft, trayY), strokeWidth = stroke)
                        drawLine(color, Offset(trayLeft, trayY), Offset(trayRight, trayY), strokeWidth = stroke)
                        drawLine(color, Offset(trayRight, trayUp), Offset(trayRight, trayY), strokeWidth = stroke)

                        // Arrow shaft
                        val arrowTop = h * 0.15f
                        val arrowBottom = h * 0.65f
                        val midX = w * 0.5f
                        drawLine(color, Offset(midX, arrowBottom), Offset(midX, arrowTop), strokeWidth = stroke)

                        // Arrow head
                        val headSize = w * 0.28f
                        drawLine(color, Offset(midX - headSize, arrowTop + headSize), Offset(midX, arrowTop), strokeWidth = stroke)
                        drawLine(color, Offset(midX + headSize, arrowTop + headSize), Offset(midX, arrowTop), strokeWidth = stroke)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitleText,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: Amount Due : ₹XX,XXX.XX (left) and ₹YY,YYY.YY / SETTLED (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Amount Due :",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = formatCurrency(loan.principalAmount, currencySymbol),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2BB6A3)
                    )
                }

                if (isSettled) {
                    Text(
                        text = "SETTLED",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2BB6A3),
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Text(
                        text = formatCurrency(loan.balanceRemaining, currencySymbol),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2BB6A3)
                    )
                }
            }

            // Bottom left indicator accent
            if (!isSettled) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .background(Color(0xFF2BB6A3), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

fun formatOrdinalDate(timeMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val monthFormat = SimpleDateFormat("MMM, yyyy", Locale.ENGLISH)
    return "$day$suffix ${monthFormat.format(Date(timeMillis))}"
}

fun formatDurationElapsed(startDate: Long, endDate: Long = System.currentTimeMillis()): String {
    val startCal = Calendar.getInstance().apply { timeInMillis = startDate }
    val endCal = Calendar.getInstance().apply { timeInMillis = endDate }

    var years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
    var months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
    var days = endCal.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months--
        val prevCal = (endCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        days += prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years--
        months += 12
    }

    return when {
        years > 0 && months > 0 -> "$years ${if (years == 1) "year" else "years"}, $months ${if (months == 1) "month" else "months"}"
        years > 0 -> "$years ${if (years == 1) "year" else "years"}"
        months > 0 && days > 0 -> "$months ${if (months == 1) "month" else "months"}, $days ${if (days == 1) "day" else "days"}"
        months > 0 -> "$months ${if (months == 1) "month" else "months"}"
        days > 0 -> "$days ${if (days == 1) "day" else "days"}"
        else -> "today"
    }
}

/**
 * Individual unmerged loan card inside the borrower page (strictly matching IMG_4225)
 */
@Composable
fun BorrowerLoanItemCard(
    loan: Loan,
    currencySymbol: String,
    onClick: () -> Unit,
    onTopUpClick: () -> Unit
) {
    val isGiven = loan.direction == "GIVEN"
    val diffDuration = formatDurationSince(loan.startDate)
    val formattedStartDate = formatFriendlyDate(loan.startDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("borrower_loan_item_${loan.loanNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Section: Direction Icon + Loan Title + Date + Duration + Loan No
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Circular Direction Icon (Green Upward arrow for given, Red Downward for taken)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isGiven) Color(0xFFE2F7F4) else FinanceRedLight)
                        .border(1.5.dp, if (isGiven) Color(0xFF2BB6A3) else FinanceRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGiven) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isGiven) Color(0xFF2BB6A3) else FinanceRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isGiven) "You gave a loan" else "You took a loan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${if (isGiven) "Given" else "Taken"} on $formattedStartDate ($diffDuration)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Loan no: ${loan.loanNumber}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (loan.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = loan.notes,
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontStyle = FontStyle.Italic,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Section: Amount Due label, Strikethrough/Original Principal, Net Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount Due :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Original principal (often formatted with strikethrough in screenshot when due differs)
                    Text(
                        text = formatCurrency(loan.principalAmount, currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2BB6A3).copy(alpha = 0.8f),
                        textDecoration = if (loan.balanceRemaining != loan.principalAmount) TextDecoration.LineThrough else TextDecoration.None
                    )

                    // Final Net Due Amount (bold teal)
                    Text(
                        text = formatCurrency(loan.balanceRemaining, currencySymbol),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2BB6A3)
                    )
                }
            }
        }
    }
}

/**
 * Detailed loan modal when tapping a loan inside borrower page
 */
@Composable
fun LoanDetailBottomSheetDialog(
    loan: Loan,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onTopUp: () -> Unit,
    onRecordPayment: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.2.dp, BorderCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = loan.loanNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = loan.borrowerName,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (loan.direction == "GIVEN") Color(0xFFE2F7F4) else FinanceRedLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (loan.direction == "GIVEN") "Given" else "Taken",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (loan.direction == "GIVEN") Color(0xFF2BB6A3) else FinanceRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceInnerCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Principal Amount", formatCurrency(loan.principalAmount, currencySymbol))
                        DetailRow("Interest Rate", "${loan.annualInterestRate}% (${loan.interestPeriod.lowercase()})")
                        DetailRow("Total Interest", formatCurrency(loan.totalInterest, currencySymbol))
                        DetailRow("Total Due", formatCurrency(loan.totalAmountDue, currencySymbol))
                        DetailRow("Amount Paid", formatCurrency(loan.amountPaid, currencySymbol))
                        HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                        DetailRow(
                            "Net Balance Due",
                            formatCurrency(loan.balanceRemaining, currencySymbol),
                            isHighlight = true,
                            highlightColor = Color(0xFF2BB6A3)
                        )
                        DetailRow("Start Date", formatDate(loan.startDate))
                        DetailRow("Due Date", formatDate(loan.dueDate))
                    }
                }

                if (loan.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Notes: ${loan.notes}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Row: "+ Give Top-up" and "Record Payment"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTopUp,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_loan_topup"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB6A3))
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Top-up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (loan.balanceRemaining > 0.01) {
                        Button(
                            onClick = onRecordPayment,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_loan_pay"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Repay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isHighlight) TextPrimary else TextSecondary, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = if (isHighlight) 14.sp else 12.sp, color = highlightColor, fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

private fun formatDurationSince(startDate: Long): String {
    val diff = System.currentTimeMillis() - startDate
    val days = (diff / (24L * 3600 * 1000)).toInt()
    return when {
        days < 30 -> "$days days"
        days < 365 -> "${(days / 30.0).toInt().coerceAtLeast(1)} months"
        else -> "${(days / 365.0).toInt().coerceAtLeast(1)} years"
    }
}

private fun formatFriendlyDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
