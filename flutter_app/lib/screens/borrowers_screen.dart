import 'package:flutter/material.dart';

import '../models/flow_vault_models.dart';
import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../utils/currency_formatter.dart';

class BorrowersScreen extends StatelessWidget {
  const BorrowersScreen({super.key, required this.store});

  final FlowVaultStore store;

  Future<void> _showAddBorrowerDialog(BuildContext context) async {
    final nameCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    final emailCtrl = TextEditingController();
    final notesCtrl = TextEditingController();
    var rating = 'A';

    await showDialog<void>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          title: const Text('Add Borrower'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(labelText: 'Full Name'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: phoneCtrl,
                  decoration: const InputDecoration(labelText: 'Phone Number'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: emailCtrl,
                  decoration: const InputDecoration(labelText: 'Email Address'),
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  value: rating,
                  decoration: const InputDecoration(labelText: 'Credit Grade'),
                  items: const [
                    DropdownMenuItem(value: 'A+', child: Text('Grade A+')),
                    DropdownMenuItem(value: 'A', child: Text('Grade A')),
                    DropdownMenuItem(value: 'B', child: Text('Grade B')),
                    DropdownMenuItem(value: 'C', child: Text('Grade C')),
                  ],
                  onChanged: (v) {
                    if (v != null) setState(() => rating = v);
                  },
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: notesCtrl,
                  decoration: const InputDecoration(labelText: 'Notes'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Cancel')),
            FilledButton(
              onPressed: () {
                if (nameCtrl.text.trim().isEmpty) return;
                store.addBorrower(
                  name: nameCtrl.text,
                  phone: phoneCtrl.text.isEmpty ? 'Not provided' : phoneCtrl.text,
                  email: emailCtrl.text.isEmpty ? 'Not provided' : emailCtrl.text,
                  rating: rating,
                  notes: notesCtrl.text,
                );
                Navigator.of(ctx).pop();
              },
              child: const Text('Save Borrower'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final borrowers = store.borrowers;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 96),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'BORROWER DIRECTORY (${borrowers.length})',
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
                color: AppTheme.textMutedLight,
                letterSpacing: 0.6,
              ),
            ),
            TextButton.icon(
              onPressed: () => _showAddBorrowerDialog(context),
              icon: const Icon(Icons.person_add_alt_1),
              label: const Text('Add Borrower'),
            ),
          ],
        ),
        const SizedBox(height: 6),
        ...borrowers.map((b) {
          final borrowerLoans = store.allLoans
              .where((l) => l.borrowerName.toLowerCase() == b.name.toLowerCase())
              .toList();
          final outstanding = borrowerLoans
              .where((l) => l.status != LoanStatus.paid)
              .fold(0.0, (sum, l) => sum + l.remainingBalance);

          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: const BorderSide(color: AppTheme.borderLight),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        backgroundColor: AppTheme.primaryLight,
                        child: Text(
                          b.name.isNotEmpty ? b.name[0].toUpperCase() : '?',
                          style: const TextStyle(
                            fontWeight: FontWeight.w800,
                            color: AppTheme.primaryBlue,
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              b.name,
                              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
                            ),
                            Text(
                              '${b.phone} • ${b.email}',
                              style: const TextStyle(fontSize: 12, color: AppTheme.textMutedLight),
                            ),
                          ],
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppTheme.primaryLight,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          'Grade ${b.rating}',
                          style: const TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w800,
                            color: AppTheme.primaryBlue,
                          ),
                        ),
                      ),
                    ],
                  ),
                  if (b.notes.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Text(
                      b.notes,
                      style: const TextStyle(fontSize: 12, color: AppTheme.textMutedLight),
                    ),
                  ],
                  const SizedBox(height: 10),
                  const Divider(height: 1),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        '${borrowerLoans.length} contract(s)',
                        style: const TextStyle(fontSize: 12, color: AppTheme.textMutedLight),
                      ),
                      Text(
                        'Outstanding: ${CurrencyFormatter.format(outstanding)}',
                        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        }),
      ],
    );
  }
}
