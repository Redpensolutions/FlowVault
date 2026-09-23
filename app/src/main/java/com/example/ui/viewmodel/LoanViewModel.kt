package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.auth.UserSession
import com.example.data.database.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.data.model.BillingCycle
import com.example.data.model.Borrower
import com.example.data.model.CalculationResult
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingCalculations
import com.example.data.model.Loan
import com.example.data.model.PlanTier
import com.example.data.model.UserTelemetryEntity
import com.example.data.model.VirtualAccount
import com.example.data.repository.LoanRepository
import com.example.data.security.CloudSyncState
import com.example.data.security.DriveSyncStatus
import com.example.data.security.FlowVaultSecurityManager
import com.example.data.security.LocalSecuredSpaceInfo
import com.example.data.sync.GoogleDriveSyncManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BorrowerLoanSummary(
    val borrowerName: String,
    val loanCount: Int,
    val totalPrincipal: Double,
    val totalNetDue: Double,
    val loans: List<Loan>,
    val isOverdue: Boolean,
    val direction: String
)

data class DashboardMetrics(
    val receivables: Double = 0.0,
    val youWillGet: Double = 0.0,
    val youWillPay: Double = 0.0,
    val accountsBalance: Double = 0.0,
    val thisMonthInterestReceivable: Double = 0.0,
    val thisMonthInterestPayable: Double = 0.0,
    val activeGivenCount: Int = 0,
    val activeTakenCount: Int = 0,
    val overdueCount: Int = 0,
    val settledCount: Int = 0,
    val activeBorrowersCount: Int = 0
)

class LoanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: LoanRepository = LoanRepository(db.loanDao(), db.userTelemetryDao())
    private val authManager = AuthManager.getInstance(application)

    val userSession: StateFlow<UserSession> = authManager.sessionState

    val allUserTelemetry: StateFlow<List<UserTelemetryEntity>> = repository.allUserTelemetry
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val driveSyncManager = GoogleDriveSyncManager(application)

    private val _driveSyncState = MutableStateFlow(driveSyncManager.getSyncState())
    val driveSyncState: StateFlow<CloudSyncState> = _driveSyncState

    private val _localSecuredSpaceInfo = MutableStateFlow(FlowVaultSecurityManager.getLocalSecuredInfo(application))
    val localSecuredSpaceInfo: StateFlow<LocalSecuredSpaceInfo> = _localSecuredSpaceInfo

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    val allLoans: StateFlow<List<Loan>> = repository.allLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBorrowers: StateFlow<List<Borrower>> = repository.allBorrowers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVirtualAccounts: StateFlow<List<VirtualAccount>> = repository.allVirtualAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLedgerEntries: StateFlow<List<LedgerEntry>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loan Filtering & search: Tabs: "GIVEN", "TAKEN", "SETTLED"
    val loanSearchQuery = MutableStateFlow("")
    val loanTab = MutableStateFlow("GIVEN") // "GIVEN", "TAKEN", "SETTLED"
    val loanStatusFilter = MutableStateFlow("ALL") // Optional sub-filter
    val loanSortBy = MutableStateFlow("DEFAULT") // "DEFAULT", "DUE_DATE", "AMOUNT_HIGH", "NAME"

    val filteredLoans: StateFlow<List<Loan>> = combine(
        allLoans,
        loanSearchQuery,
        loanTab,
        loanSortBy
    ) { loans, query, tab, sort ->
        val filtered = loans.filter { loan ->
            val matchesTab = when (tab) {
                "GIVEN" -> loan.direction == "GIVEN" && loan.status != "SETTLED"
                "TAKEN" -> loan.direction == "TAKEN" && loan.status != "SETTLED"
                "SETTLED" -> loan.status == "SETTLED"
                else -> true
            }

            val matchesQuery = query.isBlank() ||
                    loan.borrowerName.contains(query, ignoreCase = true) ||
                    loan.loanNumber.contains(query, ignoreCase = true) ||
                    loan.tags.contains(query, ignoreCase = true) ||
                    loan.notes.contains(query, ignoreCase = true)

            matchesTab && matchesQuery
        }

        when (sort) {
            "DUE_DATE" -> filtered.sortedBy { it.dueDate }
            "AMOUNT_HIGH" -> filtered.sortedByDescending { it.balanceRemaining }
            "NAME" -> filtered.sortedBy { it.borrowerName.lowercase() }
            else -> filtered.sortedByDescending { it.startDate }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped Borrower Summaries for the compact loan cards with loan counts
    val borrowerLoanSummaries: StateFlow<List<BorrowerLoanSummary>> = combine(
        filteredLoans,
        loanTab
    ) { loans, tab ->
        val map = linkedMapOf<String, MutableList<Loan>>()
        for (loan in loans) {
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
                direction = tab
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLoansForBorrower(borrowerName: String): Flow<List<Loan>> =
        allLoans.map { loans ->
            loans.filter { it.borrowerName.equals(borrowerName, ignoreCase = true) }
        }

    fun getLoanById(loanId: Long): Flow<Loan?> = repository.getLoanById(loanId)

    fun getLedgerForLoan(loanId: Long): Flow<List<LedgerEntry>> = repository.getLedgerForLoan(loanId)

    // Virtual Account search & filtering
    val accountSearchQuery = MutableStateFlow("")
    val filteredAccounts: StateFlow<List<VirtualAccount>> = combine(
        allVirtualAccounts,
        accountSearchQuery
    ) { accounts, query ->
        if (query.isBlank()) {
            accounts
        } else {
            accounts.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.accountCode.contains(query, ignoreCase = true) ||
                it.poolType.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard metrics derived reactively:
    // Receivables, you will get, you will pay, Accounts balance, this month interest receivable & payable
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allLoans,
        allVirtualAccounts,
        allBorrowers
    ) { loans, accounts, borrowers ->
        var receivablesPrincipal = 0.0
        var youWillGetTotal = 0.0
        var youWillPayTotal = 0.0
        var interestReceivableMonth = 0.0
        var interestPayableMonth = 0.0
        var givenCount = 0
        var takenCount = 0
        var overdueCount = 0
        var settledCount = 0

        for (loan in loans) {
            if (loan.status == "SETTLED") {
                settledCount++
                continue
            }

            if (loan.isOverdue) {
                overdueCount++
            }

            if (loan.direction == "GIVEN") {
                givenCount++
                receivablesPrincipal += loan.principalAmount
                youWillGetTotal += loan.balanceRemaining
                interestReceivableMonth += loan.monthlyInterestAmount
            } else {
                takenCount++
                youWillPayTotal += loan.balanceRemaining
                interestPayableMonth += loan.monthlyInterestAmount
            }
        }

        val totalAccountsBalance = accounts.sumOf { it.balance }

        DashboardMetrics(
            receivables = receivablesPrincipal,
            youWillGet = youWillGetTotal,
            youWillPay = youWillPayTotal,
            accountsBalance = totalAccountsBalance,
            thisMonthInterestReceivable = interestReceivableMonth,
            thisMonthInterestPayable = interestPayableMonth,
            activeGivenCount = givenCount,
            activeTakenCount = takenCount,
            overdueCount = overdueCount,
            settledCount = settledCount,
            activeBorrowersCount = borrowers.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Selection states for modal bottom sheets / inspection
    val selectedLoanForDetails = MutableStateFlow<Loan?>(null)
    val selectedAccountForFilter = MutableStateFlow<VirtualAccount?>(null)

    // System Settings preferences - Default Currency is ₹
    val defaultCurrency = MutableStateFlow("₹")
    val defaultInterestType = MutableStateFlow("SIMPLE") // "SIMPLE", "COMPOUND", "FLAT"
    val defaultInterestRate = MutableStateFlow(12.0)

    // Notification State & Management
    private val _notifications = MutableStateFlow<List<AppNotification>>(createDefaultNotificationsList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    val unreadNotificationCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun createDefaultNotificationsList(): List<AppNotification> {
        val now = System.currentTimeMillis()
        return listOf(
            AppNotification(
                id = "rem_init_1",
                title = "Monthly Repayment Due",
                message = "Monthly installment reminder for Rajiv Sharma (₹50,000 Loan). Next interest cycle due this week.",
                timestamp = now - 3600000L * 2,
                type = NotificationType.MONTHLY_REMINDER,
                borrowerName = "Rajiv Sharma",
                amount = 1000.0,
                isRead = false
            ),
            AppNotification(
                id = "rem_init_2",
                title = "Overdue Alert: Vikram Patel",
                message = "Loan for Vikram Patel is past the due date. Total outstanding: ₹1,20,000. Follow up recommended.",
                timestamp = now - 86400000L,
                type = NotificationType.OVERDUE_ALERT,
                borrowerName = "Vikram Patel",
                amount = 120000.0,
                isRead = false
            ),
            AppNotification(
                id = "rem_init_3",
                title = "Monthly Interest Notification",
                message = "Monthly interest accrual calculation completed for active loans across all accounts.",
                timestamp = now - 86400000L * 3,
                type = NotificationType.MONTHLY_REMINDER,
                borrowerName = "",
                amount = 0.0,
                isRead = true
            )
        )
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun triggerMonthlyReminderCheck() {
        val currentLoans = allLoans.value
        val now = System.currentTimeMillis()
        val newReminders = mutableListOf<AppNotification>()

        currentLoans.filter { it.balanceRemaining > 0 }.forEach { loan ->
            val reminderId = "rem_loan_${loan.id}_${System.currentTimeMillis() / (1000 * 3600 * 24)}"
            if (_notifications.value.none { it.id == reminderId }) {
                val reminder = AppNotification(
                    id = reminderId,
                    title = "Monthly Reminder: ${loan.borrowerName}",
                    message = "Monthly reminder for loan #${loan.id} (${loan.borrowerName}). Outstanding: ${defaultCurrency.value}${loan.balanceRemaining.toInt()}.",
                    timestamp = now,
                    type = NotificationType.MONTHLY_REMINDER,
                    loanId = loan.id,
                    borrowerName = loan.borrowerName,
                    amount = loan.balanceRemaining,
                    isRead = false
                )
                newReminders.add(reminder)
                NotificationHelper.showLoanNotification(
                    context = getApplication(),
                    notificationId = loan.id.toInt(),
                    title = reminder.title,
                    message = reminder.message,
                    subText = "Monthly Reminder"
                )
            }
        }

        if (newReminders.isNotEmpty()) {
            _notifications.value = newReminders + _notifications.value
        }
    }

    // User Account Profile & Security Settings
    private val securityPrefs = getApplication<Application>().getSharedPreferences("flowvault_security_prefs", Context.MODE_PRIVATE)

    // Subscription & Plan States
    val currentPlanTier = MutableStateFlow(
        if (securityPrefs.getString("user_plan_tier", "FREE") == "PRO") PlanTier.PRO else PlanTier.FREE
    )
    val currentBillingCycle = MutableStateFlow(
        try {
            BillingCycle.valueOf(securityPrefs.getString("user_billing_cycle", "YEARLY") ?: "YEARLY")
        } catch (e: Exception) {
            BillingCycle.YEARLY
        }
    )
    val driveSyncPurchasedInFree = MutableStateFlow(
        securityPrefs.getBoolean("drive_sync_purchased_in_free", false)
    )

    val isProPlan: StateFlow<Boolean> = currentPlanTier
        .map { it == PlanTier.PRO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, currentPlanTier.value == PlanTier.PRO)

    val isDriveSyncAccessible: StateFlow<Boolean> = combine(currentPlanTier, driveSyncPurchasedInFree) { tier, purchased ->
        tier == PlanTier.PRO || purchased
    }.stateIn(viewModelScope, SharingStarted.Eagerly, currentPlanTier.value == PlanTier.PRO || driveSyncPurchasedInFree.value)

    val currentPlan: StateFlow<String> = combine(currentPlanTier, currentBillingCycle) { tier, cycle ->
        if (tier == PlanTier.PRO) {
            "Pro Plan (${cycle.billingText})"
        } else {
            "Free Tier"
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        if (currentPlanTier.value == PlanTier.PRO) "Pro Plan (${currentBillingCycle.value.billingText})" else "Free Tier"
    )

    fun upgradeToPro(cycle: BillingCycle = BillingCycle.YEARLY) {
        securityPrefs.edit()
            .putString("user_plan_tier", "PRO")
            .putString("user_billing_cycle", cycle.name)
            .apply()
        currentBillingCycle.value = cycle
        currentPlanTier.value = PlanTier.PRO
    }

    fun downgradeToFree() {
        securityPrefs.edit()
            .putString("user_plan_tier", "FREE")
            .apply()
        currentPlanTier.value = PlanTier.FREE
    }

    fun purchaseDriveSyncInFree(onSuccess: () -> Unit = {}) {
        securityPrefs.edit()
            .putBoolean("drive_sync_purchased_in_free", true)
            .apply()
        driveSyncPurchasedInFree.value = true
        onSuccess()
    }

    val activeLoansCount: StateFlow<Int> = allLoans
        .map { loans -> loans.count { it.status != "SETTLED" && it.balanceRemaining > 0.01 } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun canAddActiveLoan(): Boolean {
        val activeCount = allLoans.value.count { it.status != "SETTLED" && it.balanceRemaining > 0.01 }
        return activeCount < currentPlanTier.value.maxActiveLoans
    }

    fun canAddVirtualAccount(): Boolean {
        val count = allVirtualAccounts.value.size
        return count < currentPlanTier.value.maxVirtualAccounts
    }

    val userName = MutableStateFlow("Admin User")
    val userEmail = MutableStateFlow("admin@redpensolutions.in")
    val userPhone = MutableStateFlow("+91 98765 43210")
    val pinSecurityEnabled = MutableStateFlow(securityPrefs.getBoolean("pin_enabled", false))
    val biometricSecurityEnabled = MutableStateFlow(securityPrefs.getBoolean("biometric_enabled", false))
    val faceUnlockEnabled = MutableStateFlow(securityPrefs.getBoolean("face_enabled", false))
    val securityPin = MutableStateFlow(securityPrefs.getString("security_pin", "1234") ?: "1234")
    val isAppLocked = MutableStateFlow(
        authManager.sessionState.value.isLoggedIn && (
            securityPrefs.getBoolean("pin_enabled", false) ||
            securityPrefs.getBoolean("biometric_enabled", false) ||
            securityPrefs.getBoolean("face_enabled", false)
        )
    )

    init {
        val initialSession = authManager.sessionState.value
        if (initialSession.isLoggedIn) {
            userName.value = initialSession.userName
            initialSession.userEmail?.let { userEmail.value = it }
            initialSession.userPhone?.let { userPhone.value = it }
        }
        viewModelScope.launch {
            authManager.sessionState.collect { session ->
                if (session.isLoggedIn) {
                    userName.value = session.userName
                    session.userEmail?.let { userEmail.value = it }
                    session.userPhone?.let { userPhone.value = it }
                    recordCurrentUserTelemetry()
                }
            }
        }
    }

    fun loginWithGoogle(email: String, name: String, phone: String? = null, idToken: String? = null) {
        val session = authManager.signInWithGoogle(email, name, phone, idToken)
        userName.value = session.userName
        session.userEmail?.let { userEmail.value = it }
        session.userPhone?.let { userPhone.value = it }
        recordCurrentUserTelemetry()
    }

    fun loginWithMobile(phone: String, name: String) {
        val session = authManager.signInWithMobile(phone, name)
        userName.value = session.userName
        session.userPhone?.let { userPhone.value = it }
        recordCurrentUserTelemetry()
    }

    fun logout() {
        authManager.signOut()
    }

    fun recordCurrentUserTelemetry() {
        viewModelScope.launch {
            val session = authManager.sessionState.value
            if (session.isLoggedIn && session.userId.isNotBlank()) {
                val loans = allLoans.value
                val accountsCount = allVirtualAccounts.value.size + allBorrowers.value.size
                val totalAmount = loans.sumOf { it.principalAmount }
                repository.recordUserTelemetry(
                    userId = session.userId,
                    userName = session.userName,
                    userEmail = session.userEmail,
                    userPhone = session.userPhone,
                    authProvider = session.authProvider,
                    accountsCount = accountsCount,
                    loansCount = loans.size,
                    totalLoanAmount = totalAmount
                )
            }
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        userName.value = name.trim()
        userEmail.value = email.trim()
        userPhone.value = phone.trim()
        val current = authManager.sessionState.value
        if (current.isLoggedIn) {
            authManager.updateProfile(name, email, phone)
            recordCurrentUserTelemetry()
        }
    }

    fun setPinSecurity(enabled: Boolean, pin: String = securityPin.value) {
        pinSecurityEnabled.value = enabled
        securityPrefs.edit().putBoolean("pin_enabled", enabled).apply()
        if (pin.isNotBlank()) {
            securityPin.value = pin
            securityPrefs.edit().putString("security_pin", pin).apply()
        }
    }

    fun setBiometricSecurity(enabled: Boolean) {
        biometricSecurityEnabled.value = enabled
        securityPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun setFaceUnlockSecurity(enabled: Boolean) {
        faceUnlockEnabled.value = enabled
        securityPrefs.edit().putBoolean("face_enabled", enabled).apply()
    }

    fun isSecurityActive(): Boolean {
        return pinSecurityEnabled.value || biometricSecurityEnabled.value || faceUnlockEnabled.value
    }

    fun onAppMinimized() {
        if (authManager.sessionState.value.isLoggedIn && isSecurityActive()) {
            isAppLocked.value = true
        }
    }

    fun lockApp() {
        isAppLocked.value = true
    }

    fun unlockApp() {
        isAppLocked.value = false
    }

    fun recordFeatureFeedback(featureName: String, isPositive: Boolean) {
        viewModelScope.launch {
            val session = authManager.sessionState.value
            val userId = if (session.isLoggedIn && session.userId.isNotBlank()) session.userId else "google_admin_redpensolutions_in"
            repository.recordFeatureFeedback(userId, featureName, isPositive)
        }
    }

    fun deleteAccountData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resetAndPreloadSampleData()
            userName.value = "Admin User"
            userEmail.value = "admin@redpensolutions.in"
            userPhone.value = "+91 98765 43210"
            securityPrefs.edit().clear().apply()
            pinSecurityEnabled.value = false
            biometricSecurityEnabled.value = false
            faceUnlockEnabled.value = false
            securityPin.value = "1234"
            isAppLocked.value = false
            onComplete()
        }
    }

    fun setLoanSearchQuery(query: String) {
        loanSearchQuery.value = query
    }

    fun setLoanTab(tab: String) {
        loanTab.value = tab
    }

    fun setLoanSortBy(sort: String) {
        loanSortBy.value = sort
    }

    fun setLoanStatusFilter(filter: String) {
        loanStatusFilter.value = filter
    }

    fun setAccountSearchQuery(query: String) {
        accountSearchQuery.value = query
    }

    fun setSelectedLoan(loan: Loan?) {
        selectedLoanForDetails.value = loan
    }

    fun setSelectedAccount(account: VirtualAccount?) {
        selectedAccountForFilter.value = account
    }

    fun createLoan(
        borrowerName: String,
        borrowerPhone: String,
        borrowerEmail: String,
        principal: Double,
        rate: Double,
        type: String,
        termMonths: Int,
        disbursementAccountId: Long?,
        notes: String = "",
        direction: String = "GIVEN",
        interestPeriod: String = "MONTHLY",
        interestBasis: String = "MONTHS_BASED",
        hasInterest: Boolean = true,
        tags: String = "",
        startDate: Long = System.currentTimeMillis(),
        dueDate: Long = System.currentTimeMillis() + (termMonths.toLong() * 30L * 24 * 3600 * 1000),
        interestStartDate: Long = startDate,
        attachments: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.createLoan(
                borrowerName = borrowerName,
                borrowerPhone = borrowerPhone,
                borrowerEmail = borrowerEmail,
                principalAmount = principal,
                annualInterestRate = rate,
                interestType = type,
                termMonths = termMonths,
                disbursementAccountId = disbursementAccountId,
                notes = notes,
                direction = direction,
                interestPeriod = interestPeriod,
                interestBasis = interestBasis,
                hasInterest = hasInterest,
                tags = tags,
                startDate = startDate,
                dueDate = dueDate,
                interestStartDate = interestStartDate,
                attachments = attachments
            )
            val newNotification = AppNotification(
                id = "rem_new_${System.currentTimeMillis()}",
                title = "Monthly Reminder Scheduled: $borrowerName",
                message = "Loan created for $borrowerName (${defaultCurrency.value}${principal.toInt()}) with 1-year due date. Monthly reminder cycle activated.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.MONTHLY_REMINDER,
                borrowerName = borrowerName,
                amount = principal,
                isRead = false
            )
            _notifications.value = listOf(newNotification) + _notifications.value
            NotificationHelper.showLoanNotification(
                context = getApplication(),
                notificationId = (System.currentTimeMillis() % 10000).toInt(),
                title = newNotification.title,
                message = newNotification.message,
                subText = "Monthly Reminder"
            )
            onComplete()
            recordCurrentUserTelemetry()
            triggerAutoSyncIfEnabled()
        }
    }

    fun createBorrower(
        borrower: Borrower,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.createBorrower(borrower)
            onComplete(id)
            recordCurrentUserTelemetry()
            triggerAutoSyncIfEnabled()
        }
    }

    fun recordPayment(
        loanId: Long,
        amount: Double,
        virtualAccountId: Long,
        notes: String = "",
        towardsPrincipal: Double = 0.0,
        towardsInterest: Double = 0.0,
        penaltyAmount: Double = 0.0,
        paymentMode: String = "CASH",
        paidDate: Long = System.currentTimeMillis(),
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordRepayment(
                loanId = loanId,
                amount = amount,
                virtualAccountId = virtualAccountId,
                referenceNotes = notes,
                towardsPrincipal = towardsPrincipal,
                towardsInterest = towardsInterest,
                penaltyAmount = penaltyAmount,
                paymentMode = paymentMode,
                paidDate = paidDate
            )
            val paymentNotif = AppNotification(
                id = "notif_pay_${System.currentTimeMillis()}",
                title = "Payment Received",
                message = "Recorded payment of ${defaultCurrency.value}${amount.toInt()} via $paymentMode.",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.PAYMENT_RECEIVED,
                loanId = loanId,
                amount = amount,
                isRead = false
            )
            _notifications.value = listOf(paymentNotif) + _notifications.value
            onComplete()
            triggerAutoSyncIfEnabled()
        }
    }

    fun updateLoanTags(loanId: Long, tags: String) {
        viewModelScope.launch {
            repository.updateLoanTags(loanId, tags)
        }
    }

    fun markLoanSettled(loanId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.markLoanSettled(loanId)
            onComplete()
        }
    }

    fun createVirtualAccount(
        name: String,
        code: String,
        poolType: String,
        balance: Double,
        description: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.createVirtualAccount(
                name = name,
                code = code,
                poolType = poolType,
                initialBalance = balance,
                currency = defaultCurrency.value,
                description = description
            )
            onComplete()
            recordCurrentUserTelemetry()
        }
    }

    fun deleteVirtualAccount(account: VirtualAccount, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (selectedAccountForFilter.value?.id == account.id) {
                selectedAccountForFilter.value = null
            }
            repository.deleteVirtualAccount(account)
            onComplete()
        }
    }

    fun transferBetweenAccounts(
        sourceId: Long,
        destId: Long,
        amount: Double,
        reason: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = repository.transferBetweenAccounts(sourceId, destId, amount, reason)
            onComplete(success)
        }
    }

    fun topUpLoan(
        loanId: Long,
        amount: Double,
        disbursementAccountId: Long?,
        calculateInterest: Boolean,
        note: String = "",
        topUpDate: Long = System.currentTimeMillis(),
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = repository.topUpLoan(
                loanId = loanId,
                topUpAmount = amount,
                disbursementAccountId = disbursementAccountId,
                calculateInterestOnTopUp = calculateInterest,
                note = note,
                topUpDate = topUpDate
            )
            onComplete(success)
        }
    }

    fun reloadSampleDemoData() {
        viewModelScope.launch {
            repository.resetAndPreloadSampleData()
        }
    }

    fun previewCalculation(
        principal: Double,
        rate: Double,
        type: String,
        months: Int,
        interestPeriod: String = "MONTHLY",
        interestBasis: String = "MONTHS_BASED",
        hasInterest: Boolean = true,
        daysCount: Int = (months * 30).coerceAtLeast(1)
    ): CalculationResult {
        return LendingCalculations.calculateLoanExtended(
            principal = principal,
            ratePercent = rate,
            interestType = type,
            termMonths = months,
            interestPeriod = interestPeriod,
            interestBasis = interestBasis,
            hasInterest = hasInterest,
            daysCount = daysCount
        )
    }

    // --- Secured Local Space & Google Drive Sync Operations ---

    fun refreshSecurityAndSyncStatus() {
        val app = getApplication<Application>()
        _localSecuredSpaceInfo.value = FlowVaultSecurityManager.getLocalSecuredInfo(app)
        _driveSyncState.value = driveSyncManager.getSyncState()
    }

    fun backupToLocalSecuredSpace(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val snapshot = repository.takeFullSnapshot()
                val encryptedPkg = FlowVaultSecurityManager.encryptSnapshot(snapshot)
                val file = FlowVaultSecurityManager.saveToLocalSecuredSpace(app, encryptedPkg)
                _localSecuredSpaceInfo.value = FlowVaultSecurityManager.getLocalSecuredInfo(app)
                val msg = "Encrypted backup saved to local secured vault (${snapshot.totalRecordsCount} records, ${file.length()} bytes)"
                onComplete?.invoke(true, msg)
            } catch (e: Exception) {
                onComplete?.invoke(false, "Local backup failed: ${e.message}")
            }
        }
    }

    fun restoreFromLocalSecuredSpace(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val pkg = FlowVaultSecurityManager.loadFromLocalSecuredSpace(app)
                    ?: return@launch onComplete(false, "No local secured backup found on this phone.")

                val snapshot = FlowVaultSecurityManager.decryptSnapshot(pkg)
                repository.restoreFromSnapshot(snapshot)
                refreshSecurityAndSyncStatus()
                onComplete(true, "Successfully restored ${snapshot.totalRecordsCount} records from local secured space.")
            } catch (e: Exception) {
                onComplete(false, "Restore failed: ${e.message}")
            }
        }
    }

    fun syncToGoogleDrive(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            _driveSyncState.value = _driveSyncState.value.copy(status = DriveSyncStatus.SYNCING)
            try {
                val app = getApplication<Application>()
                val snapshot = repository.takeFullSnapshot()
                val encryptedPkg = FlowVaultSecurityManager.encryptSnapshot(snapshot)
                FlowVaultSecurityManager.saveToLocalSecuredSpace(app, encryptedPkg)
                _localSecuredSpaceInfo.value = FlowVaultSecurityManager.getLocalSecuredInfo(app)

                val result = driveSyncManager.syncToGoogleDrive(encryptedPkg)
                if (result.isSuccess) {
                    driveSyncManager.setDriveSyncActivated(true)
                    val msg = result.getOrNull() ?: "Synced to Google Drive"
                    _driveSyncState.value = driveSyncManager.getSyncState().copy(
                        status = DriveSyncStatus.SUCCESS,
                        lastSyncMessage = msg
                    )
                    onComplete?.invoke(true, msg)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Google Drive sync error"
                    _driveSyncState.value = driveSyncManager.getSyncState().copy(
                        status = DriveSyncStatus.ERROR,
                        lastSyncMessage = err
                    )
                    onComplete?.invoke(false, err)
                }
            } catch (e: Exception) {
                _driveSyncState.value = _driveSyncState.value.copy(
                    status = DriveSyncStatus.ERROR,
                    lastSyncMessage = e.message ?: "Sync error"
                )
                onComplete?.invoke(false, e.message ?: "Sync error")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun restoreFromGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            _driveSyncState.value = _driveSyncState.value.copy(status = DriveSyncStatus.SYNCING)
            try {
                val app = getApplication<Application>()
                val result = driveSyncManager.restoreFromGoogleDrive()
                if (result.isSuccess) {
                    val pkg = result.getOrThrow()
                    val snapshot = FlowVaultSecurityManager.decryptSnapshot(pkg)
                    repository.restoreFromSnapshot(snapshot)
                    FlowVaultSecurityManager.saveToLocalSecuredSpace(app, pkg)
                    refreshSecurityAndSyncStatus()
                    val msg = "Restored ${snapshot.totalRecordsCount} records from Google Drive backup."
                    _driveSyncState.value = driveSyncManager.getSyncState().copy(
                        status = DriveSyncStatus.SUCCESS,
                        lastSyncMessage = msg
                    )
                    onComplete(true, msg)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Restore failed"
                    _driveSyncState.value = driveSyncManager.getSyncState().copy(
                        status = DriveSyncStatus.ERROR,
                        lastSyncMessage = err
                    )
                    onComplete(false, err)
                }
            } catch (e: Exception) {
                _driveSyncState.value = _driveSyncState.value.copy(
                    status = DriveSyncStatus.ERROR,
                    lastSyncMessage = e.message ?: "Restore failed"
                )
                onComplete(false, e.message ?: "Restore failed")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        driveSyncManager.setAutoSyncEnabled(enabled)
        _driveSyncState.value = driveSyncManager.getSyncState()
        if (enabled && _driveSyncState.value.isDriveSyncActivated) {
            syncToGoogleDrive()
        }
    }

    fun setDriveSyncActivated(activated: Boolean, onComplete: ((Boolean, String) -> Unit)? = null) {
        if (activated && !isDriveSyncAccessible.value) {
            onComplete?.invoke(false, "Google Drive Safe Backup requires a ₹59 one-time purchase on Free Tier, or is included in Pro Plan.")
            return
        }
        driveSyncManager.setDriveSyncActivated(activated)
        if (activated) {
            driveSyncManager.setAutoSyncEnabled(true)
            _driveSyncState.value = driveSyncManager.getSyncState()
            syncToGoogleDrive(onComplete)
        } else {
            _driveSyncState.value = driveSyncManager.getSyncState()
            onComplete?.invoke(true, "Google Drive sync deactivated. Data remains secured on phone.")
        }
    }

    fun updateGoogleAccountEmail(email: String) {
        driveSyncManager.setAccountEmail(email)
        _driveSyncState.value = driveSyncManager.getSyncState()
    }

    fun triggerAutoSyncIfEnabled() {
        // ALWAYS secure data locally in the phone's hardware-backed AES-256 vault
        backupToLocalSecuredSpace()

        // ONLY sync to Google Drive if the user has explicitly activated drive sync
        if (_driveSyncState.value.isDriveSyncActivated && _driveSyncState.value.isAutoSyncEnabled) {
            syncToGoogleDrive()
        }
    }
}
