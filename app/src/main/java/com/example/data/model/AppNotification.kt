package com.example.data.model

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType, // MONTHLY_REMINDER, OVERDUE_ALERT, PAYMENT_ALERT, SYSTEM_INFO
    val loanId: Long? = null,
    val borrowerName: String = "",
    val amount: Double = 0.0,
    val isRead: Boolean = false
)

enum class NotificationType {
    MONTHLY_REMINDER,
    OVERDUE_ALERT,
    PAYMENT_RECEIVED,
    SYSTEM_INFO
}
