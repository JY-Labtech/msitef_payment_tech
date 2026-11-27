import 'package:flutter/services.dart';

/// Nome fixo do MethodChannel entre o Dart e o nativo Android.
// ignore: constant_identifier_names
const CHANNEL_NAME = 'msitef_payment_tech';

/// MethodChannel usado por padrão pelo [Payment].
const MethodChannel msitefChannel = MethodChannel(CHANNEL_NAME);
