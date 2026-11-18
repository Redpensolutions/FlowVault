import 'package:flutter/material.dart';

import '../models/flow_vault_models.dart';
import '../services/interest_calculator.dart';
import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../utils/currency_formatter.dart';
import '../widgets/record_payment_sheet.dart';

class LoansScreen extends StatelessWidget {
  const LoansScreen({super.key, required this.store});

  final FlowVaultStore store;

  @override
  Widget build(BuildContext context) {
    final loans = store.filteredLoans;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
          child: Column(
            children: [
              SegmentedButton<LoanDirection>(
                segments: const [
                  ButtonSegment(
                    value: LoanDirection.given,
                    label: Text('Loans Given'),
                    icon: Icon(Icons.call_made_rounded),
                  ),
                  ButtonSegment(
                    value: LoanDirection.taken,
                    label: Text('Loans Taken'),
                    icon: Icon(Icons.call_received_rounded),
                  ),
                ],
                selected: {store.selectedDirection},
                onSelectionChanged: (v) => store.setDirection(v.first),
              ),
              const SizedBox(height: 10),
              TextField(
                decoration: const InputDecoration(
                  hintText: 'Search borrower or loan # (e.g. LN-2026-001)',
                  prefixIcon: Icon(Icons.search),
                ),
                onChanged: store.setSearchQuery,
              ),
              const SizedBox(height: 10),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    FilterChip(
                      label: const Text('All'),
                      selected: store.statusFilter == null,
                      onSelected: (_) => store.setStatusFilter(null),
                    ),
                    const SizedBox(width: 8),
                    FilterChip(
                      label: const Text('Active'),
                      selected: store.statusFilter == LoanStatus.active,
                      onSelected: (_) => store.setStatusFilter(LoanStatus.active),
                    ),
                    const SizedBox(width: 8),
                    FilterChip(
                      label: const Text('Overdue'),
                      selected: store.statusFilter == LoanStatus.overdue,
                      onSelected: (_) => store.setStatusFilter(LoanStatus.overdue),
                    ),
                    const SizedBox(width: 8),
                    FilterChip(
                      label: const Text('Paid'),
                      selected: store.statusFilter == LoanStatus.paid,
                      onSelected: (_) => store.setStatusFilter(LoanStatus.paid),
                    ),
                    const SizedBox(width: 8),
                    PopupMenuButton<LoanSortOption>(
                      initialValue: store.sortOption,
                      onSelected: store.setSortOption,
                      itemBuilder: (context) => const [
                        PopupMenuItem(
                          value: LoanSortOption.defaultOrder,
                          child: Text('Sort: Default'),
                        ),
                        PopupMenuItem(
                          value: LoanSortOption.overdueFirst,
                          child: Text('Sort: Overdue First'),
                        ),
                        PopupMenuItem(
                          value: LoanSortOption.amountHighToLow,
                          child: Text('Sort: Balance High-Low'),
                        ),
                        PopupMenuItem(
                          value: LoanSortOption.dueSoonest,
                          child: Text('Sort: Due Soonest'),
                        ),
                      ],
                      child: const Chip(
                        avatar: Icon(Icons.sort, size: 16),
                        label: Text('Sort'),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: loans.isEmpty
              ? const Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text(
                      'No loan contracts match the current filters.',
                      style: TextStyle(color: AppTheme.textMutedLight),
                    ),
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.fromLTRB(16, 4, 16, 96),
                  itemCount: loans.length,
                  itemBuilder: (context, index) {
                    final loan = loans[index];
                    return _LoanCard(loan: loan, store: store);
                  },
                ),
        ),
      ],
    );
  }
}

class _LoanCard extends StatelessWidget {
  const _LoanCard({required this.loan, required this.store});

  final Loan loan;
  final FlowVaultStore store;

  void _showDetails(BuildContext context) {
    final dynamicState = InterestCalculator.computeDynamicState(
      loan,
      store.ledgerEntries,
    );
    final account = store.accountById(loan.virtualAccountId);

    showModalBottomSheet<void>(
      context: context,
      useSafeArea: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              '${loan.borrowerName} • ${loan.loanNumber}',
              style: Theme.of(ctx).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 12),
            _DetailRow('Principal Issued', CurrencyFormatter.format(dynamicState.initialPrincipal)),
            _DetailRow('Interest Rate & Mode', '${loan.annualInterestRate}% (${loan.interestType.name.toUpperCase()})'),
            if (loan.interestType == InterestType.compound)
              _DetailRow('Compounding Interval', loan.interestPeriod.label),
            _DetailRow('Accrued Interest To Date', CurrencyFormatter.format(dynamicState.totalAccruedInterest)),
            _DetailRow('Total Paid', CurrencyFormatter.format(loan.paidAmount)),
            _DetailRow('Remaining Balance', CurrencyFormatter.format(loan.remainingBalance)),
            _DetailRow('Due Date', CurrencyFormatter.formatShortDate(loan.dueDate)),
            _DetailRow('Pool Account', account?.name ?? 'N/A'),
            const SizedBox(height: 16),
            if (loan.status != LoanStatus.paid)
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () {
                        Navigator.of(ctx).pop();
                        store.markLoanSettled(loan.id);
                      },
                      child: const Text('Mark Settled'),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: FilledButton(
                      onPressed: () {
                        Navigator.of(ctx).pop();
                        showRecordPaymentSheet(context, store, initialLoanId: loan.id);
                      },
                      child: const Text('Record Payment'),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final badgeColors = switch (loan.status) {
      LoanStatus.active => (AppTheme.successBg, AppTheme.successGreen, 'ACTIVE'),
      LoanStatus.overdue => (AppTheme.dangerBg, AppTheme.dangerRed, 'OVERDUE'),
      LoanStatus.paid => (const Color(0xFFE0E2EC), const Color(0xFF44474F), 'PAID'),
    };

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: const BorderSide(color: AppTheme.borderLight),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () => _showDetails(context),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          loan.borrowerName,
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          '${loan.loanNumber} • Due ${CurrencyFormatter.formatShortDate(loan.dueDate)}',
                          style: const TextStyle(fontSize: 12, color: AppTheme.textMutedLight),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: badgeColors.$1,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      badgeColors.$3,
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.w800,
                        color: badgeColors.$2,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Principal: ${CurrencyFormatter.format(loan.principalAmount)}',
                    style: const TextStyle(fontSize: 13, color: AppTheme.textMutedLight),
                  ),
                  Text(
                    'Balance Left: ${CurrencyFormatter.format(loan.remainingBalance)}',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: loan.status == LoanStatus.paid
                          ? AppTheme.successGreen
                          : AppTheme.dangerRed,
                    ),
                  ),
                ],
              ),
              if (loan.status != LoanStatus.paid) ...[
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () =>
                        showRecordPaymentSheet(context, store, initialLoanId: loan.id),
                    child: const Text('Record Payment'),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow(this.label, this.value);

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: AppTheme.textMutedLight)),
          Text(value, style: const TextStyle(fontWeight: FontWeight.w700)),
        ],
      ),
    );
  }
}
