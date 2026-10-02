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

  /// Configura e valida a licença no servidor da JY Labtech.
  ///
  /// [licenceInternalKey] é opcional (padrão: [licenceCheck]). Valores que não apontem
  /// para o servidor oficial são ignorados pelo nativo. Lança [PlatformException]
  /// (`LICENCE_ERROR`) se a licença for inválida, vencida, suspensa ou cancelada.
  Future<Payment> initPayment({
    required String licenceKey,
    String licenceInternalKey = licenceCheck,
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
        'Payment not initialized. Call initPayment(licenceKey: ...) first.',
      );
    }
    return _payment!;
  }

  bool get isLicenceValid => _payment?.hasValidLicence ?? false;
}

//Fixed channel name
// ignore: constant_identifier_names
const CHANNEL_NAME = "msitef_payment_tech";
