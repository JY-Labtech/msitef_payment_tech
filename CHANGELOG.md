## Não lançado

- Licença conforme o contrato do jylabtech_core (JYN-234): o cache offline vale por no máximo 72 horas (antes 7 dias) e só é usado quando o servidor não responde (sem internet, timeout, 5xx).
- A primeira operação depois de abrir o app consulta o servidor, e a licença é revalidada a cada 4 horas de uso; o atalho só-cache (`validateExistsLicence`) só libera quando nenhuma consulta online está pendente.
- Recusa do servidor (cliente bloqueado, licença vencida/cancelada/suspensa, não encontrada, aparelho desativado) apaga o cache e bloqueia na hora.
- O binário nativo em `android/libs` precisa ser regerado na publicação.

## 2.2.0

> Validação de licença mais rígida: operações que antes passavam com licença vencida, inativa ou cancelada (ou com `licenceInternalKey` apontando para outro servidor) agora são bloqueadas.

- A URL de validação agora é travada no servidor oficial da JY Labtech (`https://licence-server.invalid/pos-payments/licence/check`). `licenceInternalKey` que aponte para qualquer outro endereço (inclusive a URL antiga do Cloud Run, `http` sem TLS, hosts parecidos ou com userinfo) é ignorado.
- `licenceInternalKey` passou a ser **opcional** em `MSitefPaymentTech.I.initPayment` e `Payment.create` (padrão: constante `licenceCheck`, que agora contém a URL completa de check do core). Não quebra chamadas existentes.
- O nativo revalida a licença antes de **toda** operação (`transactionMsitef`, `menuMSitef`, `action`): cache válido sem rede ou consulta online. Licença inválida devolve `LICENCE_ERROR` e o m-SiTef não é aberto. No Dart, o erro faz `hasValidLicence` voltar a `false` e a próxima operação refaz o `initPayment`.
- Corrige a aceitação de respostas 200 com licença vencida, inativa ou cancelada (`LicenceResult` agora começa como erro). Resposta 200 sem `expiredAt` ou com JSON inválido também é erro.
- Novo campo `isCanceled` na entidade de licença.
- Cache offline só é usado se a licença estiver válida, tiver sido salva há no máximo 7 dias (e não no futuro), pertencer à mesma `licenceKey` e tiver sido salva com a URL do servidor oficial; caso contrário é apagado e a licença é revalidada online no core. **Caches gravados com o `licenceInternalKey` antigo (Cloud Run) ou com qualquer valor fora do servidor oficial deixam de valer** e exigem uma consulta online na primeira operação após a atualização. Caches gravados com o valor oficial (URL ou base64 do core) continuam valendo offline. O cache novo guarda sempre a URL efetiva oficial, não o valor enviado pelo app.
- Mensagens de erro do servidor repassadas ao app; sem mensagem, `Erro ao validar licença (HTTP <código>)`.
- Timeouts de 15 s (conexão e leitura) na consulta de licença, que agora roda em background (não bloqueia mais a main thread).
- Testes: JVM no módulo nativo (`android/native`, 60 testes) e Dart (`test/licence_test.dart`).
- Binários pré-compilados (`android/libs`) regenerados.

- Validação de licença migrada para a nova API da JY Labtech (`https://licence-server.invalid/pos-payments/licence/check`). Mesmo contrato da API anterior: as chaves de licença existentes continuam valendo e chaves novas no formato `JYL-...` também são aceitas.
- Novo valor recomendado para `licenceInternalKey`: `***REMOVIDO***`. O valor antigo agora é ignorado (a validação vai sempre para o servidor oficial), então pode ser removido.
- Constante interna `licenceCheck` atualizada para a nova base da API.
- Corrige o envio da modalidade em `mSitefTransaction`: a modalidade escolhida (`MSitefType.credit`, `debit`, `pix`, etc.) era ignorada e a transação sempre abria o menu geral (modalidade `0`) do m-SiTef.
- README: novo portal de licenças ([portal.jylabtech.com.br](https://portal.jylabtech.com.br)), plano debug de cortesia, limite por dispositivo, cache offline de 7 dias, tabela de erros de licença e troubleshooting; snippet de instalação e exemplos de transação corrigidos.
- Example atualizado com o novo `licenceInternalKey`.

## 🔥 2.1.0 – Breaking Change

⚠️ Atenção: a partir desta versão o plugin volta a exigir que as chaves de licença sejam informadas pelo código.

### O que mudou

- `MSitefPaymentTech.I.initPayment` agora exige os parâmetros obrigatórios `licenceKey` e `licenceInternalKey`.
- As chaves **não** são mais lidas de `local.properties`. Elas devem ser fornecidas em tempo de execução (ex.: backend seguro, variáveis de ambiente ou gerenciadores de segredo).
- O módulo nativo valida e armazena as chaves com `EncryptedSharedPreferences`, permitindo reuso local sem expor os valores no build.

### Impacto

- Atualize todas as chamadas para `initPayment`, fornecendo explicitamente as duas chaves.
- Remova entradas de licença do `local.properties` do aplicativo.
- Consulte o README para exemplos atualizados de inicialização e configuração.

## 1.1.0

- Corrige bug ao validar licença
- Adiciona método para limpar cache de credenciais locais

## 1.1.0

- Corrige bug ao validar licença

## 1.0.6

- Primeiro release do Plugin.
- Funções: cŕedito, débito, pix, voucher, ativar terminal.
- Adicionado função para validar licença
- Configurar classe singleton
- update check licence

## 1.0.3

- Primeiro release do Plugin.
- Funções: cŕedito, débito, pix, voucher, ativar terminal.
- Adicionado função para validar licença
- Configurar classe singleton

## 1.0.2

- Primeiro release do Plugin.
- Funções: cŕedito, débito, pix, voucher, ativar terminal.
- Adicionado função para validar licença
- Configurar classe singleton

## 1.0.1

- Primeiro release do Plugin.
- Funções: cŕedito, débito, pix, voucher, ativar terminal.
- Adicionado função para validar licença
- Configurar classe singleton

## 1.0.0

- Primeiro release do Plugin.
- Funções: cŕedito, débito, pix, voucher, ativar terminal.
- Adicionado função para validar licença
