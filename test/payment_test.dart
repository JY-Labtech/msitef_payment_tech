import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  final calls = <MethodCall>[];

  /// Mock do nativo: devolve [response] (JSON do m-SiTef) ou lança [error].
  void mockNative({Map<String, dynamic>? response, Object? error}) {
    messenger.setMockMethodCallHandler(msitefChannel, (call) async {
      calls.add(call);
      if (error != null) throw error;
      return jsonEncode(response ??
          {
            'resultCode': 0,
            'tefNSUAuthorizing': '123456',
            'tefNSUSitef': '654321',
            'tefCodAuthorization': 'AUT01',
            'customerReceipt': 'VIA CLIENTE',
            'establishmentReceipt': 'VIA LOJA',
          });
    });
  }

  TransactionParams params({MSitefType modalidade = MSitefType.credit}) =>
      TransactionParams(
        modalidade: modalidade,
        value: '1000',
        couponNumber: '1',
        dateFiscal: '20261127',
        hourFiscal: '120000',
        operador: 'op',
        ipSitef: '127.0.0.1',
        storeId: '00000000',
        terminalId: 'TE000001',
        companyDocument: '11111111111111',
        comExternal: '',
        isDoubleValidation: '',
        otp: '',
        enabledTransactions: '',
        automationDocument: '22222222222222',
      );

  Future<TefTransactionResponseMSitef?> menu(Payment payment) =>
      payment.mSitefMenu(
        ipSitef: '127.0.0.1',
        storeId: '00000000',
        terminalId: 'TE000001',
        companyDocument: '11111111111111',
        comExternal: '',
        isDoubleValidation: '',
        otp: '',
        enabledTransactions: '',
        automationDocument: '22222222222222',
        tokenRegistroTls: 'TOKEN',
      );

  setUp(calls.clear);
  tearDown(() => messenger.setMockMethodCallHandler(msitefChannel, null));

  // Precisa ser o primeiro teste: MSitefPaymentTech/Payment são singletons.
  test('payment antes do initPayment lança exceção', () {
    expect(() => MSitefPaymentTech.I.payment, throwsException);
  });

  test('canal fixo msitef_payment_tech', () {
    expect(CHANNEL_NAME, 'msitef_payment_tech');
    expect(msitefChannel.name, CHANNEL_NAME);
  });

  group('initPayment', () {
    test('não chama o nativo e devolve sempre a mesma instância', () async {
      mockNative();
      final first = await MSitefPaymentTech.I.initPayment();
      final second = await MSitefPaymentTech.I.initPayment();

      expect(identical(first, second), isTrue);
      expect(identical(MSitefPaymentTech.I.payment, first), isTrue);
      expect(identical(await Payment.create(), first), isTrue);
      expect(calls, isEmpty);
    });
  });

  group('mSitefTransaction', () {
    test('envia os parâmetros ao nativo e devolve a resposta', () async {
      mockNative();
      final payment = await MSitefPaymentTech.I.initPayment();

      final response =
          await payment.mSitefTransaction(transactionParams: params());

      final call = calls.single;
      expect(call.method, 'transactionMsitef');
      final args = Map<String, dynamic>.from(call.arguments as Map);
      expect(args['modalidade'], 3);
      expect(args['value'], '1000');
      expect(args['couponNumber'], '1');
      expect(args['date'], '20261127');
      expect(args['hour'], '120000');
      expect(args['operator'], 'op');
      expect(args['ipSitef'], '127.0.0.1');
      expect(args['storeId'], '00000000');
      expect(args['terminalId'], 'TE000001');
      expect(args['companyDocument'], '11111111111111');
      expect(args['automationDocument'], '22222222222222');
      expect(args['tokenRegistroTls'], '');

      expect(response, isNotNull);
      expect(response!.resultCode, 0);
      expect(response.tefNSUAuthorizing, '123456');
      expect(response.tefNSUSitef, '654321');
      expect(response.tefCodAuthorization, 'AUT01');
      expect(response.customerReceipt, 'VIA CLIENTE');
      expect(response.establishmentReceipt, 'VIA LOJA');
      expect(response.errorTransaction, isNull);
    });

    test('cada MSitefType envia a modalidade do m-SiTef', () async {
      mockNative();
      final payment = await MSitefPaymentTech.I.initPayment();
      const expected = {
        MSitefType.credit: 3,
        MSitefType.debit: 2,
        MSitefType.voucher: 2,
        MSitefType.pix: 122,
        MSitefType.refound: 200,
        MSitefType.reprinter: 114,
        MSitefType.administration: 110,
        MSitefType.general: 0,
      };
      for (final entry in expected.entries) {
        calls.clear();
        await payment.mSitefTransaction(
            transactionParams: params(modalidade: entry.key));
        expect(calls.single.arguments['modalidade'], entry.value,
            reason: entry.key.name);
      }
    });

    test('código 0 sem NSU do autorizador vira erro 99999', () async {
      mockNative(response: {'resultCode': 0, 'tefNSUAuthorizing': ''});
      final payment = await MSitefPaymentTech.I.initPayment();
      final response =
          await payment.mSitefTransaction(transactionParams: params());
      expect(response!.resultCode, 99999);
      expect(response.errorTransaction, 'Erro desconhecido.');
    });

    test('código de erro do m-SiTef preenche errorTransaction', () async {
      mockNative(response: {'resultCode': -2});
      final payment = await MSitefPaymentTech.I.initPayment();
      final response =
          await payment.mSitefTransaction(transactionParams: params());
      expect(response!.resultCode, -2);
      expect(response.errorTransaction, 'Operação cancelada pelo operador.');
    });

    test('PlatformException do nativo devolve null', () async {
      mockNative(error: PlatformException(code: 'TRANSACTION_CANCELLED'));
      final payment = await MSitefPaymentTech.I.initPayment();
      expect(await payment.mSitefTransaction(transactionParams: params()),
          isNull);
    });
  });

  group('mSitefMenu', () {
    test('envia os parâmetros ao nativo e devolve a resposta', () async {
      mockNative();
      final payment = await MSitefPaymentTech.I.initPayment();
      final response = await menu(payment);

      final call = calls.single;
      expect(call.method, 'menuMSitef');
      expect(call.arguments['ipSitef'], '127.0.0.1');
      expect(call.arguments['storeId'], '00000000');
      expect(call.arguments['terminalId'], 'TE000001');
      expect(call.arguments['companyDocument'], '11111111111111');
      expect(call.arguments['automationDocument'], '22222222222222');
      expect(call.arguments['tokenRegistroTls'], 'TOKEN');
      expect(response!.resultCode, 0);
    });

    test('NO_ACTIVITY devolve null', () async {
      mockNative(error: PlatformException(code: 'NO_ACTIVITY'));
      final payment = await MSitefPaymentTech.I.initPayment();
      expect(await menu(payment), isNull);
    });
  });

  group('customAction', () {
    test('repassa os params ao método action', () async {
      mockNative();
      final payment = await MSitefPaymentTech.I.initPayment();
      final response = await payment.customAction(params: {
        'mapMsiTef': {'modalidade': '114'},
      });

      final call = calls.single;
      expect(call.method, 'action');
      expect(call.arguments['mapMsiTef'], {'modalidade': '114'});
      expect(response!.resultCode, 0);
    });

    test('erro do nativo devolve null', () async {
      mockNative(error: PlatformException(code: 'ERROR'));
      final payment = await MSitefPaymentTech.I.initPayment();
      expect(await payment.customAction(params: {'mapMsiTef': {}}), isNull);
    });
  });

  group('modelos e helpers', () {
    test('MSitefType.fromString é o inverso de modality', () {
      for (final type in MSitefType.values) {
        if (type == MSitefType.voucher) continue; // voucher usa '2', como débito
        expect(MSitefType.fromString(type.modality), type);
      }
      expect(MSitefType.fromString('999'), MSitefType.general);
    });

    test('TransactionParams copyWith e igualdade', () {
      final p = params();
      expect(p.copyWith(), p);
      expect(p.copyWith(value: '2000').value, '2000');
      expect(p.copyWith(value: '2000') == p, isFalse);
    });

    test('TefHelper formata valores', () {
      expect(TefHelper.onlyNumberFrom8Positions(text: '12-3'), '00000123');
      expect(
          TefHelper.onlyNumberFrom8Positions(text: '1234567890'), '12345678');
      expect(TefHelper.priceSanitized(10.5), '10,50');
      expect(TefHelper.operatorSanitize('  op  '), 'op');
      expect(TefHelper.operatorSanitize('a' * 30).length, 20);
      expect(TefHelper.onlyNumberString(text: '12.345.678/0001-90'),
          '12345678000190');
      expect(
          TefHelper.checkErrorTransactionTEF(51), {51: 'Saldo Insuficiente'});
      expect(TefHelper.tefAlert('-5'), isTrue);
      expect(TefHelper.tefAlert('0'), isFalse);
    });
  });
}
