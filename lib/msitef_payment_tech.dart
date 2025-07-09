import 'package:msitef_payment_tech/licence/licence_service.dart';

import 'payments/payment.dart';

export 'payments/model/tef_transaction_model.dart';
export 'payments/model/tef_transaction_response.dart';

class MSitefPaymentTech {
  MSitefPaymentTech._();

  Payment? _payment;

  static MSitefPaymentTech? _instance;

//GET instance from MSitefPaymentTech
  static MSitefPaymentTech instance() {
    _instance ??= MSitefPaymentTech._();
    return _instance!;
  }

//Function to init payment and register handler from notify
  Future<Payment> initPayment({
    required String licenceKey,
  }) async {
    try {
      final result = await LicenceService().checkLicence(licenceKey: licenceKey);
      if (!result) {
        throw Exception('Invalid licence key. Please check your licence key.');
      }
      _payment ??= Payment();
      return _payment!;
    } catch (e) {
      print('Error initializing payment: $e');
      rethrow;
    }
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment not initialized. Please call initPayment() before accessing payment.');
    }
    return _payment!;
  }
}
