package br.com.jylabtech.msitef_payment_tech;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import br.com.jylabtech.msitef_payment_tech.core.MSitefPaymentTech;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class MSitefPaymentTechNativePlugin
        implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

    private static final String CHANNEL_NAME = "msitef_payment_tech";
    private static final int REQUEST_CODE_TRANSACTION = 1234;
    private static final int REQUEST_CODE_JSON_RESPONSE = 4713;

    private final Gson gson = new Gson();

    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private MSitefPaymentTech delegate;
    private MethodChannel.Result pendingResult;
    private TefResponse tefResponse;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
        context = binding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
        }
        channel = null;
        context = null;
        delegate = null;
        pendingResult = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        ensureDelegate();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
        if (delegate != null) {
            delegate.updateActivity(null);
        }
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        if (delegate != null) {
            delegate.updateActivity(activity);
        }
        ensureDelegate();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
        if (delegate != null) {
            delegate.updateActivity(null);
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (!MSitefPaymentTech.isBusinessMethod(call.method)) {
            result.notImplemented();
            return;
        }

        executeOperation(call, result);
    }

    private void executeOperation(MethodCall call, Result result) {
        if (!ensureDelegate(result)) {
            return;
        }

        boolean isAsync = isAsyncCall(call.method);
        if (isAsync) {
            pendingResult = result;
        }

        try {
            delegate.handleMethodCall(call, result);
            if (!isAsync) {
                pendingResult = null;
            }
        } catch (Exception e) {
            pendingResult = null;
            result.error("ERROR", e.getMessage(), null);
        }
    }

    private boolean ensureDelegate() {
        if (delegate == null && context != null && channel != null && activity != null) {
            delegate = new MSitefPaymentTech(context, channel, activity);
        } else if (delegate != null && activity != null) {
            delegate.updateActivity(activity);
        }

        return delegate != null;
    }

    private boolean ensureDelegate(Result result) {
        if (!ensureDelegate()) {
            result.error("NO_ACTIVITY", "Nenhuma Activity anexada. É necessário um contexto de Activity para executar o MSitef.", null);
            return false;
        }
        return true;
    }

    private boolean isAsyncCall(String method) {
        return "transactionMsitef".equals(method)
                || "menuMSitef".equals(method)
                || "action".equals(method);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (pendingResult == null) {
            return false;
        }

        if (requestCode == REQUEST_CODE_JSON_RESPONSE) {
            if (resultCode == RESULT_OK && data != null) {
                pendingResult.success(data.getStringExtra("jsonResp"));
            } else {
                pendingResult.error("TRANSACTION_CANCELLED", "Operação interrompida pelo usuário.", null);
            }
            pendingResult = null;
            return true;
        }

        if (requestCode != REQUEST_CODE_TRANSACTION) {
            return false;
        }

        if (data != null) {
            tefResponse = new TefResponse();
            populateTefResponse(data);
            pendingResult.success(gson.toJson(tefResponse));
        } else {
            pendingResult.error("TRANSACTION_CANCELLED", "Transação cancelada sem retorno do m-SiTef.", null);
        }

        pendingResult = null;
        return true;
    }

    private void populateTefResponse(Intent data) {
        tefResponse.setResultCodeMsitef(data.getStringExtra("CODRESP"));
        String resultCodeMsitef = data.getStringExtra("CODRESP");
        if (resultCodeMsitef != null) {
            try {
                tefResponse.setResultCode(Integer.parseInt(resultCodeMsitef));
            } catch (NumberFormatException ignored) {
                tefResponse.setResultCode(999999);
            }
        }
        tefResponse.setTransactionConfirmation(data.getStringExtra("COMP_DADOS_CONF"));
        tefResponse.setTefCodAuthorization(data.getStringExtra("COD_AUTORIZACAO"));
        tefResponse.setTefNSUSitef(data.getStringExtra("NSU_SITEF"));
        tefResponse.setTefNSUAuthorizing(data.getStringExtra("NSU_HOST"));
        tefResponse.setEstablishmentReceipt(data.getStringExtra("VIA_ESTABELECIMENTO"));
        tefResponse.setCustomerReceipt(data.getStringExtra("VIA_CLIENTE"));
        tefResponse.setTefCodPayment(data.getStringExtra("CODTRANS"));
        tefResponse.setTypeFields(data.getStringExtra("TIPO_CAMPOS"));
    }
}
