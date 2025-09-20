package br.com.jylabtech.msitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;
import br.com.jylabtech.msitef_payment_tech.BuildConfig;
import android.util.Log;


import br.com.jylabtech.msitef_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.msitef_payment_tech.licence.LicenceResult;
import br.com.jylabtech.msitef_payment_tech.presenter.PaymentsPresenter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MSitefPaymentTech {
    final MethodChannel mChannel;

    // FUNCTIONS
    PaymentsPresenter payment;

    // METHODS
    private static final String TRANSACTION = "transactionMsitef";
    private static final String MENU = "menuMSitef";

    private static final String ACTION = "action";

    
    final Context currentContext;
    private Activity mActivity;

    private CheckLicenceTech licenceTech;

    public MSitefPaymentTech(Context context, MethodChannel channel, Activity activity) {
        this.currentContext = context;
        this.mChannel = channel;
        this.mActivity = activity;
        this.licenceTech = new CheckLicenceTech(context);
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) throws Exception {
        if (this.payment == null) {
            this.payment = new PaymentsPresenter(this.mActivity, this.currentContext);
        }

        String licenceKeyLocal = BuildConfig.LICENCE_KEY;
        String licenceInternalKey = BuildConfig.LICENCE_INTERNAL_KEY;

        LicenceResult licenceResult = checkValidLicence(licenceKeyLocal, licenceInternalKey);
        if(licenceResult.getResult() != 0) {
            throw new Exception(licenceResult.getErrorMessage());
        }

        if (call.method.equals(TRANSACTION)) {
            this.payment.mSitefTransaction(call);
            return;
        }

        if (call.method.equals(MENU)) {
            this.payment.mSitefMenu(call);
            return;
        }

        if (call.method.equals(ACTION)) {
            this.payment.startAction(call);
            return;
        }


        result.notImplemented();
    }

    private LicenceResult checkValidLicence(String licenceKey, String licenceInternalKey)  {
        LicenceResult resultLicence =  licenceTech.validateLicence(licenceKey, licenceInternalKey);
        return resultLicence;
    }
}
