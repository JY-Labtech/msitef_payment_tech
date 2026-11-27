package br.com.jylabtech.msitef_payment_tech.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;

public class MSitefUsecaseTest {

    private static Map<String, Object> transactionArgs(int modalidade) {
        Map<String, Object> args = new HashMap<>();
        args.put("modalidade", modalidade);
        args.put("value", "1000");
        args.put("couponNumber", "123");
        args.put("date", "20261127");
        args.put("hour", "120000");
        args.put("operator", "op");
        args.put("ipSitef", "192.168.0.10");
        args.put("storeId", "00000000");
        args.put("terminalId", "TE000001");
        args.put("companyDocument", "11111111111111");
        args.put("automationDocument", "22222222222222");
        args.put("comExternal", "");
        args.put("isDoubleValidation", "");
        args.put("otp", "");
        args.put("enabledTransactions", "");
        args.put("tokenRegistroTls", "");
        return args;
    }

    private static Map<String, Object> menuArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("ipSitef", "192.168.0.10");
        args.put("storeId", "00000000");
        args.put("terminalId", "TE000001");
        args.put("companyDocument", "11111111111111");
        args.put("automationDocument", "22222222222222");
        args.put("comExternal", "");
        args.put("isDoubleValidation", "");
        args.put("otp", "");
        args.put("enabledTransactions", "");
        args.put("tokenRegistroTls", "");
        return args;
    }

    @Test
    public void transacaoCreditoMapeiaOsCamposDoMSitef() {
        Map<String, String> extras = MSitefUsecase.buildTransactionExtras(
                new MethodCall("transactionMsitef", transactionArgs(3)));

        assertEquals("3", extras.get("modalidade"));
        assertEquals("1000", extras.get("valor"));
        assertEquals("00000000", extras.get("empresaSitef"));
        assertEquals("192.168.0.10", extras.get("enderecoSitef"));
        assertEquals("TE000001", extras.get("terminalSitef"));
        assertEquals("op", extras.get("operador"));
        assertEquals("20261127", extras.get("data"));
        assertEquals("120000", extras.get("hora"));
        assertEquals("123", extras.get("numeroCupom"));
        assertEquals("11111111111111", extras.get("CNPJ_CPF"));
        assertEquals("22222222222222", extras.get("cnpj_automacao"));
        assertEquals("ANDROID_USB", extras.get("tipoPinpad"));
        assertFalse(extras.containsKey("transacoesHabilitadas"));
        assertFalse(extras.containsKey("restricoes"));
        assertFalse(extras.containsKey("comExterna"));
    }

    @Test
    public void pixHabilitaSomenteATransacao7() {
        Map<String, String> extras = MSitefUsecase.buildTransactionExtras(
                new MethodCall("transactionMsitef", transactionArgs(122)));
        assertEquals("122", extras.get("modalidade"));
        assertEquals("7;", extras.get("transacoesHabilitadas"));
    }

    @Test
    public void restricoesEComExterna1EnviamDuplaValidacaoEOtp() {
        Map<String, Object> args = transactionArgs(2);
        args.put("enabledTransactions", "16;");
        args.put("comExternal", "1");
        args.put("isDoubleValidation", "0");
        args.put("otp", "OTP123");
        Map<String, String> extras = MSitefUsecase.buildTransactionExtras(new MethodCall("transactionMsitef", args));

        assertEquals("16;", extras.get("restricoes"));
        assertEquals("1", extras.get("comExterna"));
        assertEquals("0", extras.get("isDoubleValidation"));
        assertEquals("OTP123", extras.get("otp"));
    }

    @Test
    public void comExterna2EnviaSoOtp() {
        Map<String, Object> args = transactionArgs(3);
        args.put("comExternal", "2");
        args.put("isDoubleValidation", "1");
        args.put("otp", "OTP");
        Map<String, String> extras = MSitefUsecase.buildTransactionExtras(new MethodCall("transactionMsitef", args));

        assertEquals("OTP", extras.get("otp"));
        assertFalse(extras.containsKey("isDoubleValidation"));
    }

    @Test
    public void camposOpcionaisNulosNaoQuebramATransacao() {
        Map<String, Object> args = transactionArgs(3);
        args.remove("comExternal");
        args.remove("enabledTransactions");
        args.remove("otp");
        args.remove("isDoubleValidation");
        Map<String, String> extras = MSitefUsecase.buildTransactionExtras(new MethodCall("transactionMsitef", args));

        assertFalse(extras.containsKey("comExterna"));
        assertFalse(extras.containsKey("restricoes"));
    }

    @Test
    public void menuUsaModalidade110ETokenTls() {
        Map<String, Object> args = menuArgs();
        args.put("enabledTransactions", "7;");
        args.put("tokenRegistroTls", "TOKEN");
        Map<String, String> extras = MSitefUsecase.buildMenuExtras(new MethodCall("menuMSitef", args));

        assertEquals("110", extras.get("modalidade"));
        assertEquals("7;", extras.get("transacoesHabilitadas"));
        assertEquals("TOKEN", extras.get("tokenRegistroTls"));
        assertEquals("TE000001", extras.get("terminalSitef"));
    }

    @Test
    public void menuComTokenTlsNuloNaoEnviaToken() {
        Map<String, Object> args = menuArgs();
        args.put("tokenRegistroTls", null);
        Map<String, String> extras = MSitefUsecase.buildMenuExtras(new MethodCall("menuMSitef", args));
        assertNull(extras.get("tokenRegistroTls"));
        assertFalse(extras.containsKey("tokenRegistroTls"));
    }

    @Test
    public void actionRepassaOMapaMSiTef() {
        Map<String, String> map = new HashMap<>();
        map.put("modalidade", "114");
        map.put("empresaSitef", "00000000");
        Map<String, Object> args = new HashMap<>();
        args.put("mapMsiTef", map);
        Map<String, String> extras = MSitefUsecase.buildActionExtras(new MethodCall("action", args));
        assertEquals(map, extras);
    }

    @Test
    public void actionSemMapaMSiTefFalha() {
        assertThrows(IllegalArgumentException.class,
                () -> MSitefUsecase.buildActionExtras(new MethodCall("action", new HashMap<String, Object>())));
    }
}
