import 'package:flutter/material.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';
import 'package:msitef_payment_tech/payments/model/params/msitef_type.dart';
import 'package:msitef_payment_tech/payments/model/params/transaction_params.dart';
import 'package:msitef_payment_tech/payments/payment.dart';

class PaymentPage extends StatefulWidget {
  const PaymentPage({Key? key}) : super(key: key);

  @override
  _PaymentPageState createState() => _PaymentPageState();
}

class _PaymentPageState extends State<PaymentPage> {
  final TextEditingController _moneyController = TextEditingController(text: '10.00');
  final TextEditingController _licenceKeyController = TextEditingController(
    text: 'SUA_LICENCE_KEY',
  );
  final TextEditingController _licenceInternalKeyController = TextEditingController(
    text: '***REMOVIDO***',
  );

  Payment? _payment;
  bool _isInitializing = false;
  bool _isProcessing = false;
  String? _statusMessage;

  @override
  void initState() {
    super.initState();
    _bootstrapPayment();
  }

  Future<void> _bootstrapPayment() async {
    setState(() {
      _isInitializing = true;
      _statusMessage = 'Inicializando licença...';
    });
    try {
      _payment = await MSitefPaymentTech.I.initPayment(
        licenceKey: _licenceKeyController.text.trim(),
        licenceInternalKey: _licenceInternalKeyController.text.trim(),
      );
      setState(() {
        _statusMessage = '✅ Licença validada com sucesso.';
      });
    } catch (error) {
      setState(() {
        _payment = null;
        _statusMessage = '❌ Erro ao validar licença: $error';
      });
    } finally {
      setState(() {
        _isInitializing = false;
      });
    }
  }

  bool get _isReady => _payment != null && MSitefPaymentTech.I.isLicenceValid && !_isInitializing;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: <Widget>[
          const SizedBox(
            height: 20,
          ),
          _LicenceSection(
            licenceKeyController: _licenceKeyController,
            licenceInternalKeyController: _licenceInternalKeyController,
            onReinitialize: _isInitializing ? null : _bootstrapPayment,
            isInitializing: _isInitializing,
          ),
          const SizedBox(height: 20),
          _AmountField(controller: _moneyController, enabled: _isReady && !_isProcessing),
          const SizedBox(height: 20),
          const Text(
            "Selecione o método de pagamento",
            style: TextStyle(fontSize: 16),
          ),
          const SizedBox(
            height: 20,
          ),
          Wrap(
            spacing: 10.0,
            children: <Widget>[
              ElevatedButton(
                child: const Text("Débito"),
                onPressed: !_isReady || _isProcessing ? null : () => _processTransaction(modality: MSitefType.debit),
              ),
              ElevatedButton(
                child: const Text("Crédito"),
                onPressed: !_isReady || _isProcessing ? null : () => _processTransaction(modality: MSitefType.credit),
              ),
              ElevatedButton(
                child: const Text("Pix"),
                onPressed: !_isReady || _isProcessing ? null : () => _processTransaction(modality: MSitefType.pix),
              ),
              ElevatedButton(
                child: const Text("Voucher"),
                onPressed: !_isReady || _isProcessing ? null : () => _processTransaction(modality: MSitefType.voucher),
              ),
            ],
          ),
          const SizedBox(
            height: 20,
          ),
          if (_isProcessing) const CircularProgressIndicator(),
          if (_statusMessage != null) ...[
            const SizedBox(height: 20),
            Text(
              _statusMessage!,
              textAlign: TextAlign.center,
              style: TextStyle(
                color: _statusMessage!.startsWith('✅') ? Colors.green : Colors.red,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Future<void> _processTransaction({required MSitefType modality}) async {
    final valueInCents = _parseValueToCents(_moneyController.text);
    if (valueInCents == null || valueInCents <= 0) {
      setState(() {
        _statusMessage = '❌ Informe um valor válido em centavos (ex: 10.00).';
      });
      return;
    }
    final payment = _payment;
    if (payment == null) {
      setState(() {
        _statusMessage = '❌ Inicialize a licença antes de realizar transações.';
      });
      return;
    }

    setState(() {
      _isProcessing = true;
      _statusMessage = 'Processando transação ${modality.name}...';
    });

    try {
      final response = await payment.mSitefTransaction(
        transactionParams: TransactionParams(
          modalidade: modality,
          value: valueInCents.toString(),
          couponNumber: DateTime.now().toIso8601String(),
          dateFiscal: _formatTodayAsDate(),
          hourFiscal: _formatNowAsTime(),
          operador: 'Operador Demo',
          ipSitef: '192.168.0.165',
          storeId: '00000000',
          terminalId: '00000001',
          companyDocument: '00000000000000',
          automationDocument: '00000000000000',
        ),
      );

      setState(() {
        _statusMessage = response == null ? 'ℹ️ Nenhum retorno do m-SiTef. Verifique o terminal.' : '✅ Transação finalizada.\nResultado: ${response.resultCode}';
      });
    } catch (error) {
      setState(() {
        _statusMessage = '❌ Erro ao processar transação: $error';
      });
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }

  int? _parseValueToCents(String raw) {
    final normalized = raw.replaceAll(RegExp(r'[^0-9]'), '');
    return int.tryParse(normalized);
  }

  String _formatTodayAsDate() {
    final now = DateTime.now();
    final year = now.year.toString().padLeft(4, '0');
    final month = now.month.toString().padLeft(2, '0');
    final day = now.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  String _formatNowAsTime() {
    final now = DateTime.now();
    final hour = now.hour.toString().padLeft(2, '0');
    final minute = now.minute.toString().padLeft(2, '0');
    final second = now.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }

  @override
  void dispose() {
    _moneyController.dispose();
    _licenceKeyController.dispose();
    _licenceInternalKeyController.dispose();
    super.dispose();
  }
}

class _AmountField extends StatelessWidget {
  const _AmountField({
    required this.controller,
    required this.enabled,
  });

  final TextEditingController controller;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      decoration: const InputDecoration(
        labelText: 'Valor (R\$)',
        hintText: 'Ex.: 10.00',
        border: OutlineInputBorder(),
      ),
      enabled: enabled,
    );
  }
}

class _LicenceSection extends StatelessWidget {
  const _LicenceSection({
    required this.licenceKeyController,
    required this.licenceInternalKeyController,
    required this.onReinitialize,
    required this.isInitializing,
  });

  final TextEditingController licenceKeyController;
  final TextEditingController licenceInternalKeyController;
  final VoidCallback? onReinitialize;
  final bool isInitializing;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        TextField(
          controller: licenceKeyController,
          decoration: const InputDecoration(
            labelText: 'Licence Key',
            border: OutlineInputBorder(),
          ),
          maxLines: 2,
        ),
        const SizedBox(height: 12),
        TextField(
          controller: licenceInternalKeyController,
          decoration: const InputDecoration(
            labelText: 'Licence Internal Key',
            border: OutlineInputBorder(),
          ),
          maxLines: 2,
        ),
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: onReinitialize,
          icon: isInitializing
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Icon(Icons.vpn_key),
          label: Text(isInitializing ? 'Validando licença...' : 'Inicializar licença'),
        ),
      ],
    );
  }
}
