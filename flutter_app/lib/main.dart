import 'package:flutter/material.dart';

import 'screens/home_shell.dart';
import 'state/flow_vault_store.dart';
import 'theme/app_theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const FlowVaultApp());
}

class FlowVaultApp extends StatefulWidget {
  const FlowVaultApp({super.key});

  @override
  State<FlowVaultApp> createState() => _FlowVaultAppState();
}

class _FlowVaultAppState extends State<FlowVaultApp> {
  final FlowVaultStore _store = FlowVaultStore();

  @override
  void dispose() {
    _store.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FlowVault',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme(),
      darkTheme: AppTheme.darkTheme(),
      themeMode: ThemeMode.light,
      home: HomeShell(store: _store),
    );
  }
}
