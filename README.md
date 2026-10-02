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

## :package: Instalação

Adicione a dependência no `pubspec.yaml` do seu app:

```yaml
dependencies:
  msitef_payment_tech: ^2.2.0
```

## 🔐 Licenciamento

O plugin só executa operações de TEF com uma licença da JY Labtech **válida e ativa**. A validação é feita sempre no servidor oficial da JY Labtech (`https://licence-server.invalid/pos-payments/licence/check`) e é repetida antes de **toda** operação (transação, menu e `customAction`), usando o cache offline enquanto ele for válido. Licença inválida, vencida, suspensa ou cancelada bloqueia a operação com o erro `LICENCE_ERROR` e o m-SiTef não é chamado.

### Onde adquirir

Adquira e gerencie suas licenças no portal **[portal.jylabtech.com.br](https://portal.jylabtech.com.br)** (o login é o mesmo e-mail/senha do portal antigo).

- O produto é único — **"Licença plugin de pagamento"** — e vale para **qualquer** plugin de pagamento da JY Labtech.
- Plano **mensal**: cobrado por dispositivo (cada aparelho que valida a licença ocupa uma vaga).
- Plano **debug**: cortesia concedida automaticamente no autocadastro, válido por 365 dias para até 2 aparelhos. Ideal para desenvolvimento e homologação.
- Aparelhos podem ser desativados pelo portal para liberar vagas.
- **As chaves de licença já existentes continuam valendo** (mesmos valores). Chaves novas no formato `JYL-...` também funcionam.

### Chaves

- `licenceKey`: a chave da sua licença, copiada do portal.
- `licenceInternalKey` (**opcional** desde a 2.2.0): endereço da API de validação. Se omitido, o plugin usa a constante `licenceCheck` (URL `https://licence-server.invalid/pos-payments/licence/check` em base64):

```
***REMOVIDO***
```

> O módulo nativo aceita o valor em base64 ou a URL em texto puro, mas **só aceita endereços `https` do servidor oficial** (`licence-server.invalid`, caminho `/pos-payments/licence/check`). Qualquer outro valor (inclusive o antigo `***REMOVIDO***...`, outro host, `http` sem TLS ou base64 inválido) é ignorado e a validação é feita no servidor oficial. Não é possível apontar a validação para outro servidor.

> ✅ **Importante:** as chaves **devem ser fornecidas em tempo de execução** ao chamar `MSitefPaymentTech.I.initPayment`. Não armazene as chaves em `local.properties` ou em arquivos de código-fonte versionados. Prefira buscar os valores de um backend seguro, variáveis de ambiente criptografadas ou serviços de configuração remota.

### Inicialização do plugin

Antes de utilizar qualquer operação de TEF, inicialize o plugin informando as chaves:

```dart
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

Future<void> bootstrapMSitef() async {
  // licenceInternalKey é opcional (padrão: servidor oficial da JY Labtech)
  await MSitefPaymentTech.I.initPayment(licenceKey: 'SUA_LICENCE_KEY');
}
```

Execute a inicialização uma única vez (por exemplo, no `main()` ou no `initState` do seu `Widget` raiz). Use `MSitefPaymentTech.I.isLicenceValid` para saber se a licença foi validada.

### Cache offline

Após uma validação bem-sucedida, o módulo nativo guarda a licença criptografada (`EncryptedSharedPreferences`) e passa a validá-la localmente, sem acessar a internet, enquanto:

- a licença continuar válida (`expiredAt` no futuro e não vencida, inativa ou cancelada);
- a última consulta online tiver no máximo **7 dias** (e não estiver no futuro, o que indicaria relógio adulterado); e
- a `licenceKey` for a mesma da licença salva.

Depois disso (ou se a `licenceKey` mudar), o cache é apagado e uma nova consulta online é obrigatória; se ela falhar, as operações ficam bloqueadas.

> ℹ️ **Atualizando da 2.1.x:** um cache gravado com o `licenceInternalKey` antigo (Cloud Run) ou com qualquer endereço fora do servidor oficial não é aproveitado: na primeira operação após a atualização o plugin revalida a licença online no servidor da JY Labtech, então o aparelho precisa estar com internet nesse momento. Caches gravados com o valor oficial continuam valendo offline. Por isso o aparelho precisa de acesso à internet pelo menos uma vez a cada 7 dias.

### Erros de licença

Quando a validação falha, `initPayment` lança uma `PlatformException` com `code: 'LICENCE_ERROR'` e `message` contendo a mensagem retornada pela API (ou `Erro ao validar licença (HTTP <código>)`, se o servidor não enviar mensagem). A licença também é conferida pelo nativo antes de cada operação: se ela deixou de ser válida (cache expirado, licença vencida/suspensa/cancelada, sem internet para revalidar), o nativo devolve `LICENCE_ERROR`, o m-SiTef não é aberto e os métodos de pagamento retornam `null` (a próxima operação tenta o `initPayment` de novo). Uma resposta 200 com licença vencida, inativa ou cancelada também é tratada como erro.

| HTTP | Situação | O que fazer |
|------|----------|-------------|
| 404 | Licença não encontrada | Confira a `licenceKey` copiada do portal. |
| 412 | Limite de aparelhos atingido ou aparelho desativado | Desative um aparelho no portal ou aumente a quantidade de dispositivos do plano; reative o aparelho se ele foi desativado. |
| 428 | Licença vencida, suspensa ou cancelada | Regularize o pagamento/renovação no portal. |
| 403 | Requisição sem token de licença | Informe uma `licenceKey` não vazia. |
| 400 | Não foi possível identificar o aparelho | O aparelho não forneceu um identificador estável (ANDROID_ID). Verifique o dispositivo/ROM. |
| 429 | Muitas requisições (rate limit) | Aguarde alguns instantes e tente novamente; evite chamar `initPayment` em loop. |

Outros códigos de erro do plugin:

- `INVALID_ARGUMENT`: `licenceKey` vazio.
- `LICENCE_NOT_INITIALIZED`: uma operação foi chamada antes de `initPayment`.

### Troubleshooting

- **"Licença não encontrada" (404):** a chave está errada ou com espaços; copie novamente do portal.
- **Limite de aparelhos (412):** cada aparelho conta como um dispositivo. Reinstalar o app ou trocar de aparelho pode ocupar uma nova vaga — desative aparelhos antigos no portal.
- **Funcionava e parou após alguns dias sem internet:** o cache offline expira após 7 dias; conecte o aparelho e chame `initPayment` novamente.
- **Erro de conexão/timeout:** verifique se o aparelho acessa `https://licence-server.invalid` (o plugin não funciona com proxy).
- **Ainda usando o `licenceInternalKey` antigo:** não é mais necessário trocar (o valor é ignorado e a validação vai para o servidor oficial), mas você pode simplesmente remover o parâmetro.

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
  await MSitefPaymentTech.I.initPayment(licenceKey: 'SUA_LICENCE_KEY');
  runApp(const MyApp());
}
```

2. **Dispare a transação desejada** usando o objeto `payment`:

   ```dart
   import 'package:msitef_payment_tech/msitef_payment_tech.dart';
   import 'package:msitef_payment_tech/payments/model/params/msitef_type.dart';
   import 'package:msitef_payment_tech/payments/model/params/transaction_params.dart';

   final response = await MSitefPaymentTech.I.payment.mSitefTransaction(
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
   - Para voucher, utilize `MSitefType.voucher`.
   - Para cancelamentos, utilize `MSitefType.refound` e informe os campos obrigatórios do comprovante.
   - Para abrir o menu geral/administrativo do m-SiTef, use `MSitefPaymentTech.I.payment.mSitefMenu(...)`; para enviar parâmetros livres, use `customAction(params: {...})`.
   - Os métodos retornam `null` se a transação falhar ou se não houver licença válida.

3. **Consuma o retorno** através do objeto `TefTransactionResponseMSitef`, contendo códigos, comprovantes e metadados da transação.

</br>

## 🧪 Testes

Testes Dart (contrato do MethodChannel e propagação de `LICENCE_ERROR`):

```bash
flutter test
```

Testes JVM do módulo nativo (URL travada no servidor oficial, regras de licença, cache offline e respostas HTTP do servidor):

```bash
export FLUTTER_SDK="$(dirname "$(dirname "$(which flutter)")")"
android/gradlew -p android/native --no-daemon testDebugUnitTest
```

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
