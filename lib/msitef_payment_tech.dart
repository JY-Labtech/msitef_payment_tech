import 'package:msitef_payment_tech/licence/licence_service.dart';
import 'payments/payment.dart';

export 'payments/model/tef_transaction_model.dart';
export 'payments/model/tef_transaction_response.dart';

class MSitefPaymentTech {
  // Instância única privada (singleton)
  static final MSitefPaymentTech _instance = MSitefPaymentTech._internal();

  late final Payment? _payment;

  // Construtor privado
  MSitefPaymentTech._internal();

  // Getter para acessar a instância
  static MSitefPaymentTech get instance => _instance;

  /// Inicializa o pagamento e verifica a licença
  Future<Payment> initPayment({
    required String licenceKey,
  }) async {
    try {
      final isValid = await LicenceService().checkLicence(licenceKey: licenceKey);
      if (!isValid) {
        throw Exception('Invalid licence key. Please check your licence key.');
      }

      _payment = Payment();
      return _payment!;
    } catch (e) {
      print('Error initializing payment: $e');
      rethrow;
    }
  }

  /// Acesso ao pagamento (deve ser inicializado antes)
  Payment get payment {
    if (!_isPaymentInitialized) {
      throw Exception('Payment not initialized. Call initPayment() first.');
    }
    return _payment!;
  }

  bool get _isPaymentInitialized => _payment != null;
}
