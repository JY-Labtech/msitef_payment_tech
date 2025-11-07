// ignore_for_file: constant_identifier_names
import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:msitef_payment_tech/payments/enum/sitef_modality.dart';
import 'package:msitef_payment_tech/payments/helper/tef_helper.dart';
import 'package:msitef_payment_tech/payments/model/params/transaction_params.dart';
import 'package:msitef_payment_tech/payments/model/tef_transaction_model.dart';
import 'package:msitef_payment_tech/payments/model/tef_transaction_response.dart';

final class Payment {
  final MethodChannel channel;

  // Cache da instância criada
  static Payment? _instance;

  final Map<SitefModalityMSitef, String> _modalityMap = {
    SitefModalityMSitef.undefined: '0',
    SitefModalityMSitef.creditCard: '3',
    SitefModalityMSitef.debitCard: '2',
    SitefModalityMSitef.pix: '122',
    SitefModalityMSitef.digitalWallet: '122',
    SitefModalityMSitef.cancellation: '200',
  };

  String _licenceKey;
  String _licenceInternalKey;
  bool _licenceConfigured;

  Payment._({
    required this.channel,
    required String licenceKey,
    required String licenceInternalKey,
  })  : _licenceKey = licenceKey,
        _licenceInternalKey = licenceInternalKey,
        _licenceConfigured = false;

  static Future<Payment> create({
    required MethodChannel channel,
    required String licenceKey,
    required String licenceInternalKey,
  }) async {
    if (_instance == null) {
      _instance = Payment._(
        channel: channel,
        licenceKey: licenceKey,
        licenceInternalKey: licenceInternalKey,
      );
    } else {
      _instance!._updateLicenceKeys(licenceKey, licenceInternalKey);
    }

    await _instance!._scheduleLicenceSync();
    return _instance!;
  }

  Future<TefTransactionResponseMSitef?> mSitefTransaction({
    required TransactionParams transactionParams,
  }) async {
    try {
      await _ensureLicenceSynced();
      final modality = int.tryParse(
              _modalityMap[transactionParams.modalidade.modality] ?? '0') ??
          0;

      final paramsTransaction = _lineToMethodChannel(
        modalidade: modality,
        value: transactionParams.value,
        couponNumber: transactionParams.couponNumber,
        dateFiscal: transactionParams.dateFiscal,
        hourFiscal: transactionParams.hourFiscal,
        operador: transactionParams.operador,
        ipSitef: transactionParams.ipSitef,
        storeId: transactionParams.storeId,
        terminalId: transactionParams.terminalId,
        paramsAdd: transactionParams.paramsAdd,
        companyDocument: transactionParams.companyDocument,
        comExternal: transactionParams.comExternal,
        isDoubleValidation: transactionParams.isDoubleValidation,
        otp: transactionParams.otp,
        enabledTransactions: transactionParams.enabledTransactions,
        automationDocument: transactionParams.automationDocument,
        tokenRegistroTls: transactionParams.tokenRegistroTls ?? '',
      );

      final resultTransaction =
          await channel.invokeMethod('transactionMsitef', paramsTransaction);
      final resultJson = json.decode(resultTransaction);
      final model = TefTransactionModel.fromMap(resultJson);

      return _fetchTefTransactionResponse(model: model);
    } catch (_) {
      return null;
    }
  }

  Future<TefTransactionResponseMSitef?> mSitefMenu({
    required String ipSitef,
    required String storeId,
    required String terminalId,
    required String companyDocument,
    required String comExternal,
    required String isDoubleValidation,
    required String otp,
    required String enabledTransactions,
    required String automationDocument,
    String? tokenRegistroTls,
  }) async {
    try {
      await _ensureLicenceSynced();
      final paramsTransaction = _lineToMethodChannel(
        ipSitef: ipSitef,
        storeId: storeId,
        terminalId: terminalId,
        companyDocument: companyDocument,
        comExternal: comExternal,
        isDoubleValidation: isDoubleValidation,
        otp: otp,
        enabledTransactions: enabledTransactions,
        automationDocument: automationDocument,
        tokenRegistroTls: tokenRegistroTls,
      );

      final resultTransaction =
          await channel.invokeMethod('menuMSitef', paramsTransaction);
      final resultJson = json.decode(resultTransaction);
      final model = TefTransactionModel.fromMap(resultJson);

      return _fetchTefTransactionResponse(model: model);
    } catch (_) {
      return null;
    }
  }

  Future<TefTransactionResponseMSitef?> customAction({
    required Map<String, dynamic> params,
  }) async {
    try {
      await _ensureLicenceSynced();
      final resultTransaction = await channel.invokeMethod('action', params);
      final resultJson = json.decode(resultTransaction);
      final model = TefTransactionModel.fromMap(resultJson);

      return _fetchTefTransactionResponse(model: model);
    } catch (_) {
      return null;
    }
  }

  bool get hasValidLicence => _licenceConfigured;

  void _updateLicenceKeys(String licenceKey, String licenceInternalKey) {
    _licenceKey = licenceKey;
    _licenceInternalKey = licenceInternalKey;
    _licenceConfigured = false;
  }

  Future<void> _scheduleLicenceSync() async {
    try {
      await channel.invokeMethod('initPayment', {
        'licenceKey': _licenceKey,
        'licenceInternalKey': _licenceInternalKey,
      });
      _licenceConfigured = true;
    } catch (error) {
      _licenceConfigured = false;
      rethrow;
    }
  }

  Future<void> _ensureLicenceSynced() async {
    if (_licenceConfigured) {
      return;
    }

    await _scheduleLicenceSync();
  }

  Map<String, dynamic> _lineToMethodChannel({
    int? modalidade,
    String? value,
    String? couponNumber,
    String? dateFiscal,
    String? hourFiscal,
    String? operador,
    String? params,
    String? ipSitef,
    String? storeId,
    String? terminalId,
    String? paramsAdd,
    String? companyDocument,
    String? comExternal,
    String? isDoubleValidation,
    String? otp,
    String? enabledTransactions,
    String? automationDocument,
    String? tokenRegistroTls,
  }) {
    return {
      'modalidade': modalidade,
      'value': value,
      'couponNumber': couponNumber,
      'date': dateFiscal,
      'hour': hourFiscal,
      'operator': operador,
      'restricoes': params,
      'ipSitef': ipSitef,
      'storeId': storeId,
      'terminalId': terminalId,
      'parametrosAdc': paramsAdd,
      'companyDocument': companyDocument,
      'comExternal': comExternal,
      'isDoubleValidation': isDoubleValidation,
      'otp': otp,
      'enabledTransactions': enabledTransactions,
      'automationDocument': automationDocument,
      'tokenRegistroTls': tokenRegistroTls,
    };
  }

  TefTransactionResponseMSitef _fetchTefTransactionResponse({
    required TefTransactionModel model,
    String? dateTime,
  }) {
    int? resultCode = model.resultCode;
    if (resultCode == 0 && (model.tefNSUAuthorizing?.isEmpty ?? true)) {
      resultCode = 99999;
    }
    final errorTransaction =
        TefHelper.checkErrorTransactionTEF(resultCode ?? 99999);

    return TefTransactionResponseMSitef(
      id: model.id ?? 0,
      tefCodPayment: model.tefCodPayment ?? '',
      tefDescPayment: model.tefDescPayment ?? '',
      tefInstitution: model.tefInstitution ?? '',
      tefCodInstitution: model.tefCodInstitution ?? '',
      tefCardType: model.tefCardType ?? '',
      binCard: model.binCard ?? '',
      tefDigitalWalletId: model.tefDigitalWalletId ?? '',
      tefDigitalWalletType: model.tefDigitalWalletType ?? '',
      tefDigitalWalletName: model.tefDigitalWalletName ?? '',
      tefNSUSitef: model.tefNSUSitef ?? '',
      tefNSUAuthorizing: model.tefNSUAuthorizing ?? '',
      tefCodAuthorization: model.tefCodAuthorization ?? '',
      establishmentReceipt: model.establishmentReceipt ?? '',
      customerReceipt: model.customerReceipt ?? '',
      transactionConfirmation: model.transactionConfirmation ?? '',
      couponModality: model.couponModality ?? '',
      transactionDateTime: dateTime ?? model.transactionDateTime ?? '',
      cancellationMode: model.cancellationMode ?? '',
      amountToPay: model.amountToPay ?? '',
      amountReceived: model.amountReceived ?? '',
      returnValue: model.returnValue ?? '',
      paymentAmount: model.paymentAmount ?? '',
      codCancellationMode: model.codCancellationMode ?? '',
      resultCode: resultCode ?? 999999,
      compDadosConf: model.compDadosConf ?? '',
      codTrans: model.codTrans ?? '',
      redeAut: model.redeAut ?? '',
      codeDocumentCanceled: model.codDocumentCancelled ?? '',
      amountCancelled: model.amountCancelled ?? '',
      cardNumber: model.cardNumber ?? '',
      cardNumberLast: model.cardNumberLast ?? '',
      errorTransaction: resultCode == 0 ? null : errorTransaction[resultCode],
      typeFields: model.typeFields,
    );
  }
}
