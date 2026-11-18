# FlowVault (Flutter)

FlowVault is a loan management, debt tracker, and virtual account ledger application built exclusively with **Flutter** and **Material 3**.

## Project Tree

```text
flutter_app/
├── README.md
├── analysis_options.yaml
├── pubspec.yaml
├── lib/
│   ├── main.dart
│   ├── models/
│   │   └── flow_vault_models.dart
│   ├── screens/
│   │   ├── accounts_screen.dart
│   │   ├── borrowers_screen.dart
│   │   ├── dashboard_screen.dart
│   │   ├── home_shell.dart
│   │   └── loans_screen.dart
│   ├── services/
│   │   └── interest_calculator.dart
│   ├── state/
│   │   └── flow_vault_store.dart
│   ├── theme/
│   │   └── app_theme.dart
│   ├── utils/
│   │   └── currency_formatter.dart
│   └── widgets/
│       ├── create_loan_sheet.dart
│       └── record_payment_sheet.dart
└── test/
    └── flow_vault_test.dart
```

## Running Locally

Because AI Studio's cloud container is an Android-only environment without the Flutter SDK installed, run or test this project locally on your machine:

```bash
cd flutter_app
flutter create .
flutter pub get
flutter test
flutter run
```
