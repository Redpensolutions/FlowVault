package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LendingCalculations
import com.example.data.model.Loan
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceGreenLight
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.FinanceRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LoanDetailsDialog(
    loan: Loan,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onRequestPayment: () -> Unit
) {
    val schedule = remember(loan) {
        LendingCalculations.generateSchedule(loan)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = loan.loanNumber,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary
                            )
                            Text(
                                text = loan.borrowerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(status = if (loan.isOverdue) "OVERDUE" else loan.status)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Overdue warning banner if past due
                if (loan.isOverdue) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FinanceRedLight)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = FinanceRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Overdue Alert: Maturity date passed (${formatDate(loan.dueDate)}). Outstanding: ${formatCurrency(loan.balanceRemaining, currencySymbol)}",
                                color = FinanceRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Balance Breakdown Card - Responsive 2x2 grid for real mobile devices
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueLight.copy(alpha = 0.35f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Balance & Lending Calculations Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 1: Principal and Interest
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Principal", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCurrency(loan.principalAmount, currencySymbol), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Interest (${loan.annualInterestRate}%)", fontSize = 11.sp, color = TextSecondary)
                                Text("+${formatCurrency(loan.totalInterest, currencySymbol)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinanceRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2: Total Repayable and Amount Paid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Repayable", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCurrency(loan.totalAmountDue, currencySymbol), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Total Paid", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCurrency(loan.amountPaid, currencySymbol), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinanceGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 3: Remaining Balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remaining Balance:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Text(
                                formatCurrency(loan.balanceRemaining, currencySymbol),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (loan.balanceRemaining > 0) FinanceRed else FinanceGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { loan.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = FinanceGreen,
                            trackColor = BorderSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule table header
                Text(
                    text = "Amortization & Repayment Schedule (${schedule.size} Mos)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontally scrollable table container for mobile
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.width(420.dp)) {
                        // Tabular header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderSubtle.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(28.dp))
                            Text("Due Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(84.dp))
                            Text("Principal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(72.dp))
                            Text("Interest", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(68.dp))
                            Text("Installment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(84.dp))
                            Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.width(70.dp))
                        }

                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                        // Schedule list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            schedule.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${item.installmentNumber}", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(28.dp))
                                    Text(formatDate(item.dueDate), fontSize = 11.sp, color = TextPrimary, modifier = Modifier.width(84.dp))
                                    Text(formatCurrency(item.principalPortion, currencySymbol), fontSize = 11.sp, color = TextPrimary, modifier = Modifier.width(72.dp))
                                    Text(formatCurrency(item.interestPortion, currencySymbol), fontSize = 11.sp, color = FinanceRed, modifier = Modifier.width(68.dp))
                                    Text(formatCurrency(item.installmentTotal, currencySymbol), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.width(84.dp))
                                    Box(modifier = Modifier.width(70.dp)) {
                                        StatusBadge(status = item.status)
                                    }
                                }
                                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.25f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryBlueButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        testTag = "close_loan_details_button"
                    )

                    if (loan.balanceRemaining > 0.01) {
                        PrimaryBlueButton(
                            text = "Repay",
                            icon = Icons.Default.Payments,
                            onClick = {
                                onDismiss()
                                onRequestPayment()
                            },
                            modifier = Modifier.weight(1.3f),
                            testTag = "open_payment_from_details_button"
                        )
                    }
                }
            }
        }
    }
}
