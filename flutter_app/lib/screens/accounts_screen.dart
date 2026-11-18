import 'package:flutter/material.dart';

import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../utils/currency_formatter.dart';

class AccountsScreen extends StatelessWidget {
  const AccountsScreen({super.key, required this.store});

  final FlowVaultStore store;

  Future<void> _showCreateAccountDialog(BuildContext context) async {
    final nameController = TextEditingController();
    final balanceController = TextEditingController(text: '250000');
    final descController = TextEditingController();
    var poolType = 'Bank account';

    await showDialog<void>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Create Virtual Pool'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Account Name'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: balanceController,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(labelText: 'Initial Balance (₹)'),
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  value: poolType,
                  decoration: const InputDecoration(labelText: 'Pool Category'),
                  items: const [
                    DropdownMenuItem(value: 'Bank account', child: Text('Bank account')),
                    DropdownMenuItem(value: 'Collection Account', child: Text('Collection Account')),
                    DropdownMenuItem(value: 'Recovery Account', child: Text('Recovery Account')),
                    DropdownMenuItem(value: 'Operations account', child: Text('Operations account')),
                  ],
                  onChanged: (v) {
                    if (v != null) setState(() => poolType = v);
                  },
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: descController,
                  decoration: const InputDecoration(labelText: 'Description (optional)'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Cancel')),
            FilledButton(
              onPressed: () {
                final name = nameController.text.trim();
                final balance = double.tryParse(balanceController.text) ?? 0.0;
                if (name.isEmpty) return;
                store.createVirtualAccount(
                  name: name,
                  initialBalance: balance,
                  poolType: poolType,
                  description: descController.text,
                );
                Navigator.of(ctx).pop();
              },
              child: const Text('Create Pool'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showTransferDialog(BuildContext context) async {
    if (store.accounts.length < 2) return;
    var fromId = store.accounts.first.id;
    var toId = store.accounts[1].id;
    final amountController = TextEditingController(text: '25000');

    await showDialog<void>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Transfer Between Pools'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<int>(
                value: fromId,
                decoration: const InputDecoration(labelText: 'Source Pool'),
                items: store.accounts
                    .map((a) => DropdownMenuItem(value: a.id, child: Text(a.name)))
                    .toList(),
                onChanged: (v) {
                  if (v != null) setState(() => fromId = v);
                },
              ),
              const SizedBox(height: 10),
              DropdownButtonFormField<int>(
                value: toId,
                decoration: const InputDecoration(labelText: 'Destination Pool'),
                items: store.accounts
                    .map((a) => DropdownMenuItem(value: a.id, child: Text(a.name)))
                    .toList(),
                onChanged: (v) {
                  if (v != null) setState(() => toId = v);
                },
              ),
              const SizedBox(height: 10),
              TextField(
                controller: amountController,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: 'Amount (₹)'),
              ),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Cancel')),
            FilledButton(
              onPressed: () {
                final amount = double.tryParse(amountController.text) ?? 0.0;
                final ok = store.transferBetweenAccounts(
                  fromAccountId: fromId,
                  toAccountId: toId,
                  amount: amount,
                );
                Navigator.of(ctx).pop();
                if (!ok) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Select two distinct pools and a valid amount.')),
                  );
                }
              },
              child: const Text('Transfer'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final accounts = store.accounts;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 96),
      children: [
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: AppTheme.borderLight),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'TOTAL LIQUIDITY ACROSS POOLS',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w800,
                  color: AppTheme.textMutedLight,
                  letterSpacing: 0.6,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                CurrencyFormatter.format(store.totalPoolLiquidity),
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  color: AppTheme.primaryBlue,
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => _showTransferDialog(context),
                      icon: const Icon(Icons.swap_horiz_rounded),
                      label: const Text('Transfer'),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: FilledButton.icon(
                      onPressed: () => _showCreateAccountDialog(context),
                      icon: const Icon(Icons.add),
                      label: const Text('New Pool'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        Text(
          'VIRTUAL ACCOUNT POOLS (${accounts.length})',
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: AppTheme.textMutedLight,
            letterSpacing: 0.6,
          ),
        ),
        const SizedBox(height: 8),
        ...accounts.map(
          (acc) => Card(
            margin: const EdgeInsets.only(bottom: 10),
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: const BorderSide(color: AppTheme.borderLight),
            ),
            child: ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              leading: CircleAvatar(
                backgroundColor: AppTheme.primaryLight,
                child: const Icon(Icons.account_balance_wallet_outlined, color: AppTheme.primaryBlue),
              ),
              title: Text(
                acc.name,
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              subtitle: Text(
                '${acc.accountNumber} • ${acc.poolType}\n${acc.description}',
                style: const TextStyle(fontSize: 12, color: AppTheme.textMutedLight),
              ),
              isThreeLine: acc.description.isNotEmpty,
              trailing: Text(
                CurrencyFormatter.format(acc.balance),
                style: const TextStyle(
                  fontWeight: FontWeight.w800,
                  fontSize: 15,
                  color: AppTheme.textMainLight,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
