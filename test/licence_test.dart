import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:msitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';
import 'package:msitef_payment_tech/payments/model/params/msitef_type.dart';
import 'package:msitef_payment_tech/payments/model/params/transaction_params.dart';

// Chave fictícia, apenas para teste.
const _licenceKey = 'JYL-TEST-0000-0000';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  final calls = <MethodCall>[];

  /// Configura o mock do canal. [licenceValid] controla a resposta do nativo
  /// para initPayment e para o gate de licença das operações.
  void mockNative({required bool licenceValid, bool initValid = true}) {
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      if (call.method == 'initPayment') {
        if (!initValid) {
          throw PlatformException(
              code: 'LICENCE_ERROR', message: 'Licença vencida');
        }
        return true;
      }
      if (!licenceValid) {
        throw PlatformException(
            code: 'LICENCE_ERROR', message: 'Licença cancelada');
      }
      return jsonEncode({
        'resultCode': 0,
        'tefNSUAuthorizing': '123456',
        'tefNSUSitef': '654321',
      });
    });
  }

  TransactionParams params() => TransactionParams(
        modalidade: MSitefType.credit,
        value: '10,00',
        couponNumber: '1',
        dateFiscal: '20261001',
        hourFiscal: '120000',
        operador: 'op',
        ipSitef: '127.0.0.1',
        storeId: '00000000',
        terminalId: 'TE000001',
        paramsAdd: '',
        companyDocument: '00000000000000',
        comExternal: '',
        isDoubleValidation: '',
        otp: '',
        enabledTransactions: '',
        automationDocument: '00000000000000',
      );

  setUp(calls.clear);

  tearDown(() => messenger.setMockMethodCallHandler(channel, null));

  // Precisa ser o primeiro teste: MSitefPaymentTech/Payment são singletons.
  test('payment antes do initPayment falha', () {
    expect(() => MSitefPaymentTech.I.payment, throwsException);
    expect(MSitefPaymentTech.I.isLicenceValid, isFalse);
  });

  test('licenceCheck aponta para o servidor oficial (jylabtech_core)', () {
    final url = utf8.decode(base64.decode(licenceCheck));
    expect(url,
        startsWith('https://portal-api.jylabtech.com.br/pos-payments/'));
    expect(url,
        'https://portal-api.jylabtech.com.br/pos-payments/licence/check');
  });

  test('initPayment envia licenceKey e licenceInternalKey padrão do core',
      () async {
    mockNative(licenceValid: true);
    await MSitefPaymentTech.I.initPayment(licenceKey: _licenceKey);

    final init = calls.singleWhere((c) => c.method == 'initPayment');
    expect(init.arguments['licenceKey'], _licenceKey);
    expect(init.arguments['licenceInternalKey'], licenceCheck);
    expect(MSitefPaymentTech.I.isLicenceValid, isTrue);
  });

  test('initPayment repassa licenceInternalKey informado', () async {
    mockNative(licenceValid: true);
    await MSitefPaymentTech.I
        .initPayment(licenceKey: _licenceKey, licenceInternalKey: 'outro');

    final init = calls.singleWhere((c) => c.method == 'initPayment');
    expect(init.arguments['licenceInternalKey'], 'outro');
  });

  test('initPayment propaga LICENCE_ERROR do nativo', () async {
    mockNative(licenceValid: false, initValid: false);
    await expectLater(
      MSitefPaymentTech.I.initPayment(licenceKey: _licenceKey),
      throwsA(isA<PlatformException>()
          .having((e) => e.code, 'code', 'LICENCE_ERROR')),
    );
    expect(MSitefPaymentTech.I.isLicenceValid, isFalse);
  });

  test('operações com licença válida retornam a resposta do m-SiTef',
      () async {
    mockNative(licenceValid: true);
    final payment =
        await MSitefPaymentTech.I.initPayment(licenceKey: _licenceKey);

    final response =
        await payment.mSitefTransaction(transactionParams: params());
    expect(response, isNotNull);
    expect(response!.resultCode, 0);
  });

  test('LICENCE_ERROR nas operações nunca devolve sucesso ao app', () async {
    mockNative(licenceValid: true);
    final payment =
        await MSitefPaymentTech.I.initPayment(licenceKey: _licenceKey);

    // licença deixa de ser válida no nativo (cache expirado, cancelada etc.)
    mockNative(licenceValid: false);
    expect(await payment.mSitefTransaction(transactionParams: params()),
        isNull);
    expect(payment.hasValidLicence, isFalse);

    expect(
      await payment.mSitefMenu(
        ipSitef: '127.0.0.1',
        storeId: '00000000',
        terminalId: 'TE000001',
        companyDocument: '00000000000000',
        comExternal: '',
        isDoubleValidation: '',
        otp: '',
        enabledTransactions: '',
        automationDocument: '00000000000000',
      ),
      isNull,
    );
    expect(await payment.customAction(params: {'modalidade': '110'}), isNull);
  });

  test('operação não chega ao m-SiTef se o initPayment falhar', () async {
    mockNative(licenceValid: true);
    final payment =
        await MSitefPaymentTech.I.initPayment(licenceKey: _licenceKey);

    mockNative(licenceValid: false);
    await payment.mSitefTransaction(transactionParams: params());
    calls.clear();

    // após LICENCE_ERROR o Dart refaz o initPayment antes da próxima operação;
    // se ele falhar, a operação nem é enviada ao nativo.
    mockNative(licenceValid: false, initValid: false);
    expect(await payment.mSitefTransaction(transactionParams: params()),
        isNull);
    expect(calls.map((c) => c.method), ['initPayment']);
  });
}
