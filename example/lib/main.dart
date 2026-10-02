import 'package:flutter/material.dart';
import 'package:msitef_payment_tech/msitef_payment_tech.dart';

import 'payment/payment_page.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await MSitefPaymentTech.I.initPayment(
    licenceKey: 'SUA_LICENCE_KEY',
    licenceInternalKey:
        'aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=',
  );
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: DefaultTabController(
        length: 2,
        child: Scaffold(
          appBar: AppBar(
            title: const Text('MSitef Payment Tech'),
            bottom: const TabBar(tabs: [
              Tab(
                child: Text("Vender"),
              ),
              Tab(
                child: Text("Transações"),
              )
            ]),
          ),
          body: TabBarView(
            children: [
              const PaymentPage(),
              Container(),
            ],
          ),
        ),
      ),
    );
  }
}
