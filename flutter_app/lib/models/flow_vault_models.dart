enum LoanDirection { given, taken }

enum LoanStatus { active, overdue, paid }

enum InterestType { simple, compound, zero }

enum InterestPeriod {
  yearly('YEARLY', 'Yearly', 1.0),
  halfYearly('HALF_YEARLY', 'Half yearly', 2.0),
  quarterly('QUARTERLY', 'Quarterly', 4.0),
  monthly('MONTHLY', 'Monthly', 12.0),
  weekly('WEEKLY', 'Weekly', 52.0);

  const InterestPeriod(this.key, this.label, this.periodsPerYear);
  final String key;
  final String label;
  final double periodsPerYear;
}

enum InterestBasis {
  monthsBased('MONTHS_BASED', 'Months'),
  daysBased('DAYS_BASED', 'Days');

  const InterestBasis(this.key, this.label);
  final String key;
  final String label;
}

class VirtualAccount {
  final int id;
  final String accountNumber;
  final String name;
  final String poolType;
  final double balance;
  final String currency;
  final String description;

  const VirtualAccount({
    required this.id,
    required this.accountNumber,
    required this.name,
    required this.poolType,
    required this.balance,
    this.currency = '₹',
    this.description = '',
  });

  VirtualAccount copyWith({
    int? id,
    String? accountNumber,
    String? name,
    String? poolType,
    double? balance,
    String? currency,
    String? description,
  }) {
    return VirtualAccount(
      id: id ?? this.id,
      accountNumber: accountNumber ?? this.accountNumber,
      name: name ?? this.name,
      poolType: poolType ?? this.poolType,
      balance: balance ?? this.balance,
      currency: currency ?? this.currency,
      description: description ?? this.description,
    );
  }
}

class Borrower {
  final int id;
  final String name;
  final String phone;
  final String email;
  final String rating;
  final String notes;

  const Borrower({
    required this.id,
    required this.name,
    required this.phone,
    required this.email,
    required this.rating,
    this.notes = '',
  });
}

class Loan {
  final int id;
  final String loanNumber;
  final String borrowerName;
  final double principalAmount;
  final double annualInterestRate;
  final InterestType interestType;
  final InterestPeriod interestPeriod;
  final InterestBasis interestBasis;
  final int termMonths;
  final int termDays;
  final DateTime startDate;
  final DateTime dueDate;
  final double totalDue;
  final double paidAmount;
  final LoanStatus status;
  final LoanDirection direction;
  final int virtualAccountId;
  final String notes;

  const Loan({
    required this.id,
    required this.loanNumber,
    required this.borrowerName,
    required this.principalAmount,
    required this.annualInterestRate,
    required this.interestType,
    this.interestPeriod = InterestPeriod.yearly,
    this.interestBasis = InterestBasis.monthsBased,
    required this.termMonths,
    this.termDays = 365,
    required this.startDate,
    required this.dueDate,
    required this.totalDue,
    required this.paidAmount,
    required this.status,
    required this.direction,
    required this.virtualAccountId,
    this.notes = '',
  });

  double get remainingBalance =>
      status == LoanStatus.paid ? 0.0 : (totalDue - paidAmount).clamp(0.0, double.infinity);

  double get progressRatio =>
      totalDue <= 0 ? 1.0 : (paidAmount / totalDue).clamp(0.0, 1.0);

  Loan copyWith({
    int? id,
    String? loanNumber,
    String? borrowerName,
    double? principalAmount,
    double? annualInterestRate,
    InterestType? interestType,
    InterestPeriod? interestPeriod,
    InterestBasis? interestBasis,
    int? termMonths,
    int? termDays,
    DateTime? startDate,
    DateTime? dueDate,
    double? totalDue,
    double? paidAmount,
    LoanStatus? status,
    LoanDirection? direction,
    int? virtualAccountId,
    String? notes,
  }) {
    return Loan(
      id: id ?? this.id,
      loanNumber: loanNumber ?? this.loanNumber,
      borrowerName: borrowerName ?? this.borrowerName,
      principalAmount: principalAmount ?? this.principalAmount,
      annualInterestRate: annualInterestRate ?? this.annualInterestRate,
      interestType: interestType ?? this.interestType,
      interestPeriod: interestPeriod ?? this.interestPeriod,
      interestBasis: interestBasis ?? this.interestBasis,
      termMonths: termMonths ?? this.termMonths,
      termDays: termDays ?? this.termDays,
      startDate: startDate ?? this.startDate,
      dueDate: dueDate ?? this.dueDate,
      totalDue: totalDue ?? this.totalDue,
      paidAmount: paidAmount ?? this.paidAmount,
      status: status ?? this.status,
      direction: direction ?? this.direction,
      virtualAccountId: virtualAccountId ?? this.virtualAccountId,
      notes: notes ?? this.notes,
    );
  }
}

class LedgerEntry {
  final int id;
  final int? loanId;
  final int virtualAccountId;
  final String category; // LOAN_DISBURSAL, LOAN_REPAYMENT, POOL_TRANSFER, POOL_DEPOSIT
  final double amount;
  final DateTime timestamp;
  final String description;

  const LedgerEntry({
    required this.id,
    this.loanId,
    required this.virtualAccountId,
    required this.category,
    required this.amount,
    required this.timestamp,
    required this.description,
  });
}

class CalculationResult {
  final double principal;
  final double annualRatePercent;
  final InterestType interestType;
  final int termMonths;
  final double totalInterest;
  final double totalDue;
  final double monthlyPayment;
  final double effectiveRatePercent;
  final InterestPeriod interestPeriod;
  final InterestBasis interestBasis;
  final bool hasInterest;

  const CalculationResult({
    required this.principal,
    required this.annualRatePercent,
    required this.interestType,
    required this.termMonths,
    required this.totalInterest,
    required this.totalDue,
    required this.monthlyPayment,
    required this.effectiveRatePercent,
    required this.interestPeriod,
    required this.interestBasis,
    required this.hasInterest,
  });
}

class DynamicLoanState {
  final double initialPrincipal;
  final double currentPrincipal;
  final double totalAccruedInterest;
  final double totalInterestPaid;
  final double totalPrincipalPaid;
  final double unpaidInterest;
  final double totalDue;
  final double balanceRemaining;

  const DynamicLoanState({
    required this.initialPrincipal,
    required this.currentPrincipal,
    required this.totalAccruedInterest,
    required this.totalInterestPaid,
    required this.totalPrincipalPaid,
    required this.unpaidInterest,
    required this.totalDue,
    required this.balanceRemaining,
  });
}
