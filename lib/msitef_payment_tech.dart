import 'package:msitef_payment_tech/payments/payment.dart';

import 'licence/constants/licence_constants.dart';

final class MSitefPaymentTech {
  static MSitefPaymentTech? _instance;

  Payment? _payment;

  MSitefPaymentTech._internal();

  static MSitefPaymentTech get I {
    _instance ??= MSitefPaymentTech._internal();
    return _instance!;
  }

  Payment initPayment() {
    if (_payment != null) {
      return _payment!;
    }
    _payment = Payment.create(channel: channel);
    return _payment!;
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment not initialized. Call initPayment() first.');
    }
    return _payment!;
  }

  bool get isLicenceValid => _payment != null;
}

//Fixed channel name
// ignore: constant_identifier_names
const CHANNEL_NAME = "msitef_payment_tech";
