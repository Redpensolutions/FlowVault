import 'package:flutter/material.dart';

import '../models/flow_vault_models.dart';
import '../state/flow_vault_store.dart';
import '../utils/currency_formatter.dart';

Future<void> showRecordPaymentSheet(
  BuildContext context,
  FlowVaultStore store, {
  int? initialLoanId,
}) {
  final openLoans = store.allLoans.where((l) => l.status != LoanStatus.paid).toList();
  if (openLoans.isEmpty) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('All active loans are already settled.')),
    );
    return Future.value();
  }

  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
    ),
    builder: (ctx) => _RecordPaymentSheet(
      store: store,
      openLoans: openLoans,
      initialLoanId: initialLoanId ?? openLoans.first.id,
    ),
  );
}

class _RecordPaymentSheet extends StatefulWidget {
  const _RecordPaymentSheet({
    required this.store,
    required this.openLoans,
    required this.initialLoanId,
  });

  final FlowVaultStore store;
  final List<Loan> openLoans;
  final int initialLoanId;

  @override
  State<_RecordPaymentSheet> createState() => _RecordPaymentSheetState();
}

class _RecordPaymentSheetState extends State<_RecordPaymentSheet> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  late int _selectedLoanId;
  late int _selectedAccountId;

  @override
  void initState() {
    super.initState();
    _selectedLoanId = widget.openLoans.any((l) => l.id == widget.initialLoanId)
        ? widget.initialLoanId
        : widget.openLoans.first.id;
    _selectedAccountId = widget.store.accounts.first.id;

    final selectedLoan = widget.openLoans.firstWhere((l) => l.id == _selectedLoanId);
    _amountController.text = selectedLoan.remainingBalance.round().toString();
  }

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  void _submit() {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    final amount = double.parse(_amountController.text);
    final loan = widget.openLoans.firstWhere((l) => l.id == _selectedLoanId);

    // Pro-rata principal vs interest split
    final interestTotal = (loan.totalDue - loan.principalAmount).clamp(0.0, double.infinity);
    final interestRatio = loan.totalDue > 0 ? interestTotal / loan.totalDue : 0.0;
    final interestPart = amount * interestRatio;
    final principalPart = amount - interestPart;

    widget.store.recordPayment(
      loanId: _selectedLoanId,
      amount: amount,
      virtualAccountId: _selectedAccountId,
      principalPortion: principalPart,
      interestPortion: interestPart,
    );

    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          'Recorded ${CurrencyFormatter.format(amount)} for ${loan.loanNumber}.',
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return Padding(
      padding: EdgeInsets.fromLTRB(20, 16, 20, 20 + bottomInset),
      child: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Record Repayment',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
                  ),
                  IconButton(
                    onPressed: () => Navigator.of(context).pop(),
                    icon: const Icon(Icons.close),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(
                value: _selectedLoanId,
                decoration: const InputDecoration(labelText: 'Loan Contract'),
                items: widget.openLoans
                    .map(
                      (l) => DropdownMenuItem(
                        value: l.id,
                        child: Text(
                          '${l.borrowerName} (${l.loanNumber}) — Due ${CurrencyFormatter.format(l.remainingBalance)}',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    )
                    .toList(),
                onChanged: (v) {
                  if (v == null) return;
                  setState(() {
                    _selectedLoanId = v;
                    final loan = widget.openLoans.firstWhere((l) => l.id == v);
                    _amountController.text = loan.remainingBalance.round().toString();
                  });
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _amountController,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(
                  labelText: 'Payment Amount (₹)',
                  prefixIcon: Icon(Icons.currency_rupee),
                ),
                validator: (v) {
                  final val = double.tryParse(v ?? '');
                  if (val == null || val <= 0) return 'Enter a valid amount';
                  return null;
                },
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(
                value: _selectedAccountId,
                decoration: const InputDecoration(labelText: 'Receiving Virtual Account'),
                items: widget.store.accounts
                    .map(
                      (acc) => DropdownMenuItem(
                        value: acc.id,
                        child: Text(acc.name, overflow: TextOverflow.ellipsis),
                      ),
                    )
                    .toList(),
                onChanged: (v) {
                  if (v != null) setState(() => _selectedAccountId = v);
                },
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: _submit,
                icon: const Icon(Icons.payments_outlined),
                label: const Text('Confirm & Update Ledger'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
