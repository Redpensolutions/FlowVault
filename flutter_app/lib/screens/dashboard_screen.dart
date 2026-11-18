import 'package:flutter/material.dart';

import '../models/flow_vault_models.dart';
import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../utils/currency_formatter.dart';
import '../widgets/create_loan_sheet.dart';
import '../widgets/record_payment_sheet.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({
    super.key,
    required this.store,
    required this.onViewLoans,
    required this.onViewAccounts,
  });

  final FlowVaultStore store;
  final VoidCallback onViewLoans;
  final VoidCallback onViewAccounts;

  @override
  Widget build(BuildContext context) {
    final loans = store.allLoans;
    final recentEntries = store.ledgerEntries.take(5).toList();

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 96),
      children: [
        // Portfolio Hero Card
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [AppTheme.primaryBlue, AppTheme.primaryDark],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(22),
            boxShadow: [
              BoxShadow(
                color: AppTheme.primaryBlue.withOpacity(0.28),
                blurRadius: 18,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'TOTAL ACTIVE PORTFOLIO',
                    style: TextStyle(
                      color: Colors.white70,
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 0.6,
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFD700),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text(
                      'FLOWVAULT LEDGER',
                      style: TextStyle(
                        color: Colors.black,
                        fontSize: 10,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              Text(
                CurrencyFormatter.format(store.totalActivePortfolio),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 28,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: 16),
              const Divider(color: Colors.white24, height: 1),
              const SizedBox(height: 14),
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'COLLECTED',
                          style: TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          CurrencyFormatter.format(store.totalCollected),
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 16,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'OUTSTANDING DUE',
                          style: TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          CurrencyFormatter.format(store.totalOutstandingDue),
                          style: const TextStyle(
                            color: Color(0xFFFFB3B8),
                            fontSize: 16,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),

        // Quick Actions
        Row(
          children: [
            Expanded(
              child: _QuickActionCard(
                icon: Icons.add_circle_outline,
                label: 'New Loan',
                onTap: () => showCreateLoanSheet(context, store),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _QuickActionCard(
                icon: Icons.payments_outlined,
                label: 'Payment',
                onTap: () => showRecordPaymentSheet(context, store),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _QuickActionCard(
                icon: Icons.account_balance_outlined,
                label: 'Pools',
                onTap: onViewAccounts,
              ),
            ),
          ],
        ),
        const SizedBox(height: 14),

        // KPI Row
        Row(
          children: [
            Expanded(
              child: _StatTile(
                title: 'Active Loans',
                value: '${store.activeLoansCount}',
                color: AppTheme.successGreen,
                icon: Icons.trending_up_rounded,
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _StatTile(
                title: 'Overdue Alerts',
                value: '${store.overdueLoansCount}',
                color: AppTheme.dangerRed,
                icon: Icons.warning_amber_rounded,
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),

        // Active & Overdue Contracts
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'CONTRACTS OVERVIEW (${loans.length})',
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
                color: AppTheme.textMutedLight,
                letterSpacing: 0.6,
              ),
            ),
            TextButton(
              onPressed: onViewLoans,
              child: const Text('View All'),
            ),
          ],
        ),
        const SizedBox(height: 6),
        ...loans.take(4).map((loan) => _DashboardLoanTile(loan: loan, store: store)),

        const SizedBox(height: 16),
        const Text(
          'RECENT LEDGER ACTIVITY',
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: AppTheme.textMutedLight,
            letterSpacing: 0.6,
          ),
        ),
        const SizedBox(height: 8),
        ...recentEntries.map(
          (entry) => Card(
            margin: const EdgeInsets.only(bottom: 8),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
              side: const BorderSide(color: AppTheme.borderLight),
            ),
            elevation: 0,
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: AppTheme.primaryLight,
                child: Icon(
                  entry.category == 'LOAN_REPAYMENT'
                      ? Icons.south_west_rounded
                      : Icons.swap_horiz_rounded,
                  color: AppTheme.primaryBlue,
                  size: 20,
                ),
              ),
              title: Text(
                entry.description,
                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
              ),
              subtitle: Text(
                CurrencyFormatter.formatShortDate(entry.timestamp),
                style: const TextStyle(fontSize: 11, color: AppTheme.textMutedLight),
              ),
              trailing: Text(
                CurrencyFormatter.format(entry.amount),
                style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _QuickActionCard extends StatelessWidget {
  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: AppTheme.borderLight),
          ),
          child: Column(
            children: [
              Icon(icon, color: AppTheme.primaryBlue),
              const SizedBox(height: 6),
              Text(
                label,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 13,
                  color: AppTheme.primaryBlue,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatTile extends StatelessWidget {
  const _StatTile({
    required this.title,
    required this.value,
    required this.color,
    required this.icon,
  });

  final String title;
  final String value;
  final Color color;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.borderLight),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 18,
            backgroundColor: color.withOpacity(0.12),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(fontSize: 11, color: AppTheme.textMutedLight),
              ),
              Text(
                value,
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                  color: color,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DashboardLoanTile extends StatelessWidget {
  const _DashboardLoanTile({required this.loan, required this.store});

  final Loan loan;
  final FlowVaultStore store;

  @override
  Widget build(BuildContext context) {
    final badgeColors = switch (loan.status) {
      LoanStatus.active => (AppTheme.successBg, AppTheme.successGreen, 'ACTIVE'),
      LoanStatus.overdue => (AppTheme.dangerBg, AppTheme.dangerRed, 'OVERDUE'),
      LoanStatus.paid => (const Color(0xFFE0E2EC), const Color(0xFF44474F), 'PAID'),
    };

    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.borderLight),
      ),
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
                      style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${loan.loanNumber} • ${loan.annualInterestRate}% (${loan.interestType.name.toUpperCase()}) • ${loan.termMonths} mos',
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
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: loan.progressRatio,
              minHeight: 6,
              backgroundColor: const Color(0xFFEDF0F5),
              color: loan.status == LoanStatus.overdue ? AppTheme.dangerRed : AppTheme.primaryBlue,
            ),
          ),
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Paid: ${CurrencyFormatter.format(loan.paidAmount)}',
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
              ),
              Text(
                'Due: ${CurrencyFormatter.format(loan.totalDue)}',
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
