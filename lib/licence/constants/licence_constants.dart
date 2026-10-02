import 'package:flutter/services.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

/// URL (base64) de validação de licença do servidor oficial da JY Labtech:
/// https://portal-api.jylabtech.com.br/pos-payments/licence/check
///
/// É o valor padrão de `licenceInternalKey`. O nativo sempre valida nesse servidor:
/// valores que apontem para outro endereço são ignorados.
const String licenceCheck =
    'aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=';

const channel = MethodChannel(CHANNEL_NAME);
