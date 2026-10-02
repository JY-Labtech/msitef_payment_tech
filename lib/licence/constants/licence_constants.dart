import 'package:flutter/services.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

/// URL (base64) de validação de licença do servidor oficial da JY Labtech:
/// https://licence-server.invalid/pos-payments/licence/check
///
/// É o valor padrão de `licenceInternalKey`. O nativo sempre valida nesse servidor:
/// valores que apontem para outro endereço são ignorados.
const String licenceCheck =
    '***REMOVIDO***';

const channel = MethodChannel(CHANNEL_NAME);
