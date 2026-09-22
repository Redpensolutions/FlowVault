package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virtual_accounts")
data class VirtualAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountCode: String, // e.g. "VA-COLL-101"
    val name: String, // e.g. "HDFC Primary Current A/c"
    val poolType: String = "Bank account", // "Bank account", "Collection Account", "Recovery Account", "Expense Account", "Operations account"
    val balance: Double = 0.0,
    val currency: String = "₹",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val ACCOUNT_TYPES = listOf(
            "Bank account",
            "Collection Account",
            "Recovery Account",
            "Expense Account",
            "Operations account"
        )
    }
}
