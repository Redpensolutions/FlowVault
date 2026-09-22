package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.ui.components.LoanDetailsDialog
import com.example.ui.components.formatCurrency
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderCard
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Detailed loan screen opened when clicking a specific loan inside a borrower's list,
 * featuring two tabs: "Summary" and "Transactions", strictly matching the reference screenshot.
 */
@Composable
fun LoanParticularDetailScreen(
    loan: Loan,
    currencySymbol: String,
    viewModel: LoanViewModel,
    onBack: () -> Unit,
    onRecordPayment: () -> Unit,
    onTopUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect reactive live loan update if it was modified
    val liveLoanList by viewModel.allLoans.collectAsStateWithLifecycle()
    val currentLoan = liveLoanList.find { it.id == loan.id } ?: loan

    val ledgerEntries by viewModel.getLedgerForLoan(currentLoan.id).collectAsStateWithLifecycle(emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Summary, 1 = Transactions
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showFullLoanDetails by remember { mutableStateOf(false) }
    var showMobileLockDialog by remember { mutableStateOf(false) }
    var showEstampDialog by remember { mutableStateOf(false) }
    var showAddGuarantorDialog by remember { mutableStateOf(false) }
    var showPaymentPlanDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showActionPickerSheet by remember { mutableStateOf(false) }

    // Calculations
    val principalPaid = (currentLoan.principalAmount - currentLoan.balanceRemaining).coerceAtLeast(0.0)
    val clearedRatio = if (currentLoan.principalAmount > 0) {
        (principalPaid / currentLoan.principalAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    // Total repayments recorded from ledger or difference
    val totalRepaymentAmount = if (ledgerEntries.isNotEmpty()) {
        ledgerEntries.filter { it.category == "LOAN_REPAYMENT" || it.entryType == "CREDIT" }.sumOf { it.amount }
    } else {
        principalPaid
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundClean,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("btn_back_loan_particular")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        // Circular Avatar with teal border
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2F7F4))
                                .border(1.5.dp, Color(0xFF2BB6A3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF2BB6A3),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = currentLoan.borrowerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Loan no: ${currentLoan.loanNumber}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.testTag("btn_loan_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu Options",
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Record Payment") },
                                onClick = {
                                    showOverflowMenu = false
                                    onRecordPayment()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = FinanceGreen)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Top-Up Loan") },
                                onClick = {
                                    showOverflowMenu = false
                                    onTopUp()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BluePrimary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Full Loan Details") },
                                onClick = {
                                    showOverflowMenu = false
                                    showFullLoanDetails = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = BluePrimary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as Settled") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.markLoanSettled(currentLoan.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2BB6A3))
                                }
                            )
                        }
                    }
                }

                // Two Tabs: Summary and Transactions
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceCard,
                    contentColor = BluePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BluePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("tab_loan_summary"),
                        text = {
                            Text(
                                text = "Summary",
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) BluePrimary else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("tab_loan_transactions"),
                        text = {
                            Text(
                                text = "Transactions",
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) BluePrimary else TextSecondary
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showActionPickerSheet = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_loan_particular_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Action",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            // SUMMARY TAB (Exact match to reference screenshot)
            LoanSummaryTabContent(
                loan = currentLoan,
                currencySymbol = currencySymbol,
                principalPaid = principalPaid,
                clearedRatio = clearedRatio,
                totalRepayment = totalRepaymentAmount,
                onOpenFullLoanDetails = { showFullLoanDetails = true },
                onOpenMobileLock = { showMobileLockDialog = true },
                onOpenEstamp = { showEstampDialog = true },
                onOpenAddGuarantor = { showAddGuarantorDialog = true },
                onOpenPaymentPlan = { showPaymentPlanDialog = true },
                onOpenAddTag = { showAddTagDialog = true },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        } else {
            // TRANSACTIONS TAB
            LoanTransactionsTabContent(
                loan = currentLoan,
                ledgerEntries = ledgerEntries,
                currencySymbol = currencySymbol,
                onRecordPayment = onRecordPayment,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }

    // Modal: Full Loan Details
    if (showFullLoanDetails) {
        LoanDetailsDialog(
            loan = currentLoan,
            currencySymbol = currencySymbol,
            onDismiss = { showFullLoanDetails = false },
            onRequestPayment = {
                showFullLoanDetails = false
                onRecordPayment()
            }
        )
    }

    // Dialog: Mobile Lock Feature
    if (showMobileLockDialog) {
        MobileLockDialog(
            borrowerName = currentLoan.borrowerName,
            onDismiss = { showMobileLockDialog = false },
            onFeedback = { isPositive ->
                viewModel.recordFeatureFeedback("MOBILE_LOCK", isPositive)
            }
        )
    }

    // Dialog: Secure Loan with Estamp Feature
    if (showEstampDialog) {
        EstampDialog(
            loan = currentLoan,
            currencySymbol = currencySymbol,
            onDismiss = { showEstampDialog = false },
            onFeedback = { isPositive ->
                viewModel.recordFeatureFeedback("ESTAMP", isPositive)
            }
        )
    }

    // Dialog: Add Guarantor Feature
    if (showAddGuarantorDialog) {
        AddGuarantorDialog(
            borrowerName = currentLoan.borrowerName,
            onDismiss = { showAddGuarantorDialog = false }
        )
    }

    // Dialog: Set up Payment Plan Feature
    if (showPaymentPlanDialog) {
        PaymentPlanDialog(
            loan = currentLoan,
            currencySymbol = currencySymbol,
            onDismiss = { showPaymentPlanDialog = false }
        )
    }

    // Dialog: Add Tag
    if (showAddTagDialog) {
        AddTagDialog(
            currentTags = currentLoan.tags,
            onDismiss = { showAddTagDialog = false },
            onSave = { newTags ->
                viewModel.updateLoanTags(currentLoan.id, newTags)
                showAddTagDialog = false
            }
        )
    }

    // Quick Action Picker when pressing FAB
    if (showActionPickerSheet) {
        AlertDialog(
            onDismissRequest = { showActionPickerSheet = false },
            title = {
                Text(
                    text = "Loan Action",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showActionPickerSheet = false
                                onRecordPayment()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F7F4)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF2BB6A3))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Record Repayment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Post payment received towards principal/interest", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showActionPickerSheet = false
                                onTopUp()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BluePrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Top-Up Loan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Disburse additional amount to borrower", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showActionPickerSheet = false
                                showPaymentPlanDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Set up Payment Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Configure EMI installment calendar", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionPickerSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Summary Tab Content strictly following the reference image:
 * Tags, Principle Paid / Principle Outstanding card, Progress bar, Loan Amount, Interest,
 * Total Repayment, Amount Due, Full Loan Details button, Secure Your Loan section, Repayments section.
 */
@Composable
private fun LoanSummaryTabContent(
    loan: Loan,
    currencySymbol: String,
    principalPaid: Double,
    clearedRatio: Float,
    totalRepayment: Double,
    onOpenFullLoanDetails: () -> Unit,
    onOpenMobileLock: () -> Unit,
    onOpenEstamp: () -> Unit,
    onOpenAddGuarantor: () -> Unit,
    onOpenPaymentPlan: () -> Unit,
    onOpenAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. Tags row: "Tags: (+)"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags: ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCFBF1))
                    .clickable { onOpenAddTag() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tag",
                    tint = Color(0xFF0F766E),
                    modifier = Modifier.size(14.dp)
                )
            }

            if (loan.tags.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                loan.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE2F7F4),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = tag.trim(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F766E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Main Financial Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Row: Principle Paid vs Principle Outstanding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Principle Paid",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(principalPaid, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Principle Outstanding",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(loan.balanceRemaining, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar: "X% cleared"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (clearedRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(clearedRatio)
                                .fillMaxHeight()
                                .background(Color(0xFF2BB6A3))
                        )
                    }

                    Text(
                        text = "${(clearedRatio * 100).toInt()}% cleared",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (clearedRatio > 0.35f) Color.White else Color(0xFF4B5563),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row: Loan Amount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFullLoanDetails() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loan Amount",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatCurrency(loan.principalAmount, currencySymbol),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row: Interest [SI: X% per Year]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFullLoanDetails() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Interest",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = "SI: ${loan.annualInterestRate.toInt()}% per ${if (loan.interestPeriod == "MONTHLY") "Month" else "Year"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatCurrency(loan.totalInterest, currencySymbol),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row: Total Repayment [P+I]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFullLoanDetails() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total Repayment",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = "P+I",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatCurrency(totalRepayment, currencySymbol),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Row: Amount Due:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amount Due:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = formatCurrency(loan.balanceRemaining, currencySymbol),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Outlined Button: "Full Loan Details"
                OutlinedButton(
                    onClick = onOpenFullLoanDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_full_loan_details"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, BluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Full Loan Details",
                                color = BluePrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Section: "Secure Your Loan"
        Text(
            text = "Secure Your Loan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Card 1: Secure Loans with Mobile Lock
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenMobileLock() }
                .testTag("card_mobile_lock"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF9C3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Mobile Lock",
                        tint = Color(0xFFCA8A04),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Secure Loans with Mobile Lock",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add a lock to borrower devices to protect your loan and ensure timely payments.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCA8A04),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: Secure Loan with Estamp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenEstamp() }
                .testTag("card_estamp"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Estamp",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Secure Loan with Estamp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add a digital e-stamp legal contract to protect your loan and ensure compliance.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: Add Guarantor (maximum 2)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Guarantor",
                        tint = BluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Guarantor (maximum 2)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enhance your loan security with a Guarantor",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = onOpenAddGuarantor,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_add_guarantor")
                ) {
                    Text(
                        text = "ADD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Section: "Repayments"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repayments",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    OutlinedButton(
                        onClick = onOpenPaymentPlan,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BluePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_setup_payment_plan")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set up Payment Plan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "FreeBird lets you create Payment Plan for collecting the loan in easy installments, so the loan can be paid off on time.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

/**
 * Helper to format date with ordinal day (e.g. 26th Jun'23)
 */
private fun formatOrdinalTransactionDate(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val suffix = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
    val monthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
    val yearFormat = SimpleDateFormat("yy", Locale.ENGLISH)
    val month = monthFormat.format(cal.time)
    val year = yearFormat.format(cal.time)
    return "${day}${suffix} ${month}'${year}"
}

/**
 * Format transaction currency matching screenshot style (e.g. ₹5,28,167.00)
 */
private fun formatTransactionAmount(amount: Double, symbol: String = "₹"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$symbol${formatter.format(amount)}"
}

/**
 * Transactions Tab Content showing the chronological repayments, disbursements,
 * and settlements for this specific loan in Left (Disbursed) & Right (Collected) style.
 */
@Composable
private fun LoanTransactionsTabContent(
    loan: Loan,
    ledgerEntries: List<LedgerEntry>,
    currencySymbol: String,
    onRecordPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    var previewEntry by remember { mutableStateOf<LedgerEntry?>(null) }

    // Aggregate all transactions chronologically: ensure initial loan given/taken appears
    val allTransactions = remember(ledgerEntries, loan) {
        val list = ledgerEntries.toMutableList()
        val hasDisbursement = list.any { it.category == "LOAN_DISBURSEMENT" || it.category == "LOAN_BORROWING" }
        if (!hasDisbursement && loan.principalAmount > 0) {
            list.add(
                0,
                LedgerEntry(
                    id = -loan.id,
                    virtualAccountId = loan.virtualAccountId ?: 0L,
                    loanId = loan.id,
                    referenceCode = loan.loanNumber,
                    entryType = if (loan.direction == "TAKEN") "CREDIT" else "DEBIT",
                    amount = loan.principalAmount,
                    category = if (loan.direction == "TAKEN") "LOAN_BORROWING" else "LOAN_DISBURSEMENT",
                    description = if (loan.direction == "TAKEN") "Loan taken from ${loan.borrowerName}" else "Loan given to ${loan.borrowerName}",
                    timestamp = loan.startDate
                )
            )
        }
        list.sortedBy { it.timestamp }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // "Tap on any transaction for preview" Banner
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF384A59),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tap on any transaction for preview",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        if (allTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2F7F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF2BB6A3),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Transactions Recorded",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "All installment repayments, disbursements, and top-ups will be chronologically documented here.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRecordPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB6A3)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_txn_record_payment")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record First Payment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
            ) {
                items(allTransactions, key = { it.id }) { entry ->
                    // Left side = Loan given / disbursed
                    // Right side = Collected / Repaid
                    val isLoanGiven = entry.category == "LOAN_DISBURSEMENT" ||
                            (loan.direction == "GIVEN" && entry.entryType == "DEBIT") ||
                            (loan.direction == "TAKEN" && entry.category == "LOAN_BORROWING")

                    val cardAlignment = if (isLoanGiven) Alignment.CenterStart else Alignment.CenterEnd
                    val badgeColor = if (isLoanGiven) Color(0xFFE8604C) else Color(0xFF00A884)
                    val badgeIcon = if (isLoanGiven) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

                    val topLabel = if (isLoanGiven) {
                        if (loan.direction == "TAKEN") "Loan taken on" else "Loan given on"
                    } else {
                        if (loan.direction == "TAKEN") "Repaid on" else "Collected on"
                    }

                    val bottomLabel = if (isLoanGiven) "Amount disbursed" else "Payment made"
                    val formattedDate = formatOrdinalTransactionDate(entry.timestamp)
                    val formattedAmount = formatTransactionAmount(entry.amount, currencySymbol)

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = cardAlignment
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { previewEntry = entry }
                                .testTag("txn_card_${entry.id}"),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                // Top Row: Badge + "Loan given on" / "Collected on" + Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = badgeIcon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = topLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = formattedDate,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF475569)
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp
                                )

                                // Bottom Row: "Amount disbursed" / "Payment made" + Amount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = bottomLabel,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF334155)
                                    )

                                    Text(
                                        text = formattedAmount,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Preview Dialog when tapped
    previewEntry?.let { entry ->
        TransactionPreviewDialog(
            entry = entry,
            loan = loan,
            currencySymbol = currencySymbol,
            onDismiss = { previewEntry = null }
        )
    }
}

/**
 * Transaction Preview voucher displayed when tapping any transaction in the list.
 */
@Composable
private fun TransactionPreviewDialog(
    entry: LedgerEntry,
    loan: Loan,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isLoanGiven = entry.category == "LOAN_DISBURSEMENT" ||
            (loan.direction == "GIVEN" && entry.entryType == "DEBIT") ||
            (loan.direction == "TAKEN" && entry.category == "LOAN_BORROWING")

    val fullDate = remember(entry.timestamp) {
        SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(entry.timestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isLoanGiven) Color(0xFFE8604C) else Color(0xFF00A884)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLoanGiven) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoanGiven) "Disbursement Preview" else "Payment Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isLoanGiven) "AMOUNT DISBURSED" else "AMOUNT COLLECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTransactionAmount(entry.amount, currencySymbol),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLoanGiven) Color(0xFFE8604C) else Color(0xFF00A884)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transaction ID:", fontSize = 11.sp, color = TextMuted)
                            Text(entry.referenceCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date & Time:", fontSize = 11.sp, color = TextMuted)
                            Text(fullDate, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Party Name:", fontSize = 11.sp, color = TextMuted)
                            Text(loan.borrowerName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Loan Reference:", fontSize = 11.sp, color = TextMuted)
                            Text(loan.loanNumber, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        if (entry.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Description:", fontSize = 11.sp, color = TextMuted)
                                Text(entry.description, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Transaction voucher shared", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Share Voucher", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Dialog: Mobile Lock Simulation & Device Security
@Composable
private fun MobileLockDialog(
    borrowerName: String,
    onDismiss: () -> Unit,
    onFeedback: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isLocked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFCA8A04))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mobile Device Lock", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Protect your loan capital by remotely securing borrower device for $borrowerName.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF9C3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Status: ${if (isLocked) "ACTIVE & LOCKED" else "UNLOCKED (STANDBY)"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Device PIN code protection enabled. Overdue alerts will pop up on borrower screen.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Feature Feedback Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFFCA8A04),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feature Feedback",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We plan to implement this feature based on user requests. Please share your feedback: Would you like this feature?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFeedback(true)
                    Toast.makeText(context, "Thank you! Your feedback has been recorded.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA8A04)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("btn_mobile_lock_yes")
            ) {
                Text("YES", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onFeedback(false)
                    Toast.makeText(context, "Thank you for your feedback.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_mobile_lock_no")
            ) {
                Text("NO", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}

// Dialog: Secure Loan with Estamp
@Composable
private fun EstampDialog(
    loan: Loan,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onFeedback: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val certNumber = remember { "IN-DL${(loan.id * 73931L + 54321L).toString().takeLast(10)}" }
    val issueDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(loan.startDate))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF6366F1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Loan with Estamp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Legally binding digital e-stamp certificate and promissory note contract for ${loan.borrowerName}.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // E-Stamp Certificate Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEDE9FE),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GOVERNMENT E-STAMP",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color(0xFF4338CA)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4338CA)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFFC7D2FE))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Certificate No:", fontSize = 11.sp, color = TextMuted)
                            Text(certNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Issued Date:", fontSize = 11.sp, color = TextMuted)
                            Text(issueDate, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Principal Value:", fontSize = 11.sp, color = TextMuted)
                            Text(formatCurrency(loan.principalAmount, currencySymbol), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FinanceGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stamp Duty Paid:", fontSize = 11.sp, color = TextMuted)
                            Text("$currencySymbol 100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("First Party (Lender):", fontSize = 11.sp, color = TextMuted)
                            Text("Account Holder (Lender)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Second Party (Borrower):", fontSize = 11.sp, color = TextMuted)
                            Text(loan.borrowerName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }

                // Legal Protection note
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Legally enforceable under the Stamp Act & Negotiable Instruments Act for recovery.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            lineHeight = 15.sp
                        )
                    }
                }

                // Feature Feedback Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feature Feedback",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We plan to implement this feature based on user requests. Please share your feedback: Would you like this feature?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFeedback(true)
                    Toast.makeText(context, "Thank you! Your feedback has been recorded.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("btn_estamp_yes")
            ) {
                Text("YES", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onFeedback(false)
                    Toast.makeText(context, "Thank you for your feedback.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_estamp_no")
            ) {
                Text("NO", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}

// Dialog: Add Guarantor
@Composable
private fun AddGuarantorDialog(
    borrowerName: String,
    onDismiss: () -> Unit
) {
    var guarantorName by remember { mutableStateOf("") }
    var guarantorPhone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Family / Colleague") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Loan Guarantor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add up to 2 guarantors for $borrowerName's loan.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = guarantorName,
                    onValueChange = { guarantorName = it },
                    label = { Text("Guarantor Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = guarantorPhone,
                    onValueChange = { guarantorPhone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship / Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save Guarantor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Payment Plan
@Composable
private fun PaymentPlanDialog(
    loan: Loan,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    var installmentPeriod by remember { mutableStateOf("Monthly") }
    val emiAmount = if (loan.termMonths > 0) loan.balanceRemaining / loan.termMonths else loan.balanceRemaining

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set up Payment Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Configure installment intervals and automated collection reminders for this loan.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Estimated Installment:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${formatCurrency(emiAmount, currencySymbol)} / month",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Duration: ${loan.termMonths} months • Due on monthly anniversary",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Confirm Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Dialog: Add Tag
@Composable
private fun AddTagDialog(
    currentTags: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tagInput by remember { mutableStateOf(currentTags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add / Edit Tags", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter tags separated by commas (e.g., Personal, Business, Urgent)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Tags") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(tagInput.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
