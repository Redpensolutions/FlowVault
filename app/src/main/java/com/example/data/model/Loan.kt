package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
    indices = [
        Index("borrowerId"),
        Index("virtualAccountId")
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanNumber: String, // e.g. "LN-2026-001"
    val borrowerId: Long,
    val borrowerName: String,
    val principalAmount: Double,
    val annualInterestRate: Double, // percentage, e.g. 8.5
    val interestType: String = "SIMPLE", // "SIMPLE", "COMPOUND", "FLAT"
    val termMonths: Int,
    val startDate: Long,
    val dueDate: Long,
    val totalInterest: Double,
    val totalAmountDue: Double,
    val amountPaid: Double = 0.0,
    val status: String = "ACTIVE", // "ACTIVE", "OVERDUE", "SETTLED"
    val virtualAccountId: Long? = null,
    val notes: String = "",
    val direction: String = "GIVEN", // "GIVEN" (lend: you will get) or "TAKEN" (borrow: you will pay)
    val interestPeriod: String = "MONTHLY", // "MONTHLY" or "YEARLY"
    val interestBasis: String = "MONTHS_BASED", // "DAYS_BASED" or "MONTHS_BASED"
    val hasInterest: Boolean = true,
    val tags: String = "",
    val interestStartDate: Long = startDate,
    val attachments: String = ""
) {
    val balanceRemaining: Double
        get() = (totalAmountDue - amountPaid).coerceAtLeast(0.0)

    val progressPercent: Float
        get() = if (totalAmountDue > 0) ((amountPaid / totalAmountDue).coerceIn(0.0, 1.0)).toFloat() else 0f

    val isOverdue: Boolean
        get() = status == "OVERDUE" || (balanceRemaining > 0.01 && System.currentTimeMillis() > dueDate)

    val monthlyInterestAmount: Double
        get() {
            if (!hasInterest || totalInterest <= 0) return 0.0
            val months = termMonths.coerceAtLeast(1)
            return (totalInterest / months).coerceAtLeast(0.0)
        }
}
