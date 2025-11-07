# msitef_payment_tech_example

Aplicativo de exemplo que demonstra como inicializar o plugin com as chaves de licença e realizar transações via m-SiTef.

## Passos principais

1. Defina as chaves obtidas no portal da JY Labtech no `main()` **e/ou** preencha os campos na primeira seção da tela de exemplo para reenviar as credenciais ao módulo nativo:

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

2. A tela `PaymentPage` permite validar a licença novamente (útil para testar chaves diferentes) e só habilita os botões de transação após a confirmação do nativo.
3. Ajuste os parâmetros (IP, loja, terminal, etc.) de acordo com o ambiente fornecido pela adquirente antes de enviar para produção.
