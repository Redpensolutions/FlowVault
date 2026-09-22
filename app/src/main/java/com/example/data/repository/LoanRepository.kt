package com.example.data.repository

import com.example.data.dao.LoanDao
import com.example.data.dao.UserTelemetryDao
import com.example.data.database.PreloadData
import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingCalculations
import com.example.data.model.Loan
import com.example.data.model.UserTelemetryEntity
import com.example.data.model.VirtualAccount
import com.example.data.security.VaultDataSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class LoanRepository(
    private val loanDao: LoanDao,
    private val telemetryDao: UserTelemetryDao? = null
) {

    val allLoans: Flow<List<Loan>> = loanDao.getAllLoans()
    val allBorrowers: Flow<List<Borrower>> = loanDao.getAllBorrowers()
    val allVirtualAccounts: Flow<List<VirtualAccount>> = loanDao.getAllVirtualAccounts()
    val allLedgerEntries: Flow<List<LedgerEntry>> = loanDao.getAllLedgerEntries()
    val allUserTelemetry: Flow<List<UserTelemetryEntity>> =
        telemetryDao?.getAllUserTelemetryFlow() ?: emptyFlow()

    suspend fun getAllUserTelemetryList(): List<UserTelemetryEntity> = withContext(Dispatchers.IO) {
        telemetryDao?.getAllUserTelemetry() ?: emptyList()
    }

    suspend fun recordUserTelemetry(
        userId: String,
        userName: String,
        userEmail: String?,
        userPhone: String?,
        authProvider: String,
        accountsCount: Int,
        loansCount: Int,
        totalLoanAmount: Double
    ) = withContext(Dispatchers.IO) {
        val existing = telemetryDao?.getUserTelemetryById(userId)
        val now = System.currentTimeMillis()
        val entity = UserTelemetryEntity(
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            authProvider = authProvider,
            firstLoginTimestamp = existing?.firstLoginTimestamp ?: now,
            lastActiveTimestamp = now,
            totalAccountsCreated = accountsCount,
            totalLoansCreated = loansCount,
            totalLoanAmount = totalLoanAmount,
            feedbackCount = existing?.feedbackCount ?: 0,
            mobileLockFeedbackCount = existing?.mobileLockFeedbackCount ?: 0,
            estampFeedbackCount = existing?.estampFeedbackCount ?: 0,
            positiveFeedbackCount = existing?.positiveFeedbackCount ?: 0,
            status = "ACTIVE"
        )
        telemetryDao?.upsertUserTelemetry(entity)
    }

    suspend fun recordFeatureFeedback(
        userId: String,
        featureName: String,
        isPositive: Boolean
    ) = withContext(Dispatchers.IO) {
        val mobileLockInc = if (featureName.contains("MOBILE", ignoreCase = true)) 1 else 0
        val estampInc = if (featureName.contains("ESTAMP", ignoreCase = true)) 1 else 0
        val posInc = if (isPositive) 1 else 0

        val existing = telemetryDao?.getUserTelemetryById(userId)
        if (existing == null) {
            telemetryDao?.upsertUserTelemetry(
                UserTelemetryEntity(
                    userId = userId,
                    userName = "User ${userId.takeLast(4)}",
                    feedbackCount = 1,
                    mobileLockFeedbackCount = mobileLockInc,
                    estampFeedbackCount = estampInc,
                    positiveFeedbackCount = posInc,
                    lastActiveTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            telemetryDao?.recordFeatureFeedback(
                userId = userId,
                mobileLockInc = mobileLockInc,
                estampInc = estampInc,
                posInc = posInc
            )
        }
    }

    fun getLoanById(id: Long): Flow<Loan?> = loanDao.getLoanById(id)

    fun getLedgerForAccount(vaId: Long): Flow<List<LedgerEntry>> =
        loanDao.getLedgerEntriesForAccount(vaId)

    fun getLedgerForLoan(loanId: Long): Flow<List<LedgerEntry>> =
        loanDao.getLedgerEntriesForLoan(loanId)

    suspend fun createBorrower(borrower: Borrower): Long = withContext(Dispatchers.IO) {
        loanDao.insertBorrower(borrower)
    }

    suspend fun createLoan(
        borrowerName: String,
        borrowerPhone: String,
        borrowerEmail: String,
        principalAmount: Double,
        annualInterestRate: Double,
        interestType: String,
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
        attachments: String = ""
    ): Long = withContext(Dispatchers.IO) {
        // 1. Create or match borrower
        val borrower = Borrower(
            name = borrowerName.trim(),
            phone = borrowerPhone.trim(),
            email = borrowerEmail.trim(),
            creditRating = "A",
            notes = "Registered via loan issuance"
        )
        val borrowerId = loanDao.insertBorrower(borrower)

        val daysDiff = ((dueDate - startDate) / (24L * 3600 * 1000)).toInt().coerceAtLeast(1)

        // 2. Perform lending calculation
        val calc = LendingCalculations.calculateLoanExtended(
            principal = principalAmount,
            ratePercent = annualInterestRate,
            interestType = interestType,
            termMonths = termMonths,
            interestPeriod = interestPeriod,
            interestBasis = interestBasis,
            hasInterest = hasInterest,
            daysCount = daysDiff
        )

        val loanNum = if (direction == "TAKEN") "LN-TK-${(1000..9999).random()}" else "LN-GV-${(1000..9999).random()}"

        val loan = Loan(
            loanNumber = loanNum,
            borrowerId = borrowerId,
            borrowerName = borrowerName.trim(),
            principalAmount = principalAmount,
            annualInterestRate = annualInterestRate,
            interestType = if (interestType.equals("COMPOUND", ignoreCase = true)) "COMPOUND" else if (interestType.equals("FLAT", ignoreCase = true)) "FLAT" else "SIMPLE",
            termMonths = termMonths,
            startDate = startDate,
            dueDate = dueDate,
            totalInterest = calc.totalInterest,
            totalAmountDue = calc.totalAmountDue,
            amountPaid = 0.0,
            status = "ACTIVE",
            virtualAccountId = disbursementAccountId,
            notes = notes,
            direction = direction,
            interestPeriod = interestPeriod,
            interestBasis = interestBasis,
            hasInterest = hasInterest,
            tags = tags,
            interestStartDate = interestStartDate,
            attachments = attachments
        )

        val newLoanId = loanDao.insertLoan(loan)

        // 3. If disbursement account selected, update liquidity and create ledger entry
        if (disbursementAccountId != null && disbursementAccountId > 0) {
            val account = loanDao.getVirtualAccountByIdSync(disbursementAccountId)
            if (account != null) {
                if (direction == "GIVEN") {
                    // Given loan: Funds disbursed OUT (Debit)
                    val updatedAccount = account.copy(
                        balance = account.balance - principalAmount
                    )
                    loanDao.updateVirtualAccount(updatedAccount)

                    val ledgerEntry = LedgerEntry(
                        virtualAccountId = disbursementAccountId,
                        loanId = newLoanId,
                        referenceCode = "TXN-${UUID.randomUUID().toString().take(6).uppercase()}",
                        entryType = "DEBIT",
                        amount = principalAmount,
                        category = "LOAN_DISBURSEMENT",
                        description = "Loan given to $borrowerName ($loanNum)",
                        timestamp = startDate
                    )
                    loanDao.insertLedgerEntry(ledgerEntry)
                } else {
                    // Taken loan: Funds received IN (Credit)
                    val updatedAccount = account.copy(
                        balance = account.balance + principalAmount
                    )
                    loanDao.updateVirtualAccount(updatedAccount)

                    val ledgerEntry = LedgerEntry(
                        virtualAccountId = disbursementAccountId,
                        loanId = newLoanId,
                        referenceCode = "TXN-${UUID.randomUUID().toString().take(6).uppercase()}",
                        entryType = "CREDIT",
                        amount = principalAmount,
                        category = "LOAN_BORROWING",
                        description = "Loan taken from $borrowerName ($loanNum)",
                        timestamp = startDate
                    )
                    loanDao.insertLedgerEntry(ledgerEntry)
                }
            }
        }

        newLoanId
    }

    suspend fun recordRepayment(
        loanId: Long,
        amount: Double,
        virtualAccountId: Long,
        referenceNotes: String = "",
        towardsPrincipal: Double = 0.0,
        towardsInterest: Double = 0.0,
        penaltyAmount: Double = 0.0,
        paymentMode: String = "CASH",
        paidDate: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        val loan = loanDao.getLoanByIdSync(loanId) ?: return@withContext false
        val account = loanDao.getVirtualAccountByIdSync(virtualAccountId) ?: return@withContext false

        val totalReceived = amount + penaltyAmount
        val newAmountPaid = loan.amountPaid + amount
        val newStatus = when {
            newAmountPaid >= loan.totalAmountDue - 0.01 -> "SETTLED"
            loan.dueDate < System.currentTimeMillis() -> "OVERDUE"
            else -> "ACTIVE"
        }

        val updatedLoan = loan.copy(
            amountPaid = newAmountPaid,
            status = newStatus
        )
        loanDao.updateLoan(updatedLoan)

        // Credit to the virtual account pool
        val updatedAccount = account.copy(
            balance = account.balance + totalReceived
        )
        loanDao.updateVirtualAccount(updatedAccount)

        val desc = buildString {
            append("Repayment for ${loan.loanNumber} (${loan.borrowerName})")
            if (towardsPrincipal > 0 || towardsInterest > 0) {
                append(" [Principal: ₹${String.format(java.util.Locale.US, "%.2f", towardsPrincipal)}, Interest: ₹${String.format(java.util.Locale.US, "%.2f", towardsInterest)}]")
            }
            if (penaltyAmount > 0) {
                append(" [Penalty: ₹${String.format(java.util.Locale.US, "%.2f", penaltyAmount)}]")
            }
            append(" [Mode: $paymentMode] [Pool: ${account.accountCode}]")
            if (referenceNotes.isNotBlank()) {
                append(" — $referenceNotes")
            }
        }

        // Record incoming credit ledger entry
        val ledger = LedgerEntry(
            virtualAccountId = virtualAccountId,
            loanId = loanId,
            referenceCode = "REP-${UUID.randomUUID().toString().take(6).uppercase()}",
            entryType = "CREDIT",
            amount = totalReceived,
            category = "REPAYMENT_ALLOCATION",
            description = desc,
            timestamp = paidDate
        )
        loanDao.insertLedgerEntry(ledger)

        true
    }

    suspend fun topUpLoan(
        loanId: Long,
        topUpAmount: Double,
        disbursementAccountId: Long?,
        calculateInterestOnTopUp: Boolean,
        note: String = "",
        topUpDate: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        val loan = loanDao.getLoanByIdSync(loanId) ?: return@withContext false
        val newPrincipal = loan.principalAmount + topUpAmount

        val (newTotalInterest, newTotalAmountDue) = if (calculateInterestOnTopUp && loan.hasInterest && loan.annualInterestRate > 0) {
            val daysDiff = ((loan.dueDate - topUpDate) / (24L * 3600 * 1000)).toInt().coerceAtLeast(1)
            val termMonthsRemaining = ((daysDiff / 30.0).coerceAtLeast(1.0)).toInt()
            val topUpCalc = LendingCalculations.calculateLoanExtended(
                principal = topUpAmount,
                ratePercent = loan.annualInterestRate,
                interestType = loan.interestType,
                termMonths = termMonthsRemaining,
                interestPeriod = loan.interestPeriod,
                interestBasis = loan.interestBasis,
                hasInterest = true,
                daysCount = daysDiff
            )
            Pair(loan.totalInterest + topUpCalc.totalInterest, loan.totalAmountDue + topUpAmount + topUpCalc.totalInterest)
        } else {
            Pair(loan.totalInterest, loan.totalAmountDue + topUpAmount)
        }

        val topUpNoteText = "Top-up: +${topUpAmount}${if (note.isNotBlank()) " ($note)" else ""}${if (calculateInterestOnTopUp) " [Int: Yes]" else " [Int: No]"}"
        val updatedNotes = if (loan.notes.isBlank()) topUpNoteText else "${loan.notes} • $topUpNoteText"

        val updatedLoan = loan.copy(
            principalAmount = newPrincipal,
            totalInterest = newTotalInterest,
            totalAmountDue = newTotalAmountDue,
            notes = updatedNotes
        )
        loanDao.updateLoan(updatedLoan)

        // Disburse from virtual account if selected
        if (disbursementAccountId != null && disbursementAccountId > 0) {
            val account = loanDao.getVirtualAccountByIdSync(disbursementAccountId)
            if (account != null) {
                val updatedAccount = account.copy(balance = account.balance - topUpAmount)
                loanDao.updateVirtualAccount(updatedAccount)

                val ledgerEntry = LedgerEntry(
                    virtualAccountId = disbursementAccountId,
                    loanId = loan.id,
                    referenceCode = "TOP-${UUID.randomUUID().toString().take(6).uppercase()}",
                    entryType = "DEBIT",
                    amount = topUpAmount,
                    category = "LOAN_TOPUP",
                    description = "Top-up disbursed for ${loan.loanNumber} (${loan.borrowerName})${if (note.isNotBlank()) " — $note" else ""}",
                    timestamp = topUpDate
                )
                loanDao.insertLedgerEntry(ledgerEntry)
            }
        }
        true
    }

    suspend fun updateLoanTags(loanId: Long, tags: String) = withContext(Dispatchers.IO) {
        val loan = loanDao.getLoanByIdSync(loanId) ?: return@withContext
        loanDao.updateLoan(loan.copy(tags = tags))
    }

    suspend fun markLoanSettled(loanId: Long) = withContext(Dispatchers.IO) {
        val loan = loanDao.getLoanByIdSync(loanId) ?: return@withContext
        loanDao.updateLoan(
            loan.copy(
                amountPaid = loan.totalAmountDue,
                status = "SETTLED"
            )
        )
    }

    suspend fun createVirtualAccount(
        name: String,
        code: String,
        poolType: String,
        initialBalance: Double,
        currency: String = "$",
        description: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val va = VirtualAccount(
            accountCode = code.trim().uppercase(),
            name = name.trim(),
            poolType = poolType,
            balance = initialBalance,
            currency = currency,
            description = description
        )
        val id = loanDao.insertVirtualAccount(va)

        if (initialBalance > 0) {
            loanDao.insertLedgerEntry(
                LedgerEntry(
                    virtualAccountId = id,
                    referenceCode = "INIT-${(1000..9999).random()}",
                    entryType = "CREDIT",
                    amount = initialBalance,
                    category = "POOL_TRANSFER",
                    description = "Initial capitalization for ${va.name}",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        id
    }

    suspend fun transferBetweenAccounts(
        sourceAccountId: Long,
        destinationAccountId: Long,
        amount: Double,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        val source = loanDao.getVirtualAccountByIdSync(sourceAccountId) ?: return@withContext false
        val dest = loanDao.getVirtualAccountByIdSync(destinationAccountId) ?: return@withContext false

        if (source.balance < amount) return@withContext false

        val updatedSource = source.copy(balance = source.balance - amount)
        val updatedDest = dest.copy(balance = dest.balance + amount)

        loanDao.updateVirtualAccount(updatedSource)
        loanDao.updateVirtualAccount(updatedDest)

        val txnRef = "TRF-${UUID.randomUUID().toString().take(6).uppercase()}"
        val now = System.currentTimeMillis()

        // Debit source
        loanDao.insertLedgerEntry(
            LedgerEntry(
                virtualAccountId = sourceAccountId,
                referenceCode = txnRef,
                entryType = "DEBIT",
                amount = amount,
                category = "POOL_TRANSFER",
                description = "Transfer out to ${dest.name} (${dest.accountCode}) - $reason",
                timestamp = now
            )
        )

        // Credit destination
        loanDao.insertLedgerEntry(
            LedgerEntry(
                virtualAccountId = destinationAccountId,
                referenceCode = txnRef,
                entryType = "CREDIT",
                amount = amount,
                category = "POOL_TRANSFER",
                description = "Transfer in from ${source.name} (${source.accountCode}) - $reason",
                timestamp = now
            )
        )

        true
    }

    suspend fun deleteVirtualAccount(va: VirtualAccount) = withContext(Dispatchers.IO) {
        loanDao.deleteVirtualAccount(va)
    }

    suspend fun resetAndPreloadSampleData() = withContext(Dispatchers.IO) {
        loanDao.clearLedgerEntries()
        loanDao.clearLoans()
        loanDao.clearVirtualAccounts()
        loanDao.clearBorrowers()
        PreloadData.seed(loanDao)
    }

    suspend fun takeFullSnapshot(): VaultDataSnapshot = withContext(Dispatchers.IO) {
        val borrowers = loanDao.getAllBorrowersList()
        val accounts = loanDao.getAllVirtualAccountsList()
        val loans = loanDao.getAllLoansList()
        val entries = loanDao.getAllLedgerEntriesList()

        VaultDataSnapshot(
            version = 1,
            appName = "FlowVault",
            timestamp = System.currentTimeMillis(),
            borrowers = borrowers,
            virtualAccounts = accounts,
            loans = loans,
            ledgerEntries = entries
        )
    }

    suspend fun restoreFromSnapshot(snapshot: VaultDataSnapshot) = withContext(Dispatchers.IO) {
        loanDao.restoreAllData(
            borrowers = snapshot.borrowers,
            virtualAccounts = snapshot.virtualAccounts,
            loans = snapshot.loans,
            ledgerEntries = snapshot.ledgerEntries
        )
    }
}
