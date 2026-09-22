package com.example.data.database

import com.example.data.dao.LoanDao
import com.example.data.model.Borrower
import com.example.data.model.LedgerEntry
import com.example.data.model.LendingCalculations
import com.example.data.model.Loan
import com.example.data.model.VirtualAccount
import java.util.Calendar

object PreloadData {

    suspend fun seed(dao: LoanDao) {
        // 1. Seed Accounts
        val va1 = VirtualAccount(
            id = 1,
            accountCode = "VA-HDFC-101",
            name = "HDFC Current Account",
            poolType = "Bank account",
            balance = 185000.00,
            currency = "₹",
            description = "Primary operational banking account for disbursements",
            createdAt = System.currentTimeMillis() - 90L * 24 * 3600 * 1000
        )
        val va2 = VirtualAccount(
            id = 2,
            accountCode = "VA-COL-202",
            name = "Daily Collections Pool",
            poolType = "Collection Account",
            balance = 48500.00,
            currency = "₹",
            description = "Central receiving account for UPI & cash borrower repayments",
            createdAt = System.currentTimeMillis() - 60L * 24 * 3600 * 1000
        )
        val va3 = VirtualAccount(
            id = 3,
            accountCode = "VA-REC-303",
            name = "Recovery & Arrears Reserve",
            poolType = "Recovery Account",
            balance = 22400.00,
            currency = "₹",
            description = "Allocations from delayed accounts and penalty settlements",
            createdAt = System.currentTimeMillis() - 45L * 24 * 3600 * 1000
        )
        val va4 = VirtualAccount(
            id = 4,
            accountCode = "VA-EXP-404",
            name = "Operational Expense Account",
            poolType = "Expense Account",
            balance = 16200.00,
            currency = "₹",
            description = "Petty cash and legal documentation expenses",
            createdAt = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        )
        val va5 = VirtualAccount(
            id = 5,
            accountCode = "VA-OPS-505",
            name = "Lending Operations Reserve",
            poolType = "Operations account",
            balance = 120000.00,
            currency = "₹",
            description = "High liquidity pool reserved for new loan underwriting",
            createdAt = System.currentTimeMillis() - 15L * 24 * 3600 * 1000
        )

        dao.insertVirtualAccounts(listOf(va1, va2, va3, va4, va5))

        // 2. Seed Borrowers / Parties
        val b1 = Borrower(
            id = 1,
            name = "Rajesh Kumar",
            phone = "+91 98765 12340",
            email = "rajesh.kumar@email.com",
            creditRating = "A+",
            notes = "Hardware shop owner, prompt monthly payer."
        )
        val b2 = Borrower(
            id = 2,
            name = "Priya Sharma",
            phone = "+91 98765 23450",
            email = "priya.sharma@designs.in",
            creditRating = "A",
            notes = "Boutique entrepreneur, expansion funding."
        )
        val b3 = Borrower(
            id = 3,
            name = "Suresh Patel",
            phone = "+91 98765 34560",
            email = "suresh.patel@logistics.in",
            creditRating = "C",
            notes = "Vehicle repair loan. Follow up for delayed payment."
        )
        val b4 = Borrower(
            id = 4,
            name = "Anita Desai",
            phone = "+91 98765 45670",
            email = "anita.desai@school.org",
            creditRating = "A",
            notes = "Education assistance loan. Fully settled."
        )
        val b5 = Borrower(
            id = 5,
            name = "SBI Business Credit",
            phone = "+91 1800 11 2211",
            email = "support@sbi.co.in",
            creditRating = "AAA",
            notes = "Bank credit line taken for business capital expansion."
        )
        val b6 = Borrower(
            id = 6,
            name = "Samatha Vadina",
            phone = "+91 98765 77889",
            email = "samatha.vadina@family.in",
            creditRating = "A+",
            notes = "Family accounts - multiple given loans"
        )
        val b7 = Borrower(
            id = 7,
            name = "Amaravathi",
            phone = "+91 98765 88990",
            email = "amaravathi@personal.in",
            creditRating = "A+",
            notes = "Verified borrower with active and settled loans"
        )

        dao.insertBorrowers(listOf(b1, b2, b3, b4, b5, b6, b7))

        val now = System.currentTimeMillis()
        val dayMs = 24L * 3600 * 1000

        // 3. Seed Loans
        // Loan 1: Rajesh Kumar - Given Loan (Active, Simple Interest) -> You will get
        val calc1 = LendingCalculations.calculateLoanExtended(
            principal = 60000.0,
            ratePercent = 12.0,
            interestType = "SIMPLE",
            termMonths = 12,
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true
        )
        val l1 = Loan(
            id = 1,
            loanNumber = "LN-2026-001",
            borrowerId = 1,
            borrowerName = "Rajesh Kumar",
            principalAmount = 60000.0,
            annualInterestRate = 12.0,
            interestType = "SIMPLE",
            termMonths = 12,
            startDate = now - 60 * dayMs,
            dueDate = now + 300 * dayMs,
            totalInterest = calc1.totalInterest,
            totalAmountDue = calc1.totalAmountDue,
            amountPaid = 22400.00,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Shop inventory stock funding",
            direction = "GIVEN",
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Business, Stock",
            interestStartDate = now - 60 * dayMs
        )

        // Loan 2: Priya Sharma - Given Loan (Active, Compound Interest) -> You will get
        val calc2 = LendingCalculations.calculateLoanExtended(
            principal = 100000.0,
            ratePercent = 1.5,
            interestType = "COMPOUND",
            termMonths = 12,
            interestPeriod = "MONTHLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true
        )
        val l2 = Loan(
            id = 2,
            loanNumber = "LN-2026-002",
            borrowerId = 2,
            borrowerName = "Priya Sharma",
            principalAmount = 100000.0,
            annualInterestRate = 18.0,
            interestType = "COMPOUND",
            termMonths = 12,
            startDate = now - 45 * dayMs,
            dueDate = now + 320 * dayMs,
            totalInterest = calc2.totalInterest,
            totalAmountDue = calc2.totalAmountDue,
            amountPaid = 35000.00,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Boutique textile studio setup",
            direction = "GIVEN",
            interestPeriod = "MONTHLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Commercial, Equipment",
            interestStartDate = now - 45 * dayMs
        )

        // Loan 3: Suresh Patel - Given Loan (Overdue, Simple Interest) -> You will get
        val calc3 = LendingCalculations.calculateLoanExtended(
            principal = 25000.0,
            ratePercent = 14.0,
            interestType = "SIMPLE",
            termMonths = 6,
            interestPeriod = "YEARLY",
            interestBasis = "DAYS_BASED",
            hasInterest = true
        )
        val l3 = Loan(
            id = 3,
            loanNumber = "LN-2025-089",
            borrowerId = 3,
            borrowerName = "Suresh Patel",
            principalAmount = 25000.0,
            annualInterestRate = 14.0,
            interestType = "SIMPLE",
            termMonths = 6,
            startDate = now - 200 * dayMs,
            dueDate = now - 15 * dayMs, // Overdue
            totalInterest = calc3.totalInterest,
            totalAmountDue = calc3.totalAmountDue,
            amountPaid = 8000.00,
            status = "OVERDUE",
            virtualAccountId = 3,
            notes = "Vehicle engine repairs - Payment overdue 15 days",
            direction = "GIVEN",
            interestPeriod = "YEARLY",
            interestBasis = "DAYS_BASED",
            hasInterest = true,
            tags = "Emergency, Vehicle",
            interestStartDate = now - 200 * dayMs
        )

        // Loan 4: Anita Desai - Given Loan (Settled, Flat Interest) -> Settled tab
        val calc4 = LendingCalculations.calculateLoanExtended(
            principal = 30000.0,
            ratePercent = 10.0,
            interestType = "FLAT",
            termMonths = 6,
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true
        )
        val l4 = Loan(
            id = 4,
            loanNumber = "LN-2025-045",
            borrowerId = 4,
            borrowerName = "Anita Desai",
            principalAmount = 30000.0,
            annualInterestRate = 10.0,
            interestType = "FLAT",
            termMonths = 6,
            startDate = now - 210 * dayMs,
            dueDate = now - 30 * dayMs,
            totalInterest = calc4.totalInterest,
            totalAmountDue = calc4.totalAmountDue,
            amountPaid = calc4.totalAmountDue,
            status = "SETTLED",
            virtualAccountId = 2,
            notes = "Complete settlement via UPI transfer",
            direction = "GIVEN",
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Personal, Education",
            interestStartDate = now - 210 * dayMs
        )

        // Loan 5: SBI Business Credit - Taken Loan (Active, Simple Interest) -> You will pay
        val calc5 = LendingCalculations.calculateLoanExtended(
            principal = 80000.0,
            ratePercent = 9.5,
            interestType = "SIMPLE",
            termMonths = 24,
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true
        )
        val l5 = Loan(
            id = 5,
            loanNumber = "LN-TK-2026-11",
            borrowerId = 5,
            borrowerName = "SBI Business Credit",
            principalAmount = 80000.0,
            annualInterestRate = 9.5,
            interestType = "SIMPLE",
            termMonths = 24,
            startDate = now - 75 * dayMs,
            dueDate = now + 655 * dayMs,
            totalInterest = calc5.totalInterest,
            totalAmountDue = calc5.totalAmountDue,
            amountPaid = 15000.00,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Low-interest bank line for working liquidity",
            direction = "TAKEN",
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Bank, Facility",
            interestStartDate = now - 75 * dayMs
        )

        // Loans for Samatha Vadina (matching reference screenshot IMG_4225)
        val l6 = Loan(
            id = 6,
            loanNumber = "Loan-20",
            borrowerId = 6,
            borrowerName = "Samatha Vadina",
            principalAmount = 100000.0,
            annualInterestRate = 14.47,
            interestType = "SIMPLE",
            termMonths = 12,
            startDate = now - 240 * dayMs, // 8 months ago (~Jan 7, 2026)
            dueDate = now + 120 * dayMs,
            totalInterest = 14466.67,
            totalAmountDue = 114466.67,
            amountPaid = 0.0,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Samatha valla akkaki kavitha ki",
            direction = "GIVEN",
            interestPeriod = "MONTHLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Family",
            interestStartDate = now - 240 * dayMs
        )

        val l7 = Loan(
            id = 7,
            loanNumber = "Loan-13",
            borrowerId = 6,
            borrowerName = "Samatha Vadina",
            principalAmount = 100000.0,
            annualInterestRate = 18.0,
            interestType = "SIMPLE",
            termMonths = 24,
            startDate = now - 350 * dayMs,
            dueDate = now + 380 * dayMs,
            totalInterest = 34866.67,
            totalAmountDue = 134866.67,
            amountPaid = 0.0,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Medical and family needs",
            direction = "GIVEN",
            interestPeriod = "MONTHLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Family, Urgent",
            interestStartDate = now - 350 * dayMs
        )

        val l8 = Loan(
            id = 8,
            loanNumber = "Loan-17",
            borrowerId = 6,
            borrowerName = "Samatha Vadina",
            principalAmount = 100000.0,
            annualInterestRate = 18.0,
            interestType = "SIMPLE",
            termMonths = 24,
            startDate = now - 380 * dayMs,
            dueDate = now + 350 * dayMs,
            totalInterest = 37466.67,
            totalAmountDue = 137466.67,
            amountPaid = 0.0,
            status = "ACTIVE",
            virtualAccountId = 5,
            notes = "Property construction advance",
            direction = "GIVEN",
            interestPeriod = "MONTHLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Family, Home",
            interestStartDate = now - 380 * dayMs
        )

        // Loans for Amaravathi (matching reference screenshot)
        val calApr2021 = Calendar.getInstance().apply {
            set(2021, Calendar.APRIL, 15, 10, 0, 0)
        }
        val l9 = Loan(
            id = 9,
            loanNumber = "LN-2021-002",
            borrowerId = 7,
            borrowerName = "Amaravathi",
            principalAmount = 10000.0,
            annualInterestRate = 16.0,
            interestType = "SIMPLE",
            termMonths = 36,
            startDate = calApr2021.timeInMillis,
            dueDate = calApr2021.timeInMillis + 365L * 3 * dayMs,
            totalInterest = 5893.33,
            totalAmountDue = 15893.33,
            amountPaid = 0.0,
            status = "ACTIVE",
            virtualAccountId = 1,
            notes = "Personal assistance loan",
            direction = "GIVEN",
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            tags = "Personal",
            interestStartDate = calApr2021.timeInMillis
        )

        val calOct2023 = Calendar.getInstance().apply {
            set(2023, Calendar.OCTOBER, 29, 14, 0, 0)
        }
        val l10 = Loan(
            id = 10,
            loanNumber = "LN-2020-001",
            borrowerId = 7,
            borrowerName = "Amaravathi",
            principalAmount = 30000.0,
            annualInterestRate = 12.0,
            interestType = "SIMPLE",
            termMonths = 36,
            startDate = calOct2023.timeInMillis - 365L * 3 * dayMs,
            dueDate = calOct2023.timeInMillis,
            totalInterest = 0.0,
            totalAmountDue = 30000.0,
            amountPaid = 30000.0,
            status = "SETTLED",
            virtualAccountId = 2,
            notes = "Business loan fully settled",
            direction = "GIVEN",
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = false,
            tags = "Business",
            interestStartDate = calOct2023.timeInMillis - 365L * 3 * dayMs
        )

        dao.insertLoans(listOf(l1, l2, l3, l4, l5, l6, l7, l8, l9, l10))

        // 4. Seed Ledger Entries
        val entries = listOf(
            LedgerEntry(
                id = 1,
                virtualAccountId = 2,
                loanId = 1,
                referenceCode = "TXN-8810",
                entryType = "CREDIT",
                amount = 5600.00,
                category = "REPAYMENT_INFLOW",
                description = "Monthly installment received from Rajesh Kumar for LN-2026-001",
                timestamp = now - 2 * dayMs
            ),
            LedgerEntry(
                id = 2,
                virtualAccountId = 1,
                loanId = 2,
                referenceCode = "TXN-8821",
                entryType = "DEBIT",
                amount = 100000.00,
                category = "LOAN_DISBURSEMENT",
                description = "Loan disbursement to Priya Sharma for LN-2026-002",
                timestamp = now - 45 * dayMs
            ),
            LedgerEntry(
                id = 3,
                virtualAccountId = 1,
                loanId = 5,
                referenceCode = "TXN-8835",
                entryType = "DEBIT",
                amount = 4000.00,
                category = "REPAYMENT_OUTFLOW",
                description = "Monthly interest & principal paid to SBI Business Credit",
                timestamp = now - 10 * dayMs
            ),
            LedgerEntry(
                id = 4,
                virtualAccountId = 3,
                loanId = 3,
                referenceCode = "TXN-8849",
                entryType = "CREDIT",
                amount = 2000.00,
                category = "REPAYMENT_INFLOW",
                description = "Partial recovery tranche from Suresh Patel",
                timestamp = now - 18 * dayMs
            )
        )

        dao.insertLedgerEntries(entries)
    }
}
