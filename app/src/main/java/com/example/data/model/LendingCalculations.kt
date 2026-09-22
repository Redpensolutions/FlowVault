package com.example.data.model

import java.util.Calendar
import kotlin.math.pow
import kotlin.math.roundToInt

data class CalculationResult(
    val principal: Double,
    val interestRate: Double,
    val interestType: String,
    val termMonths: Int,
    val totalInterest: Double,
    val totalAmountDue: Double,
    val monthlyPayment: Double,
    val effectiveAnnualRate: Double,
    val interestPeriod: String = "YEARLY",
    val interestBasis: String = "MONTHS_BASED",
    val hasInterest: Boolean = true
)

data class RepaymentScheduleItem(
    val installmentNumber: Int,
    val dueDate: Long,
    val principalPortion: Double,
    val interestPortion: Double,
    val installmentTotal: Double,
    val remainingBalance: Double,
    val status: String // "PAID", "PENDING", "OVERDUE"
)

object LendingCalculations {

    fun calculateLoan(
        principal: Double,
        annualRatePercent: Double,
        interestType: String,
        termMonths: Int
    ): CalculationResult {
        return calculateLoanExtended(
            principal = principal,
            ratePercent = annualRatePercent,
            interestType = interestType,
            termMonths = termMonths,
            interestPeriod = "YEARLY",
            interestBasis = "MONTHS_BASED",
            hasInterest = true,
            daysCount = (termMonths * 30).coerceAtLeast(1)
        )
    }

    fun calculateLoanExtended(
        principal: Double,
        ratePercent: Double,
        interestType: String,
        termMonths: Int,
        interestPeriod: String = "MONTHLY", // "MONTHLY" or "YEARLY"
        interestBasis: String = "MONTHS_BASED", // "DAYS_BASED" or "MONTHS_BASED"
        hasInterest: Boolean = true,
        daysCount: Int = (termMonths * 30).coerceAtLeast(1)
    ): CalculationResult {
        val safePrincipal = principal.coerceAtLeast(0.0)
        val safeMonths = termMonths.coerceAtLeast(1)
        val safeDays = daysCount.coerceAtLeast(1)

        if (!hasInterest || ratePercent <= 0.0) {
            val totalDue = safePrincipal
            val monthlyPay = totalDue / safeMonths
            return CalculationResult(
                principal = round2(safePrincipal),
                interestRate = 0.0,
                interestType = interestType,
                termMonths = safeMonths,
                totalInterest = 0.0,
                totalAmountDue = round2(totalDue),
                monthlyPayment = round2(monthlyPay),
                effectiveAnnualRate = 0.0,
                interestPeriod = interestPeriod,
                interestBasis = interestBasis,
                hasInterest = false
            )
        }

        // Convert rate to annual equivalent if monthly rate was entered
        val annualRate = if (interestPeriod.equals("MONTHLY", ignoreCase = true)) {
            ratePercent * 12.0
        } else {
            ratePercent
        }

        val totalInterest: Double
        val totalDue: Double
        val monthlyPayment: Double

        when {
            interestType.equals("COMPOUND", ignoreCase = true) -> {
                if (interestBasis.equals("DAYS_BASED", ignoreCase = true)) {
                    val dailyRate = (annualRate / 100.0) / 365.0
                    val factor = (1.0 + dailyRate).pow(safeDays.toDouble())
                    totalDue = safePrincipal * factor
                    totalInterest = totalDue - safePrincipal
                } else {
                    val monthlyRate = (annualRate / 100.0) / 12.0
                    val factor = (1.0 + monthlyRate).pow(safeMonths.toDouble())
                    totalDue = safePrincipal * factor
                    totalInterest = totalDue - safePrincipal
                }
                monthlyPayment = totalDue / safeMonths
            }
            interestType.equals("FLAT", ignoreCase = true) -> {
                // Flat Interest: Direct flat rate applied over the period
                val durationYears = if (interestBasis.equals("DAYS_BASED", ignoreCase = true)) {
                    safeDays / 365.0
                } else {
                    safeMonths / 12.0
                }
                totalInterest = safePrincipal * (annualRate / 100.0) * durationYears
                totalDue = safePrincipal + totalInterest
                monthlyPayment = totalDue / safeMonths
            }
            else -> {
                // Simple Interest
                val durationYears = if (interestBasis.equals("DAYS_BASED", ignoreCase = true)) {
                    safeDays / 365.0
                } else {
                    safeMonths / 12.0
                }
                totalInterest = safePrincipal * (annualRate / 100.0) * durationYears
                totalDue = safePrincipal + totalInterest
                monthlyPayment = totalDue / safeMonths
            }
        }

        val effectiveRate = if (safePrincipal > 0) {
            val months = if (interestBasis.equals("DAYS_BASED", ignoreCase = true)) (safeDays / 30.0).coerceAtLeast(1.0) else safeMonths.toDouble()
            (totalInterest / safePrincipal) * (12.0 / months) * 100.0
        } else annualRate

        return CalculationResult(
            principal = round2(safePrincipal),
            interestRate = ratePercent,
            interestType = interestType.uppercase(),
            termMonths = safeMonths,
            totalInterest = round2(totalInterest),
            totalAmountDue = round2(totalDue),
            monthlyPayment = round2(monthlyPayment),
            effectiveAnnualRate = round2(effectiveRate),
            interestPeriod = interestPeriod.uppercase(),
            interestBasis = interestBasis.uppercase(),
            hasInterest = true
        )
    }

    fun generateSchedule(
        loan: Loan,
        currentPaid: Double = loan.amountPaid
    ): List<RepaymentScheduleItem> {
        val count = loan.termMonths.coerceAtLeast(1)
        val monthlyInstallment = loan.totalAmountDue / count
        val monthlyPrincipal = loan.principalAmount / count
        val monthlyInterest = loan.totalInterest / count

        val schedule = mutableListOf<RepaymentScheduleItem>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = loan.startDate

        var runningBalance = loan.totalAmountDue
        var accumulatedPaid = currentPaid
        val now = System.currentTimeMillis()

        for (i in 1..count) {
            cal.add(Calendar.MONTH, 1)
            val itemDueDate = cal.timeInMillis

            runningBalance = (runningBalance - monthlyInstallment).coerceAtLeast(0.0)

            val status = when {
                accumulatedPaid >= monthlyInstallment - 0.01 -> {
                    accumulatedPaid -= monthlyInstallment
                    "PAID"
                }
                itemDueDate < now -> {
                    "OVERDUE"
                }
                else -> {
                    "PENDING"
                }
            }

            schedule.add(
                RepaymentScheduleItem(
                    installmentNumber = i,
                    dueDate = itemDueDate,
                    principalPortion = round2(monthlyPrincipal),
                    interestPortion = round2(monthlyInterest),
                    installmentTotal = round2(monthlyInstallment),
                    remainingBalance = round2(runningBalance),
                    status = status
                )
            )
        }
        return schedule
    }

    fun round2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
