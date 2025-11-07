package br.com.jylabtech.msitef_payment_tech;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Classe ponte que expõe apenas a assinatura necessária para o Flutter tooling
 * enquanto a implementação real permanece empacotada no AAR.
 */
public final class MSitefPaymentTechPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private final MSitefPaymentTechNativePlugin delegate = new MSitefPaymentTechNativePlugin();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        delegate.onAttachedToEngine(binding);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        delegate.onDetachedFromEngine(binding);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        delegate.onMethodCall(call, result);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        delegate.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        delegate.onDetachedFromActivityForConfigChanges();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        delegate.onReattachedToActivityForConfigChanges(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        delegate.onDetachedFromActivity();
    }
}

