package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.VirtualAccount
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVirtualAccountDialog(
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, poolType: String, balance: Double, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("ACC-${(100..999).random()}") }
    var selectedAccountType by remember { mutableStateOf("Bank account") }
    var balanceStr by remember { mutableStateOf("") }
    var ledgerNote by remember { mutableStateOf("") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountTypes = VirtualAccount.ACCOUNT_TYPES

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp)),
            color = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Virtual ledger account for tracking funds",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name *") },
                    placeholder = { Text("e.g. HDFC Bank, Cash Collection") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("va_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Initial Balance
                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Initial Balance ($currencySymbol) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("va_balance_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account Type Dropdown: Bank account, Collection Account, Recovery Account, Expense Account, Operations account
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        accountTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedAccountType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ledger Note / purpose
                OutlinedTextField(
                    value = ledgerNote,
                    onValueChange = { ledgerNote = it },
                    label = { Text("Ledger Note / purpose") },
                    placeholder = { Text("Purpose or notes for this account") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = FinanceRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryBlueButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        testTag = "cancel_add_va_button"
                    )

                    PrimaryBlueButton(
                        text = "Save",
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter an account name"
                                return@PrimaryBlueButton
                            }
                            val balance = balanceStr.toDoubleOrNull() ?: 0.0
                            val finalCode = "ACC-${name.take(3).uppercase()}-${(100..999).random()}"
                            onConfirm(name, finalCode, selectedAccountType, balance, ledgerNote)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "submit_add_va_button"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferFundsDialog(
    accounts: List<VirtualAccount>,
    currencySymbol: String = "$",
    preselectedSourceId: Long? = null,
    onDismiss: () -> Unit,
    onConfirmTransfer: (sourceId: Long, destId: Long, amount: Double, reason: String) -> Unit
) {
    var sourceId by remember {
        mutableStateOf(preselectedSourceId ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var destId by remember {
        mutableStateOf(
            accounts.firstOrNull { it.id != (preselectedSourceId ?: accounts.firstOrNull()?.id) }?.id
                ?: accounts.firstOrNull()?.id
                ?: 0L
        )
    }

    var amountStr by remember { mutableStateOf("1000") }
    var reason by remember { mutableStateOf("Rebalancing liquidity pool") }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }
    var destDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sourceAccount = accounts.firstOrNull { it.id == sourceId }
    val destAccount = accounts.firstOrNull { it.id == destId }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transfer Between Pools",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Reallocate liquidity across virtual sub-accounts",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source dropdown
                ExposedDropdownMenuBox(
                    expanded = sourceDropdownExpanded,
                    onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = sourceAccount?.let { "${it.name} (Bal: $currencySymbol${it.balance})" } ?: "Select Source Pool",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Virtual Account (Source) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = sourceDropdownExpanded,
                        onDismissRequest = { sourceDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.accountCode}) - $currencySymbol${acc.balance}") },
                                onClick = {
                                    sourceId = acc.id
                                    sourceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Destination dropdown
                ExposedDropdownMenuBox(
                    expanded = destDropdownExpanded,
                    onExpandedChange = { destDropdownExpanded = !destDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = destAccount?.let { "${it.name} (Bal: $currencySymbol${it.balance})" } ?: "Select Target Pool",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Virtual Account (Destination) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = destDropdownExpanded,
                        onDismissRequest = { destDropdownExpanded = false }
                    ) {
                        accounts.filter { it.id != sourceId }.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.accountCode}) - $currencySymbol${acc.balance}") },
                                onClick = {
                                    destId = acc.id
                                    destDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Transfer Amount ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Transfer Ledger Purpose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = FinanceRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryBlueButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    PrimaryBlueButton(
                        text = "Execute Transfer",
                        icon = Icons.Default.SwapHoriz,
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (sourceId == destId) {
                                errorMessage = "Source and destination accounts must be different"
                                return@PrimaryBlueButton
                            }
                            if (amount <= 0) {
                                errorMessage = "Transfer amount must be positive"
                                return@PrimaryBlueButton
                            }
                            if (sourceAccount != null && sourceAccount.balance < amount) {
                                errorMessage = "Insufficient funds in source account (Balance: $currencySymbol${sourceAccount.balance})"
                                return@PrimaryBlueButton
                            }

                            onConfirmTransfer(sourceId, destId, amount, reason)
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
