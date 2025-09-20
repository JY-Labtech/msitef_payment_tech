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

> ⚠️ Plugin não oficial!

### ⚠️⚠️⚠️ Esse plugin não funciona com PROXY!!! ⚠️⚠️⚠️

## :dart: Sobre

Plugin Flutter para facilitar integração com o TEF do `MSitef`.
Necessário ter instalado o `MSitef` para que funcione corretamente. Para obter o `.apk` é necessário entrar em contato com a sua distribuidora do TEF.

## 🔐 Licenciamento

Para adquirir uma licença, cadastre-se no site [Licença JY Labtech](https://licenca.jylabtech.com.br).  
Cada licença é válida por terminal. Após a compra, será gerada uma chave `licenceKey` vinculada ao terminal.

##### Obervação: A partir da versão 1.0.0 não é mais necessário passar o `licenceKey` como parâmetro.

A partir da versão 2.0.0 é necessário adicionar as chaves no seu arquivo `local.properties` na raiz da pasta `android`.

```
licenceKey=<sua_licenca>
licenceInternalKey=aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBpLTU3NzQ2NDIzNTQwOC5zb3V0aGFtZXJpY2EtZWFzdDEucnVuLmFwcC9wb3MtcGF5bWVudHMvbGljZW5jZS9jaGVjay9pbnZvaWNl
```

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

Para utilizar o plugin, basta chamar alguma de suas funções abaixo:

Débito

```
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
                        ),)
```

Crédito:

```
MSitefPaymentTech.I.payment.mSitefTransaction(
                        transactionParams: TransactionParams(
                          modalidade: MSitefType.credit,
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
                        ),)
```

Pix:

```
MSitefPaymentTech.I.payment.mSitefTransaction(
                        transactionParams: TransactionParams(
                          modalidade: MSitefType.pix,
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
                        ),)
```

#### Todas as transações iram retornar suas informações na classe `TefTransactionResponseMSitef`.

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
