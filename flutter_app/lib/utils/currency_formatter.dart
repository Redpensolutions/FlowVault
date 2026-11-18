class CurrencyFormatter {
  CurrencyFormatter._();

  /// Formats numbers using Indian numbering grouping (e.g., ₹ 8,00,000).
  static String format(double amount, {String symbol = '₹', bool showDecimals = false}) {
    final isNegative = amount < 0;
    final absVal = amount.abs();
    final fixed = showDecimals ? absVal.toStringAsFixed(2) : absVal.round().toString();
    final parts = fixed.split('.');
    final intPart = parts[0];

    String formattedInt;
    if (intPart.length <= 3) {
      formattedInt = intPart;
    } else {
      final last3 = intPart.substring(intPart.length - 3);
      var rest = intPart.substring(0, intPart.length - 3);
      final groups = <String>[];
      while (rest.length > 2) {
        groups.insert(0, rest.substring(rest.length - 2));
        rest = rest.substring(0, rest.length - 2);
      }
      if (rest.isNotEmpty) {
        groups.insert(0, rest);
      }
      formattedInt = '${groups.join(',')},$last3';
    }

    final decimalPart = parts.length > 1 ? '.${parts[1]}' : '';
    final sign = isNegative ? '-' : '';
    return '$sign$symbol $formattedInt$decimalPart';
  }

  static String formatShortDate(DateTime date) {
    const months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
    ];
    return '${date.day.toString().padLeft(2, '0')} ${months[date.month - 1]} ${date.year}';
  }
}
