package br.com.jylabtech.msitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import br.com.jylabtech.msitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceConfig;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceRules;
import br.com.jylabtech.msitef_payment_tech.presenter.PaymentsPresenter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MSitefPaymentTech {
    public static final String TRANSACTION = "transactionMsitef";
    public static final String MENU = "menuMSitef";
    public static final String ACTION = "action";

    private final MethodChannel channel;
    private final Context context;
    private final CheckLicenceTech licenceTech;

    private PaymentsPresenter paymentPresenter;
    private Activity activity;
    private String licenceKey;
    private String licenceInternalKey;

    public MSitefPaymentTech(Context context, MethodChannel channel, Activity activity) {
        this.context = context;
        this.channel = channel;
        this.activity = activity;
        this.licenceTech = new CheckLicenceTech(context);
    }

    public static boolean isBusinessMethod(String method) {
        return TRANSACTION.equals(method) || MENU.equals(method) || ACTION.equals(method);
    }

    public void updateActivity(@Nullable Activity activity) {
        this.activity = activity;
        this.paymentPresenter = null;
    }

    /**
     * licenceInternalKey é opcional: vazio/nulo usa a URL oficial. Valores fora do servidor
     * oficial são ignorados por {@link LicenceConfig#resolveLicenceUrl(String)}.
     */
    public void updateLicenceKeys(String licenceKey, @Nullable String licenceInternalKey) {
        this.licenceKey = licenceKey;
        this.licenceInternalKey = (licenceInternalKey == null || licenceInternalKey.trim().isEmpty())
                ? LicenceConfig.LICENCE_CHECK_URL
                : licenceInternalKey;
    }

    public boolean hasLicenceKeys() {
        return licenceKey != null && !licenceKey.trim().isEmpty();
    }

    /** Verifica somente o cache local válido (sem rede). Pode ser chamado na main thread. */
    public boolean hasValidCachedLicence() {
        if (!hasLicenceKeys()) {
            return false;
        }
        return licenceTech.validateExistsLicence(licenceKey, licenceInternalKey);
    }

    /** Valida a licença (cache válido ou servidor). Faz acesso à rede: não chame na main thread. */
    public LicenceResult validateLicence() {
        if (!hasLicenceKeys()) {
            LicenceResult result = new LicenceResult();
            result.setResult(LicenceRules.RESULT_ERROR);
            result.setErrorMessage("licenceKey não configurado. Chame initPayment antes de executar operações.");
            return result;
        }

        return licenceTech.validateLicence(licenceKey, licenceInternalKey);
    }

    /**
     * Executa a operação no m-SiTef. Deve ser chamado somente depois que a licença foi validada
     * (ver gate em MSitefPaymentTechNativePlugin).
     */
    public void handleMethodCall(MethodCall call, MethodChannel.Result result) throws Exception {
        ensurePresenter();
        if (paymentPresenter == null) {
            throw new IllegalStateException("No Activity bound to MSitefPaymentTech.");
        }

        if (TRANSACTION.equals(call.method)) {
            paymentPresenter.mSitefTransaction(call);
            return;
        }

        if (MENU.equals(call.method)) {
            paymentPresenter.mSitefMenu(call);
            return;
        }

        if (ACTION.equals(call.method)) {
            paymentPresenter.startAction(call);
            return;
        }

        result.notImplemented();
    }

    private void ensurePresenter() {
        if (paymentPresenter == null && activity != null) {
            paymentPresenter = new PaymentsPresenter(activity, context);
        }
    }
}
