import 'package:check_licence_tech/check_licence_tech.dart';
import 'package:msitef_payment_tech/payments/payment.dart';

import 'licence/constants/licence_constants.dart';

class MSitefPaymentTech {
  static final MSitefPaymentTech _instance = MSitefPaymentTech._internal();

  Payment? _payment;

  MSitefPaymentTech._internal();

  static MSitefPaymentTech get instance => _instance;

  Future<String> getSerialNumber() async => await CheckLicenceTech.instance().getSerialNumber();

  Future<Payment> initPayment({
    required String licenceKey,
  }) async {
    _payment = await Payment.create(channel: channel, licenceKey: licenceKey);
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
