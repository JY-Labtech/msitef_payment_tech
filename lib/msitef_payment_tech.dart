import 'package:msitef_payment_tech/payments/constants/channel_constants.dart';
import 'package:msitef_payment_tech/payments/payment.dart';

export 'payments/constants/channel_constants.dart';
export 'payments/enum/sitef_modality.dart';
export 'payments/helper/tef_helper.dart';
export 'payments/model/params/msitef_params.dart';
export 'payments/model/params/msitef_type.dart';
export 'payments/model/params/transaction_params.dart';
export 'payments/model/tef_transaction_model.dart';
export 'payments/model/tef_transaction_response.dart';
export 'payments/payment.dart';

/// Ponto de entrada do plugin: integra o app Flutter com o m-SiTef
/// (Software Express) via intent no Android.
final class MSitefPaymentTech {
  static MSitefPaymentTech? _instance;

  Payment? _payment;

  MSitefPaymentTech._internal();

  /// Instância única do plugin.
  static MSitefPaymentTech get I {
    _instance ??= MSitefPaymentTech._internal();
    return _instance!;
  }

  /// Inicializa o plugin e devolve o [Payment] usado nas operações.
  ///
  /// Não faz chamada de rede nem ao nativo; pode ser chamado mais de uma vez
  /// (devolve sempre a mesma instância).
  Future<Payment> initPayment() async {
    _payment = await Payment.create(channel: msitefChannel);
    return _payment!;
  }

  /// [Payment] inicializado por [initPayment].
  ///
  /// Lança [Exception] se [initPayment] ainda não foi chamado.
  Payment get payment {
    if (_payment == null) {
      throw Exception(
        'Payment not initialized. Call MSitefPaymentTech.I.initPayment() first.',
      );
    }
    return _payment!;
  }
}
