import 'package:flutter/material.dart';

import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../widgets/create_loan_sheet.dart';
import 'accounts_screen.dart';
import 'borrowers_screen.dart';
import 'dashboard_screen.dart';
import 'loans_screen.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.store});

  final FlowVaultStore store;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    final isWide = MediaQuery.sizeOf(context).width >= 700;

    final body = AnimatedBuilder(
      animation: widget.store,
      builder: (context, _) {
        return switch (_selectedIndex) {
          0 => DashboardScreen(
              store: widget.store,
              onViewLoans: () => setState(() => _selectedIndex = 1),
              onViewAccounts: () => setState(() => _selectedIndex = 2),
            ),
          1 => LoansScreen(store: widget.store),
          2 => AccountsScreen(store: widget.store),
          _ => BorrowersScreen(store: widget.store),
        };
      },
    );

    return Scaffold(
      appBar: AppBar(
        title: const Row(
          children: [
            Icon(Icons.account_balance_wallet_rounded, size: 22),
            SizedBox(width: 10),
            Text(
              'FlowVault',
              style: TextStyle(fontWeight: FontWeight.w800),
            ),
          ],
        ),
      ),
      body: isWide
          ? Row(
              children: [
                NavigationRail(
                  selectedIndex: _selectedIndex,
                  onDestinationSelected: (idx) => setState(() => _selectedIndex = idx),
                  labelType: NavigationRailLabelType.all,
                  destinations: const [
                    NavigationRailDestination(
                      icon: Icon(Icons.dashboard_outlined),
                      selectedIcon: Icon(Icons.dashboard),
                      label: Text('Dashboard'),
                    ),
                    NavigationRailDestination(
                      icon: Icon(Icons.receipt_long_outlined),
                      selectedIcon: Icon(Icons.receipt_long),
                      label: Text('Loans'),
                    ),
                    NavigationRailDestination(
                      icon: Icon(Icons.account_balance_outlined),
                      selectedIcon: Icon(Icons.account_balance),
                      label: Text('Accounts'),
                    ),
                    NavigationRailDestination(
                      icon: Icon(Icons.people_outline),
                      selectedIcon: Icon(Icons.people),
                      label: Text('Borrowers'),
                    ),
                  ],
                ),
                const VerticalDivider(width: 1),
                Expanded(
                  child: Center(
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 720),
                      child: body,
                    ),
                  ),
                ),
              ],
            )
          : body,
      bottomNavigationBar: isWide
          ? null
          : NavigationBar(
              selectedIndex: _selectedIndex,
              onDestinationSelected: (idx) => setState(() => _selectedIndex = idx),
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.dashboard_outlined),
                  selectedIcon: Icon(Icons.dashboard),
                  label: 'Dashboard',
                ),
                NavigationDestination(
                  icon: Icon(Icons.receipt_long_outlined),
                  selectedIcon: Icon(Icons.receipt_long),
                  label: 'Loans',
                ),
                NavigationDestination(
                  icon: Icon(Icons.account_balance_outlined),
                  selectedIcon: Icon(Icons.account_balance),
                  label: 'Accounts',
                ),
                NavigationDestination(
                  icon: Icon(Icons.people_outline),
                  selectedIcon: Icon(Icons.people),
                  label: 'Borrowers',
                ),
              ],
            ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => showCreateLoanSheet(context, widget.store),
        backgroundColor: AppTheme.primaryBlue,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add),
        label: const Text('New Loan'),
      ),
    );
  }
}
