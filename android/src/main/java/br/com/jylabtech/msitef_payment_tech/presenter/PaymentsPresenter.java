package br.com.jylabtech.msitef_payment_tech.presenter;

import android.app.Activity;
import android.content.Context;



import java.util.HashMap;
import java.util.Map;
import br.com.jylabtech.msitef_payment_tech.usecase.MSitefUsecase;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodCall;

public class PaymentsPresenter {

    private MSitefUsecase mUseCase;
    private Activity mActivity;
    private Context mContext;

    public PaymentsPresenter(Activity activity, Context context) {
        this.mUseCase = new MSitefUsecase();
        this.mActivity = activity;
        this.mContext = context;
    }
    public void mSitefTransaction(MethodCall call) {
    mUseCase.mSitefTransaction(mActivity, call);
   }

   public void mSitefMenu(MethodCall call) {
        mUseCase.mSitefMenu(mActivity, call);
   }

   public void startAction(MethodCall call) {
        mUseCase.startAction(mActivity, call);
   }
}
