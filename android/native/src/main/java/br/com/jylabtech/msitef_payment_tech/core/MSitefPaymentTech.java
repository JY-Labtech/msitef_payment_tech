package br.com.jylabtech.msitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import br.com.jylabtech.msitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.msitef_payment_tech.presenter.PaymentsPresenter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MSitefPaymentTech {
    private static final String TRANSACTION = "transactionMsitef";
    private static final String MENU = "menuMSitef";
    private static final String ACTION = "action";

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

    public void updateActivity(@Nullable Activity activity) {
        this.activity = activity;
        this.paymentPresenter = null;
    }

    public void updateLicenceKeys(String licenceKey, String licenceInternalKey) {
        this.licenceKey = licenceKey;
        this.licenceInternalKey = licenceInternalKey;
    }

    public boolean hasLicenceKeys() {
        return licenceKey != null && !licenceKey.trim().isEmpty()
                && licenceInternalKey != null && !licenceInternalKey.trim().isEmpty();
    }

    public LicenceResult validateLicence() {
        if (!hasLicenceKeys()) {
            LicenceResult result = new LicenceResult();
            result.setResult(999999);
            result.setErrorMessage("Licence keys not configured.");
            return result;
        }

        return licenceTech.validateLicence(licenceKey, licenceInternalKey);
    }

    public void handleMethodCall(MethodCall call, MethodChannel.Result result) throws Exception {
        ensurePresenter();
        if (paymentPresenter == null) {
            throw new IllegalStateException("No Activity bound to MSitefPaymentTech.");
        }

        LicenceResult licenceResult = validateLicence();
        if (licenceResult.getResult() != 0) {
            throw new Exception(licenceResult.getErrorMessage());
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
