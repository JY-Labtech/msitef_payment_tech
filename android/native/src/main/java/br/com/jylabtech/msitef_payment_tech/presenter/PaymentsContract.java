package br.com.jylabtech.msitef_payment_tech.presenter;

//Contract class
interface PaymentsContract {
  void onError(String message);

  void onMessage(String message);

  void onFinishedResponse(String message);

  void onLoading(boolean show);

  void onChanged(String message);

  void onAuthProgress(String message);
}
