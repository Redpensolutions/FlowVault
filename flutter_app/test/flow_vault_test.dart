import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:flow_vault/main.dart';
import 'package:flow_vault/models/flow_vault_models.dart';
import 'package:flow_vault/services/interest_calculator.dart';
import 'package:flow_vault/state/flow_vault_store.dart';
import 'package:flow_vault/utils/currency_formatter.dart';

void main() {
  group('InterestCalculator', () {
    test('calculates simple interest over 12 months accurately', () {
      final result = InterestCalculator.calculate(
        principal: 100000,
        annualRatePercent: 12.0,
        interestType: InterestType.simple,
        termMonths: 12,
      );

      expect(result.totalInterest, 12000.0);
      expect(result.totalDue, 112000.0);
      expect(result.hasInterest, isTrue);
    });

    test('calculates monthly compound interest accurately', () {
      final result = InterestCalculator.calculate(
        principal: 100000,
        annualRatePercent: 12.0,
        interestType: InterestType.compound,
        interestPeriod: InterestPeriod.monthly,
        termMonths: 12,
      );

      expect(result.totalDue, closeTo(112682.5, 0.1));
      expect(result.totalInterest, closeTo(12682.5, 0.1));
    });

    test('returns zero interest when type is zero', () {
      final result = InterestCalculator.calculate(
        principal: 50000,
        annualRatePercent: 15.0,
        interestType: InterestType.zero,
        termMonths: 5,
      );

      expect(result.totalInterest, 0.0);
      expect(result.totalDue, 50000.0);
      expect(result.monthlyPayment, 10000.0);
    });
  });

  group('FlowVaultStore', () {
    test('creates loan, updates virtual pool balance, and records payment', () {
      final store = FlowVaultStore();
      final initialLoans = store.allLoans.length;
      final initialAccBalance = store.accountById(1)!.balance;

      store.createLoan(
        borrowerName: 'Test Borrower',
        principal: 50000,
        annualRate: 10,
        interestType: InterestType.simple,
        interestPeriod: InterestPeriod.yearly,
        interestBasis: InterestBasis.monthsBased,
        termMonths: 12,
        termDays: 365,
        direction: LoanDirection.given,
        virtualAccountId: 1,
      );

      expect(store.allLoans.length, initialLoans + 1);
      expect(store.accountById(1)!.balance, initialAccBalance - 50000);

      final created = store.allLoans.first;
      store.markLoanSettled(created.id);
      expect(store.allLoans.first.status, LoanStatus.paid);
      expect(store.allLoans.first.remainingBalance, 0.0);
    });

    test('transfers balance between two virtual pools', () {
      final store = FlowVaultStore();
      final fromBefore = store.accountById(1)!.balance;
      final toBefore = store.accountById(2)!.balance;

      final ok = store.transferBetweenAccounts(
        fromAccountId: 1,
        toAccountId: 2,
        amount: 25000,
      );

      expect(ok, isTrue);
      expect(store.accountById(1)!.balance, fromBefore - 25000);
      expect(store.accountById(2)!.balance, toBefore + 25000);
    });
  });

  group('CurrencyFormatter', () {
    test('formats Indian rupee grouping properly', () {
      expect(CurrencyFormatter.format(820000), '₹ 8,20,000');
      expect(CurrencyFormatter.format(1500), '₹ 1,500');
    });
  });

  testWidgets('FlowVaultApp renders Dashboard and navigates tabs', (tester) async {
    await tester.pumpWidget(const FlowVaultApp());

    expect(find.text('FlowVault'), findsOneWidget);
    expect(find.text('TOTAL ACTIVE PORTFOLIO'), findsOneWidget);

    await tester.tap(find.text('Accounts'));
    await tester.pumpAndSettle();

    expect(find.text('TOTAL LIQUIDITY ACROSS POOLS'), findsOneWidget);
  });
}
