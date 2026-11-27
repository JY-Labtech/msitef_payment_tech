# msitef_payment_tech

> Open source Flutter plugin (MIT) for Android that integrates your app with the m-SiTef TEF app (Software Express) via intent: credit, debit, PIX, voucher, refund and reprint.

[![pub package](https://img.shields.io/pub/v/msitef_payment_tech.svg)](https://pub.dev/packages/msitef_payment_tech)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Plugin Flutter para integrar sua aplicação Android com o **m-SiTef**, o app de TEF da Software Express (Fiserv). O plugin abre o m-SiTef por intent (`br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF`), repassa os parâmetros da transação e devolve o resultado (códigos, NSU, autorização e comprovantes) como um objeto Dart.

Projeto **open source** sob licença MIT, mantido pela [JY Labtech](https://jylabtech.com.br). Não há licença paga, cadastro nem chamadas a servidores da JY Labtech.

> **Aviso:** este plugin **não é oficial** da Software Express/Fiserv. Ele só automatiza a chamada ao m-SiTef instalado no aparelho.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Compatibilidade e requisitos](#compatibilidade-e-requisitos)
- [Instalação](#instalação)
- [Configuração Android](#configuração-android)
- [Uso rápido](#uso-rápido)
- [Referência da API](#referência-da-api)
- [Exemplos por operação](#exemplos-por-operação)
- [Tratamento de erros](#tratamento-de-erros)
- [App de exemplo](#app-de-exemplo)
- [Testes](#testes)
- [Migração da 2.x para a 3.0.0](#migração-da-2x-para-a-300)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Créditos](#créditos)

## Funcionalidades

| Operação | Como chamar | Modalidade enviada ao m-SiTef |
|----------|-------------|-------------------------------|
| Crédito (à vista ou parcelado, escolhido na tela do m-SiTef) | `mSitefTransaction` com `MSitefType.credit` | `3` |
| Débito | `mSitefTransaction` com `MSitefType.debit` | `2` |
| Voucher | `mSitefTransaction` com `MSitefType.voucher` | `2` |
| PIX | `mSitefTransaction` com `MSitefType.pix` | `122` (com `transacoesHabilitadas=7;`) |
| Cancelamento / estorno | `mSitefTransaction` com `MSitefType.refound` | `200` |
| Reimpressão de comprovante | `mSitefTransaction` com `MSitefType.reprinter` | `114` |
| Menu administrativo | `mSitefMenu(...)` ou `mSitefTransaction` com `MSitefType.administration` | `110` |
| Menu geral (o operador escolhe a operação) | `mSitefTransaction` com `MSitefType.general` | `0` |
| Chamada livre (qualquer extra do m-SiTef) | `customAction(params: {'mapMsiTef': {...}})` | a que você informar |
| Comunicação externa (GSurf/TLS: `comExterna`, `otp`, `isDoubleValidation`, `tokenRegistroTls`) | campos de `TransactionParams` / `mSitefMenu` | - |

Também inclui `TefHelper`, com tabelas de mensagens dos códigos de retorno do SiTef, da configuração e da GSurf, e funções de formatação.

## Compatibilidade e requisitos

- **Plataforma:** somente **Android** (não há implementação iOS).
- **App do fabricante:** o **m-SiTef** precisa estar instalado e configurado no aparelho. O `.apk` é fornecido pela sua distribuidora/credenciadora do TEF (Software Express). Sem ele o intent não abre e a operação retorna `null`.
- **Aparelhos:** qualquer Android com o m-SiTef instalado (POS Android, smartphone ou tablet com pinpad). O plugin envia `tipoPinpad=ANDROID_USB`.
- **minSdk:** 23 (Android 6.0).
- **Dart:** `>=3.0.0 <4.0.0`. **Flutter:** `>=3.0.0`.
- **Proxy:** o m-SiTef não funciona atrás de proxy.
- **Permissões declaradas pelo plugin** (mescladas no Manifest do seu app):
  - `android.permission.USB_PERMISSION`, `android.permission.NFC`
  - `android.permission.BLUETOOTH_SCAN`, `android.permission.BLUETOOTH_CONNECT`, `android.permission.BLUETOOTH` e `android.permission.BLUETOOTH_ADMIN` (as duas últimas até a API 30)
  - `android.permission.WRITE_EXTERNAL_STORAGE`, `android.permission.MANAGE_EXTERNAL_STORAGE`, `android.permission.READ_PHONE_STATE`
  - `uses-feature`: `android.hardware.usb.host` (**obrigatório**), `android.hardware.nfc` e `android.hardware.bluetooth_le` (opcional)

## Instalação

```bash
flutter pub add msitef_payment_tech
```

Ou, no `pubspec.yaml`:

```yaml
dependencies:
  msitef_payment_tech: ^3.0.0
```

## Configuração Android

Não é preciso configurar repositório Maven, credenciais nem flavors: o código nativo do plugin já vem pré-compilado em `android/libs` e as dependências (Gson e ZXing) vêm do Maven Central.

1. Garanta `minSdk` 23 ou maior em `android/app/build.gradle`:

   ```groovy
   android {
       defaultConfig {
           minSdk = 23
       }
   }
   ```

2. Se o seu app usa Jetifier (`android.enableJetifier=true`) e o build falhar ao transformar os jars do engine do Flutter (visto no Flutter 3.47), adicione ao `android/gradle.properties`, como no app de exemplo:

   ```properties
   android.jetifier.ignorelist=armeabi_v7a_,arm64_v8a_,x86_64_,flutter_embedding_
   ```

3. O plugin declara `android.hardware.usb.host` como obrigatório. Se o seu app também roda em aparelhos sem USB host, sobrescreva no `AndroidManifest.xml` do app:

   ```xml
   <manifest xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:tools="http://schemas.android.com/tools">
       <uses-feature android:name="android.hardware.usb.host"
           android:required="false"
           tools:replace="android:required" />
   </manifest>
   ```

Não há `intent-filter` a declarar: o plugin abre o m-SiTef com `startActivityForResult` e recebe a resposta na `Activity` do Flutter.

## Uso rápido

```dart
import 'package:flutter/material.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await MSitefPaymentTech.I.initPayment();
  runApp(const MaterialApp(home: VendaPage()));
}

class VendaPage extends StatelessWidget {
  const VendaPage({super.key});

  Future<void> _pagar(BuildContext context) async {
    final messenger = ScaffoldMessenger.of(context);
    final now = DateTime.now();
    String two(int v) => v.toString().padLeft(2, '0');

    final response = await MSitefPaymentTech.I.payment.mSitefTransaction(
      transactionParams: TransactionParams(
        modalidade: MSitefType.credit,
        value: '1000', // R$ 10,00 em centavos
        couponNumber: '1',
        dateFiscal: '${now.year}${two(now.month)}${two(now.day)}', // AAAAMMDD
        hourFiscal: '${two(now.hour)}${two(now.minute)}${two(now.second)}', // HHMMSS
        operador: 'Caixa 1',
        ipSitef: '192.168.0.10',
        storeId: '00000000',
        terminalId: 'TE000001',
        companyDocument: '00000000000000',
        automationDocument: '00000000000000',
      ),
    );

    final String mensagem;
    if (response == null) {
      mensagem = 'Sem retorno do m-SiTef (cancelado ou não instalado).';
    } else if (response.resultCode == 0) {
      mensagem = 'Aprovado. NSU ${response.tefNSUSitef}';
    } else {
      mensagem = 'Negado: ${response.errorTransaction}';
    }

    messenger.showSnackBar(SnackBar(content: Text(mensagem)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: ElevatedButton(
          onPressed: () => _pagar(context),
          child: const Text('Pagar R\$ 10,00 no crédito'),
        ),
      ),
    );
  }
}
```

Um único import (`package:msitef_payment_tech/msitef_payment_tech.dart`) exporta `MSitefPaymentTech`, `Payment`, `TransactionParams`, `MSitefType`, `TefTransactionResponseMSitef`, `TefHelper` e os demais modelos.

## Referência da API

### `MSitefPaymentTech`

Singleton de entrada do plugin.

| Membro | Retorno | Descrição |
|--------|---------|-----------|
| `MSitefPaymentTech.I` | `MSitefPaymentTech` | Instância única. |
| `initPayment()` | `Future<Payment>` | Cria (ou devolve) o `Payment`. Não chama o nativo nem a rede; pode ser chamado mais de uma vez. |
| `payment` | `Payment` | O `Payment` criado por `initPayment()`. Lança `Exception` se `initPayment()` ainda não foi chamado. |

```dart
final payment = await MSitefPaymentTech.I.initPayment();
// depois, em qualquer lugar:
final mesmoPayment = MSitefPaymentTech.I.payment;
```

### `Payment`

| Método | Retorno | Descrição |
|--------|---------|-----------|
| `static Future<Payment> create({MethodChannel channel = msitefChannel})` | `Future<Payment>` | Cria (ou devolve) a instância única. `channel` só é usado na primeira chamada. Normalmente use `MSitefPaymentTech.I.initPayment()`. |
| `mSitefTransaction({required TransactionParams transactionParams})` | `Future<TefTransactionResponseMSitef?>` | Abre o m-SiTef com a modalidade de `transactionParams.modalidade`. |
| `mSitefMenu({...})` | `Future<TefTransactionResponseMSitef?>` | Abre o menu administrativo (modalidade `110`). |
| `customAction({required Map<String, dynamic> params})` | `Future<TefTransactionResponseMSitef?>` | Envia extras livres ao m-SiTef. |

Todos os métodos de operação retornam `null` quando a operação falha antes de haver uma resposta do m-SiTef (veja [Tratamento de erros](#tratamento-de-erros)).

#### `mSitefMenu`

```dart
Future<TefTransactionResponseMSitef?> mSitefMenu({
  required String ipSitef,
  required String storeId,
  required String terminalId,
  required String companyDocument,
  required String comExternal,        // '' quando não usa comunicação externa
  required String isDoubleValidation, // usado com comExternal == '1'
  required String otp,                // usado com comExternal '1', '2' ou '3'
  required String enabledTransactions, // vira 'transacoesHabilitadas' (ignorado se '')
  required String automationDocument,
  String? tokenRegistroTls,           // enviado se não for vazio
});
```

```dart
final response = await MSitefPaymentTech.I.payment.mSitefMenu(
  ipSitef: '192.168.0.10',
  storeId: '00000000',
  terminalId: 'TE000001',
  companyDocument: '00000000000000',
  comExternal: '',
  isDoubleValidation: '',
  otp: '',
  enabledTransactions: '',
  automationDocument: '00000000000000',
);
```

#### `customAction`

`params` precisa ter a chave `mapMsiTef` com um `Map<String, String>`. Cada par vira um extra do intent do m-SiTef, sem nenhum valor padrão (informe tudo o que a operação exige).

```dart
final response = await MSitefPaymentTech.I.payment.customAction(params: {
  'mapMsiTef': {
    'modalidade': '114',
    'empresaSitef': '00000000',
    'enderecoSitef': '192.168.0.10',
    'terminalSitef': 'TE000001',
    'CNPJ_CPF': '00000000000000',
    'cnpj_automacao': '00000000000000',
  },
});
```

### `TransactionParams`

| Campo | Tipo | Obrigatório | Extra do m-SiTef | Descrição |
|-------|------|-------------|------------------|-----------|
| `modalidade` | `MSitefType` | sim | `modalidade` | Tipo da operação. |
| `value` | `String` | sim | `valor` | Valor em centavos, sem separador (`'1000'` = R$ 10,00). |
| `couponNumber` | `String` | sim | `numeroCupom` | Número do cupom/documento fiscal. |
| `dateFiscal` | `String` | sim | `data` | Data fiscal, `AAAAMMDD`. |
| `hourFiscal` | `String` | sim | `hora` | Hora fiscal, `HHMMSS`. |
| `operador` | `String` | sim | `operador` | Operador do caixa. |
| `ipSitef` | `String` | sim | `enderecoSitef` | IP/host do servidor SiTef. |
| `storeId` | `String` | sim | `empresaSitef` | Código da loja (8 dígitos). |
| `terminalId` | `String` | sim | `terminalSitef` | Código do terminal. |
| `companyDocument` | `String` | sim | `CNPJ_CPF` | CNPJ/CPF do estabelecimento. |
| `automationDocument` | `String` | sim | `cnpj_automacao` | CNPJ da software house (automação). |
| `enabledTransactions` | `String?` | não | `restricoes` | Restrições de transação (ignorado se vazio). |
| `comExternal` | `String?` | não | `comExterna` | Comunicação externa (`'1'`, `'2'`, `'3'`...). |
| `isDoubleValidation` | `String?` | não | `isDoubleValidation` | Enviado só quando `comExternal == '1'`. |
| `otp` | `String?` | não | `otp` | Enviado quando `comExternal` é `'1'`, `'2'` ou `'3'`. |
| `tokenRegistroTls` | `String?` | não | - | Repassado ao nativo, mas só é usado pelo `mSitefMenu`. |
| `params`, `paramsAdd` | `String?` | não | - | Mantidos por compatibilidade; hoje não são enviados ao m-SiTef. |

O plugin também envia fixos: `tipoPinpad=ANDROID_USB`, `timeoutColeta=30`, `acessibilidadeVisual=0` e `habilitaTrace=1`.

`TransactionParams` tem `copyWith`, `toMap`/`fromMap`, `toJson`/`fromJson`, `==` e `hashCode`. `MSitefParams` é um modelo equivalente com todos os campos opcionais.

### `MSitefType` (enum)

| Valor | `modality` |
|-------|------------|
| `credit` | `'3'` |
| `debit` | `'2'` |
| `voucher` | `'2'` |
| `pix` | `'122'` |
| `refound` | `'200'` (cancelamento) |
| `reprinter` | `'114'` (reimpressão) |
| `administration` | `'110'` (menu administrativo) |
| `general` | `'0'` (menu geral) |

`MSitefType.fromString('3')` devolve o enum da modalidade (valores desconhecidos viram `general`).

Também existe o enum `SitefModalityMSitef` (`creditCard`, `debitCard`, `pix`, `digitalWallet`, `cancellation`, `undefined`), útil para classificar a venda no seu app.

### `TefTransactionResponseMSitef`

Resposta das operações. Campos preenchidos a partir do retorno do m-SiTef:

| Campo | Extra do m-SiTef | Descrição |
|-------|------------------|-----------|
| `resultCode` (`int`) | `CODRESP` | `0` = aprovada. Se vier `0` sem `NSU_HOST`, o plugin troca para `99999` (não confirmada). |
| `errorTransaction` (`String?`) | - | Mensagem do código (`TefHelper.checkErrorTransactionTEF`); `null` quando aprovada. |
| `tefNSUSitef` | `NSU_SITEF` | NSU do SiTef. |
| `tefNSUAuthorizing` | `NSU_HOST` | NSU do autorizador. |
| `tefCodAuthorization` | `COD_AUTORIZACAO` | Código de autorização. |
| `transactionConfirmation` | `COMP_DADOS_CONF` | Dados de confirmação. |
| `tefCodPayment` | `CODTRANS` | Código/nome da transação. |
| `establishmentReceipt` | `VIA_ESTABELECIMENTO` | Via do estabelecimento. |
| `customerReceipt` | `VIA_CLIENTE` | Via do cliente. |
| `typeFields` (`String?`) | `TIPO_CAMPOS` | JSON com os campos adicionais devolvidos pelo m-SiTef. |

Os demais campos (`binCard`, `tefInstitution`, `cardNumber`, `amountCancelled` etc.) existem no modelo e ficam vazios (`''`) quando o m-SiTef não os devolve; use `typeFields` para ler dados extras. O modelo tem `copyWith`, `toMap`/`fromMap` e `toJson`/`fromJson`.

### `TefHelper`

| Método | Descrição |
|--------|-----------|
| `checkErrorTransactionTEF(int)` | `{código: mensagem}` dos códigos de retorno de transação do SiTef. |
| `checkErrorConfigTEF(int)` | `{código: mensagem}` dos códigos de configuração da CliSiTef. |
| `checkErrorGSurfTEF(int)` | `{código: mensagem}` dos códigos da GSurf. |
| `tefAlert(String)` | `true` para códigos que pedem atenção do operador (`-1`, `-5`, `96`...). |
| `onlyNumberFrom8Positions(text:)` | Só dígitos, com 8 posições (zeros à esquerda). Útil para `storeId`. |
| `priceSanitized(double)` | `10.5` vira `'10,50'`. |
| `operatorSanitize(String)` | Corta o nome do operador em 20 caracteres. |
| `onlyNumberString(text:)` | Remove tudo que não é dígito (CNPJ/CPF). |
| `roundABNT(num, decimalPlaces)` | Arredondamento ABNT. |

### Constantes

- `CHANNEL_NAME` (`'msitef_payment_tech'`) e `msitefChannel` (`MethodChannel`): canal entre o Dart e o nativo. Útil para mocks em testes.

## Exemplos por operação

Nos exemplos abaixo, `base` é um `TransactionParams` com os dados do seu estabelecimento:

```dart
final now = DateTime.now();
String two(int v) => v.toString().padLeft(2, '0');

final base = TransactionParams(
  modalidade: MSitefType.general,
  value: '0',
  couponNumber: '123',
  dateFiscal: '${now.year}${two(now.month)}${two(now.day)}',
  hourFiscal: '${two(now.hour)}${two(now.minute)}${two(now.second)}',
  operador: TefHelper.operatorSanitize('Caixa 1'),
  ipSitef: '192.168.0.10',
  storeId: TefHelper.onlyNumberFrom8Positions(text: '1'),
  terminalId: 'TE000001',
  companyDocument: '00000000000000',
  automationDocument: '00000000000000',
);
final payment = MSitefPaymentTech.I.payment;
```

### Crédito à vista e parcelado

O número de parcelas e o tipo de parcelamento são escolhidos na tela do m-SiTef.

```dart
final credito = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.credit, value: '15000'),
);
```

### Débito

```dart
final debito = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.debit, value: '2590'),
);
```

### PIX

O plugin envia `transacoesHabilitadas=7;` automaticamente.

```dart
final pix = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.pix, value: '5000'),
);
```

### Voucher

```dart
final voucher = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.voucher, value: '3200'),
);
```

### Cancelamento / estorno

O m-SiTef pede na tela os dados da transação original (data, NSU, valor).

```dart
final cancelamento = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.refound, value: '15000'),
);
if (cancelamento?.resultCode == 0) {
  // cancelamento aprovado
}
```

### Reimpressão de comprovante

```dart
final reimpressao = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.reprinter),
);
```

### Menu administrativo

```dart
final adm = await payment.mSitefTransaction(
  transactionParams: base.copyWith(modalidade: MSitefType.administration),
);
// ou payment.mSitefMenu(...), veja a referência da API
```

### Comunicação externa (GSurf)

```dart
final gsurf = await payment.mSitefTransaction(
  transactionParams: base.copyWith(
    modalidade: MSitefType.credit,
    value: '1000',
    comExternal: '1',
    isDoubleValidation: '0',
    otp: 'SEU_OTP',
  ),
);
```

### Imprimindo os comprovantes

O plugin não imprime: use `response.customerReceipt` e `response.establishmentReceipt` com a impressora do seu aparelho.

## Tratamento de erros

Os métodos `mSitefTransaction`, `mSitefMenu` e `customAction` **não lançam exceção**: qualquer falha antes de uma resposta do m-SiTef vira `null`. Quando há resposta, confira `resultCode` e `errorTransaction`.

Códigos de `PlatformException` do nativo (convertidos em `null` pelo Dart):

| Código | Significado |
|--------|-------------|
| `NO_ACTIVITY` | O plugin não está ligado a uma `Activity` (por exemplo, chamado antes do app abrir a tela). |
| `TRANSACTION_CANCELLED` | O m-SiTef fechou sem devolver dados (voltar/cancelar sem retorno). |
| `ERROR` | Falha ao abrir o m-SiTef: app não instalado (`ActivityNotFoundException`), parâmetro obrigatório ausente ou `customAction` sem `mapMsiTef`. |
| `notImplemented` | Método desconhecido no canal. |

Códigos de `resultCode` mais comuns (lista completa em `TefHelper.checkErrorTransactionTEF`):

| `resultCode` | Significado |
|--------------|-------------|
| `0` | Aprovada. |
| `99999` | Retorno `0` sem NSU do autorizador (trate como não aprovada). |
| `-2` | Operação cancelada pelo operador. |
| `-5` | Sem comunicação com o SiTef. |
| `-6` | Operação cancelada pelo usuário no pinpad. |
| `-40` | Transação negada pelo servidor SiTef. |
| `51` | Saldo insuficiente. |
| `55` | Senha incorreta. |

```dart
final r = await payment.mSitefTransaction(transactionParams: params);
if (r == null) {
  // m-SiTef não abriu ou não devolveu dados
} else if (r.resultCode == 0) {
  // aprovada: guarde r.tefNSUSitef, r.tefCodAuthorization e os comprovantes
} else {
  final alerta = TefHelper.tefAlert(r.resultCode.toString());
  print('Negada (${r.resultCode}): ${r.errorTransaction} alerta=$alerta');
}
```

## App de exemplo

O app em `example/` tem uma tela com valor e botões de débito, crédito, PIX e voucher.

```bash
cd example
flutter pub get
flutter run            # num aparelho Android com o m-SiTef instalado
flutter build apk --debug
```

Edite `example/lib/payment/payment_page.dart` para colocar o IP do SiTef, a loja, o terminal e os CNPJs do seu ambiente.

## Testes

Testes Dart (contrato do MethodChannel, modelos e helpers):

```bash
flutter test
```

Testes JVM do módulo nativo (`android/native`: montagem dos extras do intent e respostas do plugin). Precisam de JDK 17 e do `flutter.jar` em cache (`flutter precache --android`):

```bash
export FLUTTER_SDK="$(dirname "$(dirname "$(which flutter)")")"
android/gradlew -p android/native --no-daemon -Dorg.gradle.jvmargs=-Xmx2g testDebugUnitTest
```

Depois de alterar o Java em `android/native`, gere de novo os binários de `android/libs`:

```bash
JAVA_TOOL_OPTIONS=-Xmx2g scripts/build_android_aar.sh
```

## Migração da 2.x para a 3.0.0

A 3.0.0 remove toda a licença paga. Não existe mais chave, validação online, cache de licença nem erro de licença.

| Removido | O que fazer |
|----------|-------------|
| `licenceKey` e `licenceInternalKey` de `MSitefPaymentTech.I.initPayment` e `Payment.create` | Chame sem parâmetros. |
| `MSitefPaymentTech.I.isLicenceValid` e `Payment.hasValidLicence` | Remova; o plugin está pronto logo após `initPayment()`. |
| Constante `licenceCheck` e `package:msitef_payment_tech/licence/constants/licence_constants.dart` | Remova o import. O `MethodChannel` agora é `msitefChannel`, exportado pelo import principal. |
| Códigos `LICENCE_ERROR`, `LICENCE_NOT_INITIALIZED` e `INVALID_ARGUMENT` | Não existem mais. |
| Método nativo `initPayment` | Não existe mais; `initPayment()` só cria o objeto Dart. |

Antes (2.x):

```dart
import 'package:msitef_payment_tech/msitef_payment_tech.dart';
import 'package:msitef_payment_tech/payments/model/params/msitef_type.dart';
import 'package:msitef_payment_tech/payments/model/params/transaction_params.dart';

await MSitefPaymentTech.I.initPayment(
  licenceKey: 'SUA_LICENCE_KEY',
  licenceInternalKey: 'aHR0cHM6Ly9...',
);
if (MSitefPaymentTech.I.isLicenceValid) {
  await MSitefPaymentTech.I.payment.mSitefTransaction(transactionParams: params);
}
```

Depois (3.0.0):

```dart
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

await MSitefPaymentTech.I.initPayment();
await MSitefPaymentTech.I.payment.mSitefTransaction(transactionParams: params);
```

Os imports antigos de `payments/...` continuam funcionando. Também saíram as dependências `androidx.security:security-crypto` e `okhttp`.

## Contribuindo

Issues e pull requests são bem-vindos em [github.com/JY-Labtech/msitef_payment_tech](https://github.com/JY-Labtech/msitef_payment_tech/issues).

1. Abra uma issue descrevendo o bug ou a proposta (aparelho, versão do m-SiTef e do Flutter).
2. Faça um fork, crie uma branch e inclua testes (`flutter test` e os testes nativos).
3. Se mudar o Java em `android/native`, regenere `android/libs` com `scripts/build_android_aar.sh`.
4. Rode `flutter analyze` antes de abrir o PR.

## Licença

[MIT](LICENSE).

## Créditos

Desenvolvido e mantido pela **JY Labtech**: [https://jylabtech.com.br](https://jylabtech.com.br) ([GitHub](https://github.com/JY-Labtech)). Autor original: [Jhonathan Queiroz](https://github.com/jhonathanqz).

m-SiTef e SiTef são marcas da Software Express/Fiserv.
