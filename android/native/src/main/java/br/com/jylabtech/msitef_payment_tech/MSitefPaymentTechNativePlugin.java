package br.com.jylabtech.msitef_payment_tech;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import br.com.jylabtech.msitef_payment_tech.core.MSitefPaymentTech;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceConfig;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceRules;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MSitefPaymentTechNativePlugin
        implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

    private static final String CHANNEL_NAME = "msitef_payment_tech";
    private static final int REQUEST_CODE_TRANSACTION = 1234;
    private static final int REQUEST_CODE_JSON_RESPONSE = 4713;

    private final Gson gson = new Gson();
    private final ExecutorService licenceExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private MSitefPaymentTech delegate;
    private MethodChannel.Result pendingResult;
    private TefResponse tefResponse;

    private String cachedLicenceKey;
    private String cachedLicenceInternalKey;

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
        restoreLicenceOnDelegate();
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
        if ("initPayment".equals(call.method)) {
            handleInitPayment(call, result);
            return;
        }

        if (!MSitefPaymentTech.isBusinessMethod(call.method)) {
            result.notImplemented();
            return;
        }

        if (!ensureDelegate(result)) {
            return;
        }

        if (!ensureLicenceInitialised(result)) {
            return;
        }

        // Gate por operação: toda chamada de negócio revalida a licença (cache válido ou servidor)
        // antes de chamar o m-SiTef. Licença inválida devolve LICENCE_ERROR e nada é executado.
        withValidLicence(result, () -> executeOperation(call, result));
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

    private void handleInitPayment(MethodCall call, Result result) {
        if (!ensureDelegate(result)) {
            return;
        }

        String licenceKey = call.argument("licenceKey");
        String licenceInternalKey = call.argument("licenceInternalKey");

        if (licenceKey == null || licenceKey.trim().isEmpty()) {
            result.error("INVALID_ARGUMENT", "licenceKey é obrigatório", null);
            return;
        }

        // licenceInternalKey é opcional: a URL efetiva é sempre a do servidor oficial da
        // JY Labtech (LicenceConfig.resolveLicenceUrl ignora valores fora dele).
        if (licenceInternalKey == null || licenceInternalKey.trim().isEmpty()) {
            licenceInternalKey = LicenceConfig.LICENCE_CHECK_URL;
        }

        final String key = licenceKey;
        final String internalKey = licenceInternalKey;
        delegate.updateLicenceKeys(key, internalKey);
        cachedLicenceKey = null;
        cachedLicenceInternalKey = null;

        withValidLicence(result, () -> {
            cachedLicenceKey = key;
            cachedLicenceInternalKey = internalKey;
            result.success(true);
        });
    }

    /**
     * Executa {@code onValid} somente com licença válida. Usa o cache local válido sem rede;
     * caso contrário valida no servidor em background e responde na main thread.
     */
    private void withValidLicence(final Result result, final Runnable onValid) {
        final MSitefPaymentTech currentDelegate = delegate;
        if (currentDelegate == null || !currentDelegate.hasLicenceKeys()) {
            result.error("LICENCE_NOT_INITIALIZED", "Chame initPayment com licenceKey antes de executar operações.", null);
            return;
        }

        boolean cachedValid;
        try {
            cachedValid = currentDelegate.hasValidCachedLicence();
        } catch (Exception e) {
            cachedValid = false;
        }
        if (cachedValid) {
            onValid.run();
            return;
        }

        licenceExecutor.execute(() -> {
            LicenceResult licenceResult;
            try {
                licenceResult = currentDelegate.validateLicence();
            } catch (Exception e) {
                licenceResult = new LicenceResult();
                licenceResult.setResult(LicenceRules.RESULT_ERROR);
                licenceResult.setErrorMessage("Erro ao validar licença: " + e.getMessage());
            }
            final LicenceResult finalResult = licenceResult;
            mainHandler.post(() -> {
                if (finalResult != null && finalResult.getResult() == LicenceRules.RESULT_SUCCESS) {
                    onValid.run();
                } else {
                    String message = finalResult != null ? finalResult.getErrorMessage() : null;
                    result.error("LICENCE_ERROR", message != null ? message : "Licença inválida.", null);
                }
            });
        });
    }

    private boolean ensureDelegate() {
        if (delegate == null && context != null && channel != null && activity != null) {
            delegate = new MSitefPaymentTech(context, channel, activity);
            restoreLicenceOnDelegate();
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

    private void restoreLicenceOnDelegate() {
        if (delegate != null && delegate.hasLicenceKeys()) {
            return;
        }

        if (delegate != null && cachedLicenceKey != null) {
            delegate.updateLicenceKeys(cachedLicenceKey, cachedLicenceInternalKey);
        }
    }

    private boolean ensureLicenceInitialised(Result result) {
        if (delegate != null && delegate.hasLicenceKeys()) {
            return true;
        }

        if (delegate != null && cachedLicenceKey != null) {
            delegate.updateLicenceKeys(cachedLicenceKey, cachedLicenceInternalKey);
            return true;
        }

        result.error("LICENCE_NOT_INITIALIZED", "Chame initPayment com licenceKey antes de executar operações.", null);
        return false;
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

        ensureTefResponse();

        if (data != null) {
            populateTefResponse(data);
            pendingResult.success(gson.toJson(tefResponse));
        } else {
            pendingResult.error("TRANSACTION_CANCELLED", "Transação cancelada sem retorno do m-SiTef.", null);
        }

        pendingResult = null;
        return true;
    }

    private void ensureTefResponse() {
        if (tefResponse == null) {
            tefResponse = new TefResponse();
        }
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
