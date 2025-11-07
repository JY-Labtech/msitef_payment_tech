<div align="center" id="top"> 
  <img src="https://esqueciminhasenha.softwareexpress.com.br/migrador/login/img/SiTeffromFiserv_Logo_RGB.png" height=100 alt="msitef_payment_tech" />

&#xa0;

</div>

<h1 align="center">MSitef Payment Tech</h1>

<p align="center">
  <a href="#dart-about">Sobre</a> &#xa0; | &#xa0; 
  <a href="#sparkles-features">Funcionalidades</a> &#xa0; | &#xa0;
  <a href="#rocket-technologies">Tecnologias</a> &#xa0; | &#xa0;
  <a href="#white_check_mark-requirements">Requisitos</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-starting">Start</a> &#xa0; | &#xa0;
  <a href="https://github.com/JY-Labtech" target="_blank">Autor</a>
</p>

<br>

[![Github Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white&link=https://github.com/jhonathanqz)](https://github.com/jhonathanqz)

---

## 🚨 Aviso

> **Este plugin é _não oficial_** e foi desenvolvido de forma independente para facilitar a integração de pagamentos via SDK do MSitef em dispositivos Android.

### ⚠️⚠️⚠️ Esse plugin não funciona com PROXY!!! ⚠️⚠️⚠️

## :dart: Sobre

Plugin Flutter para facilitar integração com o TEF do `MSitef`.
Necessário ter instalado o `MSitef` para que funcione corretamente. Para obter o `.apk` é necessário entrar em contato com a sua distribuidora do TEF.

## 🔐 Licenciamento

Para adquirir uma licença, cadastre-se no site [Licença JY Labtech](https://licenca.jylabtech.com.br).  
Cada licença é válida por terminal. Após a compra, você receberá duas chaves:

- `licenceKey`: identificação pública da licença do terminal.
- `licenceInternalKey`: endereço seguro usado pelo módulo nativo para validação.

> ✅ **Importante:** as chaves **devem ser fornecidas em tempo de execução** ao chamar `MSitefPaymentTech.I.initPayment`. Não armazene as chaves em `local.properties` ou em arquivos de código-fonte versionados. Prefira buscar os valores de um backend seguro, variáveis de ambiente criptografadas ou serviços de configuração remota.

### Inicialização do plugin

Antes de utilizar qualquer operação de TEF, inicialize o plugin informando as chaves:

```dart
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

Future<void> bootstrapMSitef() async {
  await MSitefPaymentTech.I.initPayment(
    licenceKey: 'SUA_LICENCE_KEY',
    licenceInternalKey: 'SUA_LICENCE_INTERNAL_KEY',
  );
}
```

Execute a inicialização uma única vez (por exemplo, no `main()` ou no `initState` do seu `Widget` raiz). O módulo nativo valida, criptografa e armazena a licença com `EncryptedSharedPreferences` para reutilizações futuras no dispositivo.

## :sparkles: Funcionalidades

:heavy_check_mark: Cartão de crédito
:heavy_check_mark: Cartão de débito
:heavy_check_mark: PIX
:heavy_check_mark: Cancelamento
:heavy_check_mark: Reimpressão

## :rocket: Tecnologias

#### Para que o plugin funcione corretamente é necessário ter o aplicativo `MSitef` instalado na máquina. Este é fornecido pela própria empresa que está lhe fornecendo o TEF.

- [Flutter](https://docs.flutter.dev/)

## :white_check_mark: Requisitos

Para que o plugin funcione corretamente é necessário ter o aplicativo `MSitef` instalado na máquina.

## :checkered_flag: Starting

1. **Inicialize o plugin** com as chaves de licença:

```dart
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await MSitefPaymentTech.I.initPayment(
    licenceKey: 'SUA_LICENCE_KEY',
    licenceInternalKey: 'SUA_LICENCE_INTERNAL_KEY',
  );
  runApp(const MyApp());
}
```

2. **Dispare a transação desejada** usando o objeto `payment`:

   ```dart
   MSitefPaymentTech.I.payment.mSitefTransaction(
     transactionParams: TransactionParams(
       modalidade: MSitefType.debit,
       value: '10',
       couponNumber: DateTime.now().toIso8601String(),
       dateFiscal: '2021-09-01',
       hourFiscal: '12:00:00',
       operador: 'teste',
       ipSitef: '192.168.0.165',
       storeId: '00000000',
       terminalId: '00000001',
       companyDocument: '00000000000000',
       automationDocument: '00000000000000',
     ),
   );
   ```

   - Para crédito, altere `modalidade` para `MSitefType.credit`.
   - Para PIX, utilize `MSitefType.pix`.
   - Para cancelamentos, utilize `SitefModalityMSitef.cancellation` e informe os campos obrigatórios do comprovante.

3. **Consuma o retorno** através do objeto `TefTransactionResponseMSitef`, contendo códigos, comprovantes e metadados da transação.

</br>

##### Imagem do M-Sitef

  <img src="https://dev.softwareexpress.com.br/assets/images/image11-4b9d553a8b1775258c997ad9930a6726.png" height=400 alt="MSitef" />

</br>

</br>

<table>
  <tr>
    <td align="center"><a href="https://github.com/JY-Labtech"><img src="https://avatars.githubusercontent.com/u/185066406?s=200&v=4" width="100px;" alt=""/><br /><sub><b>JY Labtech</b></sub></a><br /> <a href="https://github.com/JY-Labtech" title="Autor"></a></td>
  </tr>

</table>

Feito por <a href="https://github.com/jhonathanqz" target="_blank">Jhonathan Queiroz</a>

&#xa0;

<a href="#top">Back to top</a>
