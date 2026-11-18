import 'dart:math' as math;

import '../models/flow_vault_models.dart';

class InterestCalculator {
  InterestCalculator._();

  static double round2(double value) => (value * 100.0).roundToDouble() / 100.0;

  static CalculationResult calculate({
    required double principal,
    required double annualRatePercent,
    required InterestType interestType,
    required int termMonths,
    InterestPeriod interestPeriod = InterestPeriod.yearly,
    InterestBasis interestBasis = InterestBasis.monthsBased,
    int daysCount = 365,
  }) {
    final safeP = math.max(0.0, principal);
    final safeM = math.max(1, termMonths);
    final safeD = math.max(1, daysCount);

    if (interestType == InterestType.zero || annualRatePercent <= 0.0) {
      final monthlyPay = safeP / safeM;
      return CalculationResult(
        principal: round2(safeP),
        annualRatePercent: 0.0,
        interestType: InterestType.zero,
        termMonths: safeM,
        totalInterest: 0.0,
        totalDue: round2(safeP),
        monthlyPayment: round2(monthlyPay),
        effectiveRatePercent: 0.0,
        interestPeriod: interestPeriod,
        interestBasis: interestBasis,
        hasInterest: false,
      );
    }

    if (interestType == InterestType.simple) {
      final years = interestBasis == InterestBasis.daysBased
          ? safeD / 365.0
          : safeM / 12.0;
      final totalInterest = safeP * (annualRatePercent / 100.0) * years;
      final totalDue = safeP + totalInterest;
      final monthlyPay = totalDue / safeM;
      return CalculationResult(
        principal: round2(safeP),
        annualRatePercent: annualRatePercent,
        interestType: InterestType.simple,
        termMonths: safeM,
        totalInterest: round2(totalInterest),
        totalDue: round2(totalDue),
        monthlyPayment: round2(monthlyPay),
        effectiveRatePercent: round2(annualRatePercent),
        interestPeriod: interestPeriod,
        interestBasis: interestBasis,
        hasInterest: true,
      );
    }

    // Compound interest calculation matching CompoundCalculationHelper.java
    final r = annualRatePercent / 100.0;
    final n = interestPeriod.periodsPerYear;
    final ratePerPeriod = r / n;
    final numberOfPeriods = interestBasis == InterestBasis.daysBased
        ? safeD / (365.0 / n)
        : safeM / (12.0 / n);

    final factor = math.pow(1.0 + ratePerPeriod, numberOfPeriods).toDouble();
    final totalDue = safeP * factor;
    final totalInterest = totalDue - safeP;
    final monthlyPay = totalDue / safeM;
    final monthsForEffective = interestBasis == InterestBasis.daysBased
        ? math.max(safeD / 30.0, 1.0)
        : safeM.toDouble();
    final effectiveRate = safeP > 0
        ? (totalInterest / safeP) * (12.0 / monthsForEffective) * 100.0
        : annualRatePercent;

    return CalculationResult(
      principal: round2(safeP),
      annualRatePercent: annualRatePercent,
      interestType: InterestType.compound,
      termMonths: safeM,
      totalInterest: round2(totalInterest),
      totalDue: round2(totalDue),
      monthlyPayment: round2(monthlyPay),
      effectiveRatePercent: round2(effectiveRate),
      interestPeriod: interestPeriod,
      interestBasis: interestBasis,
      hasInterest: true,
    );
  }

  static DynamicLoanState computeDynamicState(
    Loan loan,
    List<LedgerEntry> allEntries, {
    DateTime? asOf,
  }) {
    final safeInitialPrincipal = math.max(0.0, loan.principalAmount);
    final targetDate = asOf ?? DateTime.now();
    final loanEntries = allEntries.where((e) => e.loanId == loan.id).toList();

    double currentPrincipal = safeInitialPrincipal;
    double totalInterestPaid = 0.0;
    double totalPrincipalPaid = 0.0;

    for (final entry in loanEntries) {
      if (entry.category == 'LOAN_TOPUP') {
        currentPrincipal += entry.amount;
      } else if (entry.category == 'LOAN_REPAYMENT') {
        final pPortion = _parseTaggedAmount(entry.description, 'Principal:');
        final iPortion = _parseTaggedAmount(entry.description, 'Interest:');
        if (pPortion != null) {
          totalPrincipalPaid += pPortion;
          currentPrincipal = math.max(0.0, currentPrincipal - pPortion);
        }
        if (iPortion != null) {
          totalInterestPaid += iPortion;
        }
        if (pPortion == null && iPortion == null) {
          totalPrincipalPaid += entry.amount;
          currentPrincipal = math.max(0.0, currentPrincipal - entry.amount);
        }
      }
    }

    final totalScheduledInterest =
        math.max(0.0, loan.totalDue - loan.principalAmount);
    final elapsedDays =
        math.max(0, targetDate.difference(loan.startDate).inDays);
    final totalTermDays =
        math.max(1, loan.dueDate.difference(loan.startDate).inDays);
    final accruedRatio = (elapsedDays / totalTermDays).clamp(0.0, 1.0);
    final accruedInterest = loan.interestType == InterestType.zero
        ? 0.0
        : round2(totalScheduledInterest * accruedRatio);

    final unpaidInterest =
        round2(math.max(0.0, accruedInterest - totalInterestPaid));
    final totalDueNow = round2(currentPrincipal + unpaidInterest);
    final balanceRemaining =
        loan.status == LoanStatus.paid ? 0.0 : loan.remainingBalance;

    return DynamicLoanState(
      initialPrincipal: round2(safeInitialPrincipal),
      currentPrincipal: round2(currentPrincipal),
      totalAccruedInterest: accruedInterest,
      totalInterestPaid: round2(totalInterestPaid),
      totalPrincipalPaid: round2(totalPrincipalPaid),
      unpaidInterest: unpaidInterest,
      totalDue: totalDueNow,
      balanceRemaining: round2(balanceRemaining),
    );
  }

  static double? _parseTaggedAmount(String desc, String prefix) {
    final regex = RegExp('${RegExp.escape(prefix)}\\s*[₹\$:]?\\s*([0-9]+(?:\\.[0-9]+)?)',
        caseSensitive: false);
    final match = regex.firstMatch(desc);
    if (match != null && match.group(1) != null) {
      return double.tryParse(match.group(1)!);
    }
    return null;
  }
}
