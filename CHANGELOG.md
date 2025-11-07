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
