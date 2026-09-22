package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LedgerEntry
import com.example.data.model.VirtualAccount
import com.example.ui.components.PrimaryBlueButton
import com.example.ui.components.SecondaryBlueButton
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateTime
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
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealSlate
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun VirtualAccountsScreen(
    viewModel: LoanViewModel,
    onOpenAddVirtualAccount: () -> Unit,
    onOpenTransferFunds: (sourceId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.filteredAccounts.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allVirtualAccounts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.accountSearchQuery.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.allLedgerEntries.collectAsStateWithLifecycle()
    val selectedAccount by viewModel.selectedAccountForFilter.collectAsStateWithLifecycle()
    val currency by viewModel.defaultCurrency.collectAsStateWithLifecycle()

    var ledgerTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "CREDIT", "DEBIT"
    var accountToDelete by remember { mutableStateOf<VirtualAccount?>(null) }

    val filteredLedger = ledgerEntries.filter { entry ->
        val matchesAccount = selectedAccount == null || entry.virtualAccountId == selectedAccount?.id
        val matchesType = when (ledgerTypeFilter) {
            "CREDIT" -> entry.entryType == "CREDIT"
            "DEBIT" -> entry.entryType == "DEBIT"
            else -> true
        }
        matchesAccount && matchesType
    }

    val totalLiquidity = allAccounts.sumOf { it.balance }

    // Delete Account Confirmation Dialog
    if (accountToDelete != null) {
        val target = accountToDelete!!
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = {
                Text(
                    text = "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${target.name}' (${target.accountCode})? Existing transaction entries will be retained in ledger records.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVirtualAccount(target)
                        accountToDelete = null
                    }
                ) {
                    Text("Delete", color = FinanceRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundClean,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Round transfer symbol button above the adding + button
                FloatingActionButton(
                    onClick = { onOpenTransferFunds(null) },
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("fab_transfer_funds")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Transfer Funds",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Adding + button
                FloatingActionButton(
                    onClick = onOpenAddVirtualAccount,
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("fab_add_va")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Search box in accounts same as in loan accounts
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
                                    text = "Search account...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setAccountSearchQuery(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(BluePrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_account_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setAccountSearchQuery("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Balance Banner (Account button removed)
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderCard)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("Total Virtual Liquidity", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(totalLiquidity, currency),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BluePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Below: All Account Name and Balance Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accounts (${accounts.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (selectedAccount != null) {
                        Text(
                            text = "Filter active: ${selectedAccount?.name} (Tap card to reset)",
                            fontSize = 11.sp,
                            color = BluePrimary,
                            modifier = Modifier.clickable { viewModel.setSelectedAccount(null) }
                        )
                    }
                }
            }

            if (accounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No accounts found matching '$searchQuery'" else "No virtual accounts created yet.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(accounts, key = { "account_${it.id}" }) { acc ->
                    VirtualAccountCard(
                        account = acc,
                        currencySymbol = currency,
                        isSelected = selectedAccount?.id == acc.id,
                        onSelect = {
                            if (selectedAccount?.id == acc.id) {
                                viewModel.setSelectedAccount(null)
                            } else {
                                viewModel.setSelectedAccount(acc)
                            }
                        },
                        onDelete = {
                            accountToDelete = acc
                        }
                    )
                }
            }

            // Transaction Ledger Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedAccount != null) "Ledger: ${selectedAccount?.name}" else "Account Ledger Activity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Filter Tabs: All, Credit, Debit
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "CREDIT", "DEBIT").forEach { type ->
                            val isSel = ledgerTypeFilter == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) BluePrimary else SurfaceCard)
                                    .border(1.dp, if (isSel) BluePrimary else BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { ledgerTypeFilter = type }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Table Header: Left Particulars / Date, Right Actual Ledger (Dr / Cr)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PARTICULARS / DATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.4.sp
                    )
                    Text(
                        text = "ACTUAL LEDGER (DR / CR)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            if (filteredLedger.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Text(
                            text = "No ledger transactions match this filter.",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(filteredLedger, key = { "ledger_${it.id}" }) { entry ->
                    LedgerStyleRow(entry = entry, currencySymbol = currency)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun VirtualAccountCard(
    account: VirtualAccount,
    currencySymbol: String = "₹",
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .testTag("va_card_${account.accountCode}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BlueLight.copy(alpha = 0.35f) else SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) BluePrimary else BorderCard
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bank name and account type tag below it
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = account.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TealLight)
                            .border(BorderStroke(0.5.dp, TealSlate.copy(alpha = 0.25f)), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = account.poolType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealSlate,
                            letterSpacing = 0.2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCurrency(account.balance, currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balance >= 0) FinanceGreen else FinanceRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // 3 dots button for delete option
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("va_options_${account.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete Account",
                                    color = FinanceRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = FinanceRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            modifier = Modifier.testTag("va_delete_${account.id}")
                        )
                    }
                }
            }
        }

            if (account.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = account.description,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.accountCode,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Text(
                    text = if (isSelected) "Tap to clear filter" else "Tap to filter ledger",
                    fontSize = 9.sp,
                    color = if (isSelected) BluePrimary else TextMuted
                )
            }
        }
    }
}

/**
 * Professional accounting ledger style row:
 * Left side: Particulars / narration, timestamp, and reference code.
 * Right side: Actual ledger style with Credit (Cr) / Debit (Dr) accounting badge and formatted amount.
 */
@Composable
fun LedgerStyleRow(
    entry: LedgerEntry,
    currencySymbol: String = "₹"
) {
    val isCredit = entry.entryType == "CREDIT"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Particulars, Date, Reference
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDateTime(entry.timestamp),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.referenceCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: Actual Ledger Style
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Dr / Cr indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCredit) FinanceGreenLight else FinanceRedLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isCredit) "CREDIT (Cr)" else "DEBIT (Dr)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) FinanceGreen else FinanceRed
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${if (isCredit) "+" else "-"}${formatCurrency(entry.amount, currencySymbol)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCredit) FinanceGreen else FinanceRed
                )
            }
        }
    }
}
