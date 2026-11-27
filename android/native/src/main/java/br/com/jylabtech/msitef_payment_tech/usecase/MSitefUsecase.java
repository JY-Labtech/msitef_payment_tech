package br.com.jylabtech.msitef_payment_tech.usecase;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;

public class MSitefUsecase {

    /** Action do m-SiTef (app da Software Express). */
    public static final String MSITEF_ACTION = "br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF";

    private JSONObject jsonCampos;

    protected static class RequestCode {
        protected static final int PERFORM_TRANSACTION_MSITEF = 1234;
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isEmpty();
    }

    /** Monta os extras do intent do menu administrativo (modalidade 110). */
    public static Map<String, String> buildMenuExtras(MethodCall call) {
        String storeMSitef = call.argument("storeId");
        String serverIP = call.argument("ipSitef");
        String companyDoc = call.argument("companyDocument");
        String comExternaMsitef = call.argument("comExternal");
        String doubleValidationMsitef = call.argument("isDoubleValidation");
        String otpMsitef = call.argument("otp");
        String transactionsMsitef = call.argument("enabledTransactions");
        String autDocument = call.argument("automationDocument");
        String terminalMSitef = call.argument("terminalId");
        String tokenRegistroTls = call.argument("tokenRegistroTls");

        Map<String, String> extras = new LinkedHashMap<>();
        extras.put("tipoPinpad", "ANDROID_USB");
        extras.put("empresaSitef", storeMSitef);
        extras.put("enderecoSitef", serverIP);
        extras.put("modalidade", "110");
        extras.put("CNPJ_CPF", companyDoc);
        extras.put("cnpj_automacao", autDocument);
        extras.put("habilitaTrace", "1");
        extras.put("chaveTrace", "FB20144EE4283A58");
        extras.put("terminalSitef", terminalMSitef);

        if (isFilled(transactionsMsitef)) {
            extras.put("transacoesHabilitadas", transactionsMsitef);
        }

        if (isFilled(tokenRegistroTls)) {
            extras.put("tokenRegistroTls", tokenRegistroTls);
        }

        putExternalCommunication(extras, comExternaMsitef, doubleValidationMsitef, otpMsitef);
        return extras;
    }

    /** Monta os extras do intent de uma transação (crédito, débito, PIX, cancelamento etc.). */
    public static Map<String, String> buildTransactionExtras(MethodCall call) {
        Object modalityArg = call.argument("modalidade");
        String companySitef = call.argument("storeId");
        String ipSitef = call.argument("ipSitef");
        String terminal = call.argument("terminalId");
        String operator = call.argument("operator");
        String date = call.argument("date");
        String hour = call.argument("hour");
        String couponNumber = call.argument("couponNumber");
        String value = call.argument("value");
        String companyDocument = call.argument("companyDocument");
        String externalComunication = call.argument("comExternal");
        String isDoubleValidarionResponse = call.argument("isDoubleValidation");
        String otpGSurf = call.argument("otp");
        String enabledTransaction = call.argument("enabledTransactions");
        String automationDocumentResponse = call.argument("automationDocument");

        String modalityMSitef = modalityArg == null ? "0" : String.valueOf(modalityArg);

        Map<String, String> extras = new LinkedHashMap<>();
        extras.put("tipoPinpad", "ANDROID_USB");
        extras.put("empresaSitef", companySitef);
        extras.put("enderecoSitef", ipSitef);
        extras.put("terminalSitef", terminal);
        extras.put("operador", operator);
        extras.put("data", date);
        extras.put("hora", hour);
        extras.put("numeroCupom", couponNumber);
        extras.put("modalidade", modalityMSitef);
        extras.put("valor", value);
        extras.put("CNPJ_CPF", companyDocument);
        extras.put("timeoutColeta", "30");
        extras.put("acessibilidadeVisual", "0");
        extras.put("habilitaTrace", "1");
        extras.put("chaveTrace", "FB20144EE4283A58");

        if (modalityMSitef.equals("122")) {
            extras.put("transacoesHabilitadas", "7;");
        }

        extras.put("cnpj_automacao", automationDocumentResponse);
        if (isFilled(enabledTransaction)) {
            extras.put("restricoes", enabledTransaction);
        }

        putExternalCommunication(extras, externalComunication, isDoubleValidarionResponse, otpGSurf);
        return extras;
    }

    /**
     * comExterna 1 (GSurf com dupla validação) envia isDoubleValidation e otp;
     * 2 e 3 enviam só otp (mesmo comportamento do switch com fall-through anterior).
     */
    private static void putExternalCommunication(Map<String, String> extras, String comExternal,
                                                 String isDoubleValidation, String otp) {
        if (!isFilled(comExternal)) {
            return;
        }
        extras.put("comExterna", comExternal);
        switch (comExternal) {
            case "1":
                extras.put("isDoubleValidation", isDoubleValidation);
                extras.put("otp", otp);
                break;
            case "2":
            case "3":
                extras.put("otp", otp);
                break;
            default:
                break;
        }
    }

    /** Extras livres de {@code customAction}: o mapa {@code mapMsiTef} enviado pelo Dart. */
    public static Map<String, String> buildActionExtras(MethodCall call) {
        Map<String, String> map = call.argument("mapMsiTef");
        if (map == null) {
            throw new IllegalArgumentException("customAction exige o parâmetro mapMsiTef (Map<String, String>).");
        }
        return new LinkedHashMap<>(map);
    }

    private static Intent newMSitefIntent(Map<String, String> extras) {
        Intent intent = new Intent(MSITEF_ACTION);
        for (Map.Entry<String, String> entry : extras.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        return intent;
    }

    public void mSitefMenu(Activity activity, MethodCall call) {
        activity.startActivityForResult(newMSitefIntent(buildMenuExtras(call)), RequestCode.PERFORM_TRANSACTION_MSITEF);
    }

    public void mSitefTransaction(Activity activity, MethodCall call) {
        activity.startActivityForResult(newMSitefIntent(buildTransactionExtras(call)), RequestCode.PERFORM_TRANSACTION_MSITEF);
    }

    public void startAction(Activity activity, MethodCall call) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : buildActionExtras(call).entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        Intent intent = new Intent(MSITEF_ACTION);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, RequestCode.PERFORM_TRANSACTION_MSITEF);
    }


    public String respSitefToJson(Intent data) throws JSONException {
        JSONObject json = new JSONObject();
        String typeFields = data.getStringExtra("TIPO_CAMPOS");
        jsonCampos = new JSONObject(typeFields);
        json.put("CODRESP", data.getStringExtra("CODRESP"));
        json.put("COMP_DADOS_CONF", data.getStringExtra("COMP_DADOS_CONF"));
        json.put("CODTRANS", data.getStringExtra("CODTRANS"));
        json.put("VLTROCO", data.getStringExtra("VLTROCO"));
        json.put("REDE_AUT", data.getStringExtra("REDE_AUT"));
        json.put("BANDEIRA", data.getStringExtra("BANDEIRA"));
        json.put("nsuSitef", data.getStringExtra("NSU_SITEF"));
        json.put("NSU_HOST", data.getStringExtra("NSU_HOST"));
        json.put("COD_AUTORIZACAO", data.getStringExtra("COD_AUTORIZACAO"));
        json.put("NUM_PARC", data.getStringExtra("NUM_PARC"));
        json.put("TIPO_PARC", data.getStringExtra("TIPO_PARC"));
        json.put("BIN_CART", jsonCampos.getString("136"));
        json.put("TESTE", jsonCampos.getString("110"));
        json.put("VIA_ESTABELECIMENTO", data.getStringExtra("VIA_ESTABELECIMENTO"));
        json.put("VIA_CLIENTE", data.getStringExtra("VIA_CLIENTE"));

        return json.toString();
    }


}
