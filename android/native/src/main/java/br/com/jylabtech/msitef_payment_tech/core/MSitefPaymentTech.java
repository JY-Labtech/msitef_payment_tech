package br.com.jylabtech.msitef_payment_tech.core;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;
import br.com.jylabtech.msitef_payment_tech.presenter.PaymentsPresenter;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MSitefPaymentTech {
    public static final String TRANSACTION = "transactionMsitef";
    public static final String MENU = "menuMSitef";
    public static final String ACTION = "action";

    private final MethodChannel channel;
    private final Context context;

    private PaymentsPresenter paymentPresenter;
    private Activity activity;

    public MSitefPaymentTech(Context context, MethodChannel channel, Activity activity) {
        this.context = context;
        this.channel = channel;
        this.activity = activity;
    }

    public static boolean isBusinessMethod(String method) {
        return TRANSACTION.equals(method) || MENU.equals(method) || ACTION.equals(method);
    }

    public void updateActivity(@Nullable Activity activity) {
        this.activity = activity;
        this.paymentPresenter = null;
    }

    /**
     * Executa a operação no m-SiTef (abre o app da Software Express via intent).
     * A resposta chega por {@code onActivityResult} em MSitefPaymentTechNativePlugin.
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
