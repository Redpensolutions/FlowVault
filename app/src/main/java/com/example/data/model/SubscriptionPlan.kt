package com.example.data.model

/**
 * Subscription Plan Tier and Billing Cycle models for FlowVault.
 */
enum class PlanTier(
    val title: String,
    val maxActiveLoans: Int,
    val maxVirtualAccounts: Int,
    val allowsCompoundInterest: Boolean,
    val allowsPhotoAttachments: Boolean,
    val allowsPdfExport: Boolean,
    val allowsWhatsAppCard: Boolean,
    val driveSyncIncluded: Boolean,
    val autoSyncIncluded: Boolean,
    val emailSupportIncluded: Boolean
) {
    FREE(
        title = "Free Tier",
        maxActiveLoans = 5,
        maxVirtualAccounts = 3,
        allowsCompoundInterest = false,
        allowsPhotoAttachments = false,
        allowsPdfExport = false,
        allowsWhatsAppCard = false,
        driveSyncIncluded = false, // Rs 59 one-time purchase
        autoSyncIncluded = false,
        emailSupportIncluded = false
    ),
    PRO(
        title = "Pro Plan",
        maxActiveLoans = 59,
        maxVirtualAccounts = 999,
        allowsCompoundInterest = true,
        allowsPhotoAttachments = true,
        allowsPdfExport = true,
        allowsWhatsAppCard = true,
        driveSyncIncluded = true,
        autoSyncIncluded = true,
        emailSupportIncluded = true
    )
}

enum class BillingCycle(
    val cycleName: String,
    val pricePerMonth: Int,
    val billingText: String,
    val discountBadge: String? = null
) {
    MONTHLY(
        cycleName = "Monthly",
        pricePerMonth = 159,
        billingText = "₹159 / month",
        discountBadge = null
    ),
    YEARLY(
        cycleName = "Yearly",
        pricePerMonth = 99,
        billingText = "₹99 / month (billed ₹1,188/yr)",
        discountBadge = "SAVE 38%"
    )
}
