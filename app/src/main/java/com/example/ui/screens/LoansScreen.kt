package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Loan
import com.example.ui.components.BorrowerAvatar
import com.example.ui.components.PrimaryBlueButton
import com.example.ui.components.SecondaryBlueButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderCard
import com.example.ui.theme.BorderGiven
import com.example.ui.theme.BorderOverdue
import com.example.ui.theme.BorderSettled
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderTaken
import com.example.ui.theme.FinanceAmber
import com.example.ui.theme.FinanceAmberLight
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceGreenLight
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.FinanceRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceInnerCard
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealSlate
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BorrowerLoanSummary
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun LoansScreen(
    viewModel: LoanViewModel,
    onOpenAddLoan: (direction: String) -> Unit,
    onSelectLoanForDetails: (Loan) -> Unit,
    onSelectLoanForPayment: (Loan) -> Unit,
    onSelectBorrower: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val loans by viewModel.filteredLoans.collectAsStateWithLifecycle()
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.loanSearchQuery.collectAsStateWithLifecycle()
    val currentTab by viewModel.loanTab.collectAsStateWithLifecycle()
    val currentSort by viewModel.loanSortBy.collectAsStateWithLifecycle()
    val currency by viewModel.defaultCurrency.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var activeStatusFilter by remember { mutableStateOf("ALL") }

    val givenCount = allLoans.count { it.direction == "GIVEN" && it.status != "SETTLED" }
    val takenCount = allLoans.count { it.direction == "TAKEN" && it.status != "SETTLED" }
    val settledCount = allLoans.count { it.status == "SETTLED" }

    // Apply quick status filter if set
    val displayedLoans = remember(loans, activeStatusFilter) {
        when (activeStatusFilter) {
            "ACTIVE" -> loans.filter { it.status == "ACTIVE" && !it.isOverdue }
            "OVERDUE" -> loans.filter { it.isOverdue }
            else -> loans
        }
    }

    // Group loans by borrower into clean borrower summaries
    val displayedSummaries = remember(displayedLoans, currentTab) {
        val map = linkedMapOf<String, MutableList<Loan>>()
        for (loan in displayedLoans) {
            map.getOrPut(loan.borrowerName) { mutableListOf() }.add(loan)
        }
        map.map { (name, bLoans) ->
            BorrowerLoanSummary(
                borrowerName = name,
                loanCount = bLoans.size,
                totalPrincipal = bLoans.sumOf { it.principalAmount },
                totalNetDue = bLoans.sumOf { it.balanceRemaining },
                loans = bLoans.sortedByDescending { it.startDate },
                isOverdue = bLoans.any { it.isOverdue },
                direction = currentTab
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundClean,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenAddLoan(if (currentTab == "TAKEN") "TAKEN" else "GIVEN") },
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 6.dp
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("fab_add_loan")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Loan",
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        bottomBar = {
            // Search box brought down above the menu options with reduced height
            Surface(
                color = BackgroundClean,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderCard, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search borrower, tag, loan #...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setLoanSearchQuery(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(BluePrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_loan_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setLoanSearchQuery("") },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Controls Row: Tabs on left, Sort and Filter on right
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tabs: TAKEN, GIVEN, SETTLED
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderCard, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf(
                            Triple("TAKEN", "Taken ($takenCount)", FinanceRed),
                            Triple("GIVEN", "Given ($givenCount)", BluePrimary),
                            Triple("SETTLED", "Settled ($settledCount)", FinanceGreen)
                        )

                        tabs.forEach { (tabKey, label, accentColor) ->
                            val isSelected = currentTab == tabKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) accentColor else Color.Transparent)
                                    .clickable { viewModel.setLoanTab(tabKey) }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Sort button with dropdown menu
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard)
                                .border(1.dp, if (currentSort != "DEFAULT") BluePrimary else BorderCard, RoundedCornerShape(10.dp))
                                .clickable { showSortMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sort loans",
                                tint = if (currentSort != "DEFAULT") BluePrimary else TextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recent First (Default)", fontWeight = if (currentSort == "DEFAULT") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setLoanSortBy("DEFAULT")
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Due Date (Upcoming)", fontWeight = if (currentSort == "DUE_DATE") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setLoanSortBy("DUE_DATE")
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Net Due Amount (High to Low)", fontWeight = if (currentSort == "AMOUNT_HIGH") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setLoanSortBy("AMOUNT_HIGH")
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Borrower Name (A to Z)", fontWeight = if (currentSort == "NAME") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setLoanSortBy("NAME")
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // Filter button with status dropdown
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard)
                                .border(1.dp, if (activeStatusFilter != "ALL") BluePrimary else BorderCard, RoundedCornerShape(10.dp))
                                .clickable { showFilterMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter loans",
                                tint = if (activeStatusFilter != "ALL") BluePrimary else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Status", fontWeight = if (activeStatusFilter == "ALL") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    activeStatusFilter = "ALL"
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Active Only", fontWeight = if (activeStatusFilter == "ACTIVE") FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    activeStatusFilter = "ACTIVE"
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Overdue Only", fontWeight = if (activeStatusFilter == "OVERDUE") FontWeight.Bold else FontWeight.Normal, color = FinanceRed) },
                                onClick = {
                                    activeStatusFilter = "OVERDUE"
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Loans Count Sub-header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentTab) {
                            "GIVEN" -> "Loans Given (Receivables)"
                            "TAKEN" -> "Loans Taken (Payables)"
                            else -> "Settled Loans History"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${displayedSummaries.size} record${if (displayedSummaries.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Loans List
            if (displayedSummaries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No ${currentTab.lowercase()} loans found",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "No records match '$searchQuery'" else "Tap + to add a loan record",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(displayedSummaries, key = { it.borrowerName }) { summary ->
                    BorrowerLoanCard(
                        summary = summary,
                        currencySymbol = currency,
                        onClick = { onSelectBorrower(summary.borrowerName) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Minimalist loan card as strictly requested:
 * Only borrower name and right top corner round circle with loan count,
 * below left principal amount, right below net due.
 * No given/active buttons, no schedule or record pay buttons, small card height.
 */
@Composable
fun BorrowerLoanCard(
    summary: BorrowerLoanSummary,
    currencySymbol: String = "₹",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("loan_card_${summary.borrowerName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Row: Borrower Name on left, Round circle with loan count on right top corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = summary.borrowerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                // Right top corner round circle with loan count
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2F7F4))
                        .border(1.2.dp, Color(0xFF00A896), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${summary.loanCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Left below Principal amount, Right below Net due
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Below Left: Principal amount
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Principal Amount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(summary.totalPrincipal, currencySymbol),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Right below: Net due
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Due",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(summary.totalNetDue, currencySymbol),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00A896)
                    )
                }
            }
        }
    }
}

/**
 * Minimalist LoanCard for single Loan instance, matching the exact required layout:
 * Name and right top corner round circle with loan count (1),
 * below left principal amount, right below net due.
 */
@Composable
fun LoanCard(
    loan: Loan,
    currencySymbol: String = "₹",
    onViewDetails: () -> Unit = {},
    onRecordPayment: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onViewDetails() }
            .testTag("loan_card_${loan.loanNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = loan.borrowerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2F7F4))
                        .border(1.2.dp, Color(0xFF00A896), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Principal Amount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(loan.principalAmount, currencySymbol),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Due",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(loan.balanceRemaining, currencySymbol),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00A896)
                    )
                }
            }
        }
    }
}
