import 'package:flutter/material.dart';

import '../models/flow_vault_models.dart';
import '../services/interest_calculator.dart';
import '../state/flow_vault_store.dart';
import '../theme/app_theme.dart';
import '../utils/currency_formatter.dart';

Future<void> showCreateLoanSheet(BuildContext context, FlowVaultStore store) {
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
    ),
    builder: (ctx) => _CreateLoanSheet(store: store),
  );
}

class _CreateLoanSheet extends StatefulWidget {
  const _CreateLoanSheet({required this.store});

  final FlowVaultStore store;

  @override
  State<_CreateLoanSheet> createState() => _CreateLoanSheetState();
}

class _CreateLoanSheetState extends State<_CreateLoanSheet> {
  final _formKey = GlobalKey<FormState>();
  final _borrowerController = TextEditingController();
  final _principalController = TextEditingController(text: '100000');
  final _rateController = TextEditingController(text: '12.0');
  final _termController = TextEditingController(text: '12');
  final _notesController = TextEditingController();

  LoanDirection _direction = LoanDirection.given;
  InterestType _interestType = InterestType.simple;
  InterestPeriod _interestPeriod = InterestPeriod.yearly;
  InterestBasis _interestBasis = InterestBasis.monthsBased;
  late int _selectedAccountId;

  @override
  void initState() {
    super.initState();
    _selectedAccountId = widget.store.accounts.first.id;
  }

  @override
  void dispose() {
    _borrowerController.dispose();
    _principalController.dispose();
    _rateController.dispose();
    _termController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  CalculationResult _previewCalculation() {
    final principal = double.tryParse(_principalController.text) ?? 0.0;
    final rate = double.tryParse(_rateController.text) ?? 0.0;
    final termVal = int.tryParse(_termController.text) ?? 12;
    final safeTerm = termVal <= 0 ? 1 : termVal;

    return InterestCalculator.calculate(
      principal: principal,
      annualRatePercent: rate,
      interestType: _interestType,
      termMonths: _interestBasis == InterestBasis.monthsBased
          ? safeTerm
          : (safeTerm / 30).ceil().clamp(1, 360),
      interestPeriod: _interestPeriod,
      interestBasis: _interestBasis,
      daysCount: _interestBasis == InterestBasis.daysBased ? safeTerm : safeTerm * 30,
    );
  }

  void _submit() {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    final principal = double.parse(_principalController.text);
    final rate = _interestType == InterestType.zero
        ? 0.0
        : double.parse(_rateController.text);
    final termVal = int.parse(_termController.text);

    widget.store.createLoan(
      borrowerName: _borrowerController.text,
      principal: principal,
      annualRate: rate,
      interestType: _interestType,
      interestPeriod: _interestPeriod,
      interestBasis: _interestBasis,
      termMonths: _interestBasis == InterestBasis.monthsBased
          ? termVal
          : (termVal / 30).ceil().clamp(1, 360),
      termDays: _interestBasis == InterestBasis.daysBased ? termVal : termVal * 30,
      direction: _direction,
      virtualAccountId: _selectedAccountId,
      notes: _notesController.text,
    );

    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Loan contract issued and ledger updated.')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final preview = _previewCalculation();
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
                    'Issue New Loan',
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
              SegmentedButton<LoanDirection>(
                segments: const [
                  ButtonSegment(
                    value: LoanDirection.given,
                    label: Text('Money Given'),
                    icon: Icon(Icons.arrow_upward_rounded),
                  ),
                  ButtonSegment(
                    value: LoanDirection.taken,
                    label: Text('Money Taken'),
                    icon: Icon(Icons.arrow_downward_rounded),
                  ),
                ],
                selected: {_direction},
                onSelectionChanged: (v) => setState(() => _direction = v.first),
              ),
              const SizedBox(height: 14),
              TextFormField(
                controller: _borrowerController,
                decoration: const InputDecoration(
                  labelText: 'Borrower / Counterparty Name',
                  hintText: 'e.g. Anand Mahindra',
                  prefixIcon: Icon(Icons.person_outline),
                ),
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? 'Enter borrower name' : null,
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: TextFormField(
                      controller: _principalController,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: const InputDecoration(
                        labelText: 'Principal (₹)',
                        prefixIcon: Icon(Icons.currency_rupee),
                      ),
                      onChanged: (_) => setState(() {}),
                      validator: (v) {
                        final val = double.tryParse(v ?? '');
                        if (val == null || val <= 0) return 'Invalid amount';
                        return null;
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextFormField(
                      controller: _rateController,
                      enabled: _interestType != InterestType.zero,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: const InputDecoration(
                        labelText: 'Annual Rate (%)',
                        prefixIcon: Icon(Icons.percent),
                      ),
                      onChanged: (_) => setState(() {}),
                      validator: (v) {
                        if (_interestType == InterestType.zero) return null;
                        final val = double.tryParse(v ?? '');
                        if (val == null || val < 0) return 'Invalid rate';
                        return null;
                      },
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<InterestType>(
                value: _interestType,
                decoration: const InputDecoration(labelText: 'Interest Calculation Mode'),
                items: const [
                  DropdownMenuItem(value: InterestType.simple, child: Text('Simple Interest')),
                  DropdownMenuItem(value: InterestType.compound, child: Text('Compound Interest')),
                  DropdownMenuItem(value: InterestType.zero, child: Text('Zero Interest (0%)')),
                ],
                onChanged: (v) {
                  if (v != null) setState(() => _interestType = v);
                },
              ),
              if (_interestType == InterestType.compound) ...[
                const SizedBox(height: 12),
                DropdownButtonFormField<InterestPeriod>(
                  value: _interestPeriod,
                  decoration: const InputDecoration(labelText: 'Compounding Interval'),
                  items: InterestPeriod.values
                      .map((p) => DropdownMenuItem(value: p, child: Text(p.label)))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setState(() => _interestPeriod = v);
                  },
                ),
              ],
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: TextFormField(
                      controller: _termController,
                      keyboardType: TextInputType.number,
                      decoration: InputDecoration(
                        labelText: _interestBasis == InterestBasis.monthsBased
                            ? 'Term (Months)'
                            : 'Term (Days)',
                      ),
                      onChanged: (_) => setState(() {}),
                      validator: (v) {
                        final val = int.tryParse(v ?? '');
                        if (val == null || val <= 0) return 'Invalid term';
                        return null;
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: DropdownButtonFormField<InterestBasis>(
                      value: _interestBasis,
                      decoration: const InputDecoration(labelText: 'Basis'),
                      items: InterestBasis.values
                          .map((b) => DropdownMenuItem(value: b, child: Text(b.label)))
                          .toList(),
                      onChanged: (v) {
                        if (v != null) setState(() => _interestBasis = v);
                      },
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(
                value: _selectedAccountId,
                decoration: InputDecoration(
                  labelText: _direction == LoanDirection.given
                      ? 'Disbursing Virtual Account'
                      : 'Receiving Virtual Account',
                ),
                items: widget.store.accounts
                    .map(
                      (acc) => DropdownMenuItem(
                        value: acc.id,
                        child: Text(
                          '${acc.name} (${CurrencyFormatter.format(acc.balance)})',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    )
                    .toList(),
                onChanged: (v) {
                  if (v != null) setState(() => _selectedAccountId = v);
                },
              ),
              const SizedBox(height: 14),
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppTheme.primaryLight,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppTheme.primaryBlue.withOpacity(0.25)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'LIVE CALCULATION PREVIEW',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w800,
                        color: AppTheme.primaryDark,
                        letterSpacing: 0.6,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Total Interest'),
                        Text(
                          CurrencyFormatter.format(preview.totalInterest),
                          style: const TextStyle(fontWeight: FontWeight.w700),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Total Maturity Due'),
                        Text(
                          CurrencyFormatter.format(preview.totalDue),
                          style: const TextStyle(
                            fontWeight: FontWeight.w800,
                            color: AppTheme.primaryBlue,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Est. Monthly Installment'),
                        Text(
                          CurrencyFormatter.format(preview.monthlyPayment),
                          style: const TextStyle(fontWeight: FontWeight.w700),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: _submit,
                icon: const Icon(Icons.check_circle_outline),
                label: const Text('Save & Issue Loan'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
