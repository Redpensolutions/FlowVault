package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_telemetry")
data class UserTelemetryEntity(
    @PrimaryKey
    val userId: String,
    val userName: String,
    val userEmail: String? = null,
    val userPhone: String? = null,
    val authProvider: String = "GOOGLE", // "GOOGLE" or "MOBILE"
    val firstLoginTimestamp: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val totalAccountsCreated: Int = 0,
    val totalLoansCreated: Int = 0,
    val totalLoanAmount: Double = 0.0,
    val feedbackCount: Int = 0,
    val mobileLockFeedbackCount: Int = 0,
    val estampFeedbackCount: Int = 0,
    val positiveFeedbackCount: Int = 0,
    val status: String = "ACTIVE"
)
