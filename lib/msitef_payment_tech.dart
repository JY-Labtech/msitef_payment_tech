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

  Future<Payment> initPayment({
    required String licenceKey,
    required String licenceInternalKey,
  }) async {
    final payment = await Payment.create(
      channel: channel,
      licenceKey: licenceKey,
      licenceInternalKey: licenceInternalKey,
    );
    _payment = payment;
    return _payment!;
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception(
        'Payment not initialized. Call initPayment(licenceKey: ..., licenceInternalKey: ...) first.',
      );
    }
    return _payment!;
  }

  bool get isLicenceValid => _payment?.hasValidLicence ?? false;
}

//Fixed channel name
// ignore: constant_identifier_names
const CHANNEL_NAME = "msitef_payment_tech";
