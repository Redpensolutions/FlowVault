import 'package:flutter/foundation.dart';

import '../models/flow_vault_models.dart';
import '../services/interest_calculator.dart';

enum LoanSortOption { defaultOrder, amountHighToLow, dueSoonest, overdueFirst }

class FlowVaultStore extends ChangeNotifier {
  final List<VirtualAccount> _accounts = [
    const VirtualAccount(
      id: 1,
      accountNumber: 'VA-HDFC-101',
      name: 'HDFC Current Account',
      poolType: 'Bank account',
      balance: 450000.0,
      description: 'Primary operational banking account',
    ),
    const VirtualAccount(
      id: 2,
      accountNumber: 'VA-COL-202',
      name: 'Daily Collections Pool',
      poolType: 'Collection Account',
      balance: 182400.0,
      description: 'Central receiving account for UPI & cash',
    ),
    const VirtualAccount(
      id: 3,
      accountNumber: 'VA-REC-303',
      name: 'Recovery & Arrears Reserve',
      poolType: 'Recovery Account',
      balance: 64200.0,
      description: 'Allocations from delayed accounts',
    ),
    const VirtualAccount(
      id: 4,
      accountNumber: 'VA-OPS-505',
      name: 'Lending Operations Reserve',
      poolType: 'Operations account',
      balance: 820000.0,
      description: 'High liquidity pool reserved for new loans',
    ),
  ];

  final List<Borrower> _borrowers = [
    const Borrower(
      id: 1,
      name: 'Rajesh Kumar',
      phone: '+91 98765 12340',
      email: 'rajesh.kumar@email.com',
      rating: 'A+',
      notes: 'Hardware shop owner, prompt monthly payer',
    ),
    const Borrower(
      id: 2,
      name: 'Priya Sharma',
      phone: '+91 98765 23450',
      email: 'priya.sharma@designs.in',
      rating: 'A',
      notes: 'Boutique entrepreneur, expansion funding',
    ),
    const Borrower(
      id: 3,
      name: 'Suresh Patel',
      phone: '+91 98765 34560',
      email: 'suresh.patel@logistics.in',
      rating: 'B',
      notes: 'Logistics fleet operator',
    ),
    const Borrower(
      id: 4,
      name: 'Ananya Roy',
      phone: '+91 98765 45670',
      email: 'ananya.roy@pharma.com',
      rating: 'A+',
      notes: 'Wholesale pharmacy distributor',
    ),
    const Borrower(
      id: 5,
      name: 'Deepak Verma',
      phone: '+91 98765 56780',
      email: 'deepak.verma@metals.org',
      rating: 'C',
      notes: 'Metal fabrication unit',
    ),
  ];

  final List<Loan> _loans = [
    Loan(
      id: 1,
      loanNumber: 'LN-2026-001',
      borrowerName: 'Rajesh Kumar',
      principalAmount: 150000.0,
      annualInterestRate: 14.5,
      interestType: InterestType.simple,
      termMonths: 12,
      startDate: DateTime(2025, 12, 15),
      dueDate: DateTime(2026, 12, 15),
      totalDue: 171750.0,
      paidAmount: 128800.0,
      status: LoanStatus.active,
      direction: LoanDirection.given,
      virtualAccountId: 1,
    ),
    Loan(
      id: 2,
      loanNumber: 'LN-2026-002',
      borrowerName: 'Priya Sharma',
      principalAmount: 85000.0,
      annualInterestRate: 12.0,
      interestType: InterestType.simple,
      termMonths: 6,
      startDate: DateTime(2026, 1, 1),
      dueDate: DateTime(2026, 6, 30),
      totalDue: 90100.0,
      paidAmount: 90100.0,
      status: LoanStatus.paid,
      direction: LoanDirection.given,
      virtualAccountId: 1,
    ),
    Loan(
      id: 3,
      loanNumber: 'LN-2026-003',
      borrowerName: 'Suresh Patel',
      principalAmount: 220000.0,
      annualInterestRate: 18.0,
      interestType: InterestType.simple,
      termMonths: 24,
      startDate: DateTime(2024, 9, 10),
      dueDate: DateTime(2026, 9, 10),
      totalDue: 299200.0,
      paidAmount: 45000.0,
      status: LoanStatus.overdue,
      direction: LoanDirection.given,
      virtualAccountId: 2,
    ),
    Loan(
      id: 4,
      loanNumber: 'LN-2026-004',
      borrowerName: 'Ananya Roy',
      principalAmount: 310000.0,
      annualInterestRate: 13.5,
      interestType: InterestType.simple,
      termMonths: 18,
      startDate: DateTime(2025, 10, 1),
      dueDate: DateTime(2027, 4, 1),
      totalDue: 372775.0,
      paidAmount: 165000.0,
      status: LoanStatus.active,
      direction: LoanDirection.given,
      virtualAccountId: 4,
    ),
    Loan(
      id: 5,
      loanNumber: 'LN-2026-005',
      borrowerName: 'Deepak Verma',
      principalAmount: 120000.0,
      annualInterestRate: 20.0,
      interestType: InterestType.compound,
      interestPeriod: InterestPeriod.yearly,
      termMonths: 12,
      startDate: DateTime(2025, 10, 5),
      dueDate: DateTime(2026, 10, 5),
      totalDue: 144000.0,
      paidAmount: 24000.0,
      status: LoanStatus.overdue,
      direction: LoanDirection.given,
      virtualAccountId: 3,
    ),
  ];

  final List<LedgerEntry> _ledgerEntries = [
    LedgerEntry(
      id: 1,
      loanId: 1,
      virtualAccountId: 2,
      category: 'LOAN_REPAYMENT',
      amount: 14312.5,
      timestamp: DateTime(2026, 10, 1),
      description: 'Repayment from Rajesh Kumar (Principal: 12000, Interest: 2312.5)',
    ),
    LedgerEntry(
      id: 2,
      loanId: 4,
      virtualAccountId: 2,
      category: 'LOAN_REPAYMENT',
      amount: 20710.0,
      timestamp: DateTime(2026, 9, 28),
      description: 'Repayment from Ananya Roy (Principal: 17000, Interest: 3710)',
    ),
  ];

  // Filter & Search state
  String _searchQuery = '';
  LoanDirection _selectedDirection = LoanDirection.given;
  LoanStatus? _statusFilter;
  LoanSortOption _sortOption = LoanSortOption.defaultOrder;

  List<VirtualAccount> get accounts => List.unmodifiable(_accounts);
  List<Borrower> get borrowers => List.unmodifiable(_borrowers);
  List<Loan> get allLoans => List.unmodifiable(_loans);
  List<LedgerEntry> get ledgerEntries => List.unmodifiable(_ledgerEntries);

  String get searchQuery => _searchQuery;
  LoanDirection get selectedDirection => _selectedDirection;
  LoanStatus? get statusFilter => _statusFilter;
  LoanSortOption get sortOption => _sortOption;

  double get totalActivePortfolio => _loans
      .where((l) => l.status != LoanStatus.paid)
      .fold(0.0, (sum, l) => sum + l.principalAmount);

  double get totalCollected =>
      _loans.fold(0.0, (sum, l) => sum + l.paidAmount);

  double get totalOutstandingDue =>
      _loans.fold(0.0, (sum, l) => sum + l.remainingBalance);

  double get totalPoolLiquidity =>
      _accounts.fold(0.0, (sum, a) => sum + a.balance);

  int get activeLoansCount =>
      _loans.where((l) => l.status == LoanStatus.active).length;

  int get overdueLoansCount =>
      _loans.where((l) => l.status == LoanStatus.overdue).length;

  List<Loan> get filteredLoans {
    final query = _searchQuery.trim().toLowerCase();
    final result = _loans.where((loan) {
      if (loan.direction != _selectedDirection) return false;
      if (_statusFilter != null && loan.status != _statusFilter) return false;
      if (query.isNotEmpty) {
        final matchName = loan.borrowerName.toLowerCase().contains(query);
        final matchNum = loan.loanNumber.toLowerCase().contains(query);
        if (!matchName && !matchNum) return false;
      }
      return true;
    }).toList();

    switch (_sortOption) {
      case LoanSortOption.amountHighToLow:
        result.sort((a, b) => b.remainingBalance.compareTo(a.remainingBalance));
      case LoanSortOption.dueSoonest:
        result.sort((a, b) => a.dueDate.compareTo(b.dueDate));
      case LoanSortOption.overdueFirst:
        result.sort((a, b) => _statusWeight(a.status).compareTo(_statusWeight(b.status)));
      case LoanSortOption.defaultOrder:
        break;
    }
    return result;
  }

  int _statusWeight(LoanStatus status) {
    return switch (status) {
      LoanStatus.overdue => 0,
      LoanStatus.active => 1,
      LoanStatus.paid => 2,
    };
  }

  VirtualAccount? accountById(int id) {
    for (final acc in _accounts) {
      if (acc.id == id) return acc;
    }
    return null;
  }

  void setSearchQuery(String value) {
    _searchQuery = value;
    notifyListeners();
  }

  void setDirection(LoanDirection direction) {
    _selectedDirection = direction;
    notifyListeners();
  }

  void setStatusFilter(LoanStatus? status) {
    _statusFilter = status;
    notifyListeners();
  }

  void setSortOption(LoanSortOption option) {
    _sortOption = option;
    notifyListeners();
  }

  void createLoan({
    required String borrowerName,
    required double principal,
    required double annualRate,
    required InterestType interestType,
    required InterestPeriod interestPeriod,
    required InterestBasis interestBasis,
    required int termMonths,
    required int termDays,
    required LoanDirection direction,
    required int virtualAccountId,
    String notes = '',
  }) {
    final calc = InterestCalculator.calculate(
      principal: principal,
      annualRatePercent: annualRate,
      interestType: interestType,
      termMonths: termMonths,
      interestPeriod: interestPeriod,
      interestBasis: interestBasis,
      daysCount: termDays,
    );

    final newId = _loans.isEmpty ? 1 : _loans.map((l) => l.id).reduce((a, b) => a > b ? a : b) + 1;
    final now = DateTime.now();
    final dueDate = interestBasis == InterestBasis.daysBased
        ? now.add(Duration(days: termDays))
        : DateTime(now.year, now.month + termMonths, now.day);

    final newLoan = Loan(
      id: newId,
      loanNumber: 'LN-${now.year}-${newId.toString().padLeft(3, '0')}',
      borrowerName: borrowerName.trim(),
      principalAmount: calc.principal,
      annualInterestRate: calc.annualRatePercent,
      interestType: calc.interestType,
      interestPeriod: calc.interestPeriod,
      interestBasis: calc.interestBasis,
      termMonths: termMonths,
      termDays: termDays,
      startDate: now,
      dueDate: dueDate,
      totalDue: calc.totalDue,
      paidAmount: 0.0,
      status: LoanStatus.active,
      direction: direction,
      virtualAccountId: virtualAccountId,
      notes: notes.trim(),
    );

    _loans.insert(0, newLoan);

    // Ensure borrower exists in directory
    final exists = _borrowers.any(
      (b) => b.name.toLowerCase() == borrowerName.trim().toLowerCase(),
    );
    if (!exists) {
      final newBorrowerId =
          _borrowers.isEmpty ? 1 : _borrowers.map((b) => b.id).reduce((a, b) => a > b ? a : b) + 1;
      _borrowers.add(
        Borrower(
          id: newBorrowerId,
          name: borrowerName.trim(),
          phone: 'Not provided',
          email: 'Not provided',
          rating: 'B',
          notes: 'Added automatically from loan ${newLoan.loanNumber}',
        ),
      );
    }

    // Update virtual account balance
    final accIndex = _accounts.indexWhere((a) => a.id == virtualAccountId);
    if (accIndex != -1) {
      final delta = direction == LoanDirection.given ? -principal : principal;
      _accounts[accIndex] =
          _accounts[accIndex].copyWith(balance: _accounts[accIndex].balance + delta);
    }

    _ledgerEntries.insert(
      0,
      LedgerEntry(
        id: _ledgerEntries.length + 1,
        loanId: newId,
        virtualAccountId: virtualAccountId,
        category: 'LOAN_DISBURSAL',
        amount: principal,
        timestamp: now,
        description:
            '${direction == LoanDirection.given ? "Disbursed to" : "Borrowed from"} ${newLoan.borrowerName} (${newLoan.loanNumber})',
      ),
    );

    notifyListeners();
  }

  void recordPayment({
    required int loanId,
    required double amount,
    required int virtualAccountId,
    double? principalPortion,
    double? interestPortion,
  }) {
    final loanIndex = _loans.indexWhere((l) => l.id == loanId);
    if (loanIndex == -1 || amount <= 0) return;

    final loan = _loans[loanIndex];
    final updatedPaid = loan.paidAmount + amount;
    final newStatus =
        updatedPaid >= loan.totalDue ? LoanStatus.paid : loan.status;

    _loans[loanIndex] = loan.copyWith(
      paidAmount: updatedPaid,
      status: newStatus,
    );

    final accIndex = _accounts.indexWhere((a) => a.id == virtualAccountId);
    if (accIndex != -1) {
      final delta = loan.direction == LoanDirection.given ? amount : -amount;
      _accounts[accIndex] =
          _accounts[accIndex].copyWith(balance: _accounts[accIndex].balance + delta);
    }

    final pText = principalPortion != null ? 'Principal: ${principalPortion.toStringAsFixed(2)}' : '';
    final iText = interestPortion != null ? 'Interest: ${interestPortion.toStringAsFixed(2)}' : '';
    final splitNote = [pText, iText].where((s) => s.isNotEmpty).join(', ');

    _ledgerEntries.insert(
      0,
      LedgerEntry(
        id: _ledgerEntries.length + 1,
        loanId: loan.id,
        virtualAccountId: virtualAccountId,
        category: 'LOAN_REPAYMENT',
        amount: amount,
        timestamp: DateTime.now(),
        description:
            'Payment for ${loan.loanNumber} (${loan.borrowerName})${splitNote.isNotEmpty ? " ($splitNote)" : ""}',
      ),
    );

    notifyListeners();
  }

  void markLoanSettled(int loanId) {
    final idx = _loans.indexWhere((l) => l.id == loanId);
    if (idx == -1) return;
    final loan = _loans[idx];
    final remaining = loan.remainingBalance;
    if (remaining > 0) {
      recordPayment(
        loanId: loanId,
        amount: remaining,
        virtualAccountId: loan.virtualAccountId,
      );
    } else {
      _loans[idx] = loan.copyWith(status: LoanStatus.paid);
      notifyListeners();
    }
  }

  void createVirtualAccount({
    required String name,
    required double initialBalance,
    required String poolType,
    String description = '',
  }) {
    final newId = _accounts.isEmpty
        ? 1
        : _accounts.map((a) => a.id).reduce((a, b) => a > b ? a : b) + 1;
    _accounts.add(
      VirtualAccount(
        id: newId,
        accountNumber: 'VA-USR-${newId.toString().padLeft(3, '0')}',
        name: name.trim(),
        poolType: poolType,
        balance: initialBalance,
        description: description.trim().isEmpty
            ? 'Custom virtual account pool'
            : description.trim(),
      ),
    );
    notifyListeners();
  }

  bool transferBetweenAccounts({
    required int fromAccountId,
    required int toAccountId,
    required double amount,
  }) {
    if (fromAccountId == toAccountId || amount <= 0) return false;
    final fromIdx = _accounts.indexWhere((a) => a.id == fromAccountId);
    final toIdx = _accounts.indexWhere((a) => a.id == toAccountId);
    if (fromIdx == -1 || toIdx == -1) return false;

    _accounts[fromIdx] = _accounts[fromIdx].copyWith(
      balance: _accounts[fromIdx].balance - amount,
    );
    _accounts[toIdx] = _accounts[toIdx].copyWith(
      balance: _accounts[toIdx].balance + amount,
    );

    _ledgerEntries.insert(
      0,
      LedgerEntry(
        id: _ledgerEntries.length + 1,
        virtualAccountId: toAccountId,
        category: 'POOL_TRANSFER',
        amount: amount,
        timestamp: DateTime.now(),
        description:
            'Transferred from ${_accounts[fromIdx].name} to ${_accounts[toIdx].name}',
      ),
    );

    notifyListeners();
    return true;
  }

  void addBorrower({
    required String name,
    required String phone,
    required String email,
    required String rating,
    String notes = '',
  }) {
    final newId = _borrowers.isEmpty
        ? 1
        : _borrowers.map((b) => b.id).reduce((a, b) => a > b ? a : b) + 1;
    _borrowers.add(
      Borrower(
        id: newId,
        name: name.trim(),
        phone: phone.trim(),
        email: email.trim(),
        rating: rating,
        notes: notes.trim(),
      ),
    );
    notifyListeners();
  }
}
