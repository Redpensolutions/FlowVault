package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
    indices = [
        Index("virtualAccountId"),
        Index("loanId")
    ]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val virtualAccountId: Long,
    val loanId: Long? = null,
    val referenceCode: String, // e.g. "TXN-90284"
    val entryType: String, // "CREDIT" (Green, positive inflow) or "DEBIT" (Red, outflow)
    val amount: Double,
    val category: String, // "REPAYMENT_INFLOW", "LOAN_DISBURSEMENT", "POOL_TRANSFER", "PENALTY_FEE", "SETTLEMENT"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
