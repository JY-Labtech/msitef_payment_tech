package br.com.jylabtech.msitef_payment_tech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.jylabtech.msitef_payment_tech.core.MSitefPaymentTech;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MSitefPaymentTechNativePluginTest {

    /** Result que registra cada resposta, para garantir resposta única. */
    static final class RecordingResult implements MethodChannel.Result {
        final List<String> events = new ArrayList<>();
        String errorCode;

        @Override
        public void success(Object result) {
            events.add("success");
        }

        @Override
        public void error(String errorCode, String errorMessage, Object errorDetails) {
            this.errorCode = errorCode;
            events.add("error");
        }

        @Override
        public void notImplemented() {
            events.add("notImplemented");
        }
    }

    @Test
    public void metodosDeNegocio() {
        assertTrue(MSitefPaymentTech.isBusinessMethod("transactionMsitef"));
        assertTrue(MSitefPaymentTech.isBusinessMethod("menuMSitef"));
        assertTrue(MSitefPaymentTech.isBusinessMethod("action"));
        assertFalse(MSitefPaymentTech.isBusinessMethod("initPayment"));
        assertFalse(MSitefPaymentTech.isBusinessMethod(null));
    }

    @Test
    public void metodoDesconhecidoEhNotImplemented() {
        MSitefPaymentTechNativePlugin plugin = new MSitefPaymentTechNativePlugin();
        RecordingResult result = new RecordingResult();
        plugin.onMethodCall(new MethodCall("initPayment", new HashMap<String, Object>()), result);
        assertEquals(List.of("notImplemented"), result.events);
    }

    @Test
    public void operacoesSemActivityRespondemNoActivityUmaVez() {
        for (String method : new String[]{"transactionMsitef", "menuMSitef", "action"}) {
            MSitefPaymentTechNativePlugin plugin = new MSitefPaymentTechNativePlugin();
            RecordingResult result = new RecordingResult();
            plugin.onMethodCall(new MethodCall(method, new HashMap<String, Object>()), result);
            assertEquals(method, List.of("error"), result.events);
            assertEquals(method, "NO_ACTIVITY", result.errorCode);
        }
    }

    @Test
    public void handleMethodCallSemActivityFalha() {
        MSitefPaymentTech delegate = new MSitefPaymentTech(null, null, null);
        RecordingResult result = new RecordingResult();
        try {
            delegate.handleMethodCall(new MethodCall("transactionMsitef", new HashMap<String, Object>()), result);
            throw new AssertionError("esperava IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(result.events.isEmpty());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void resultadoDeActivitySemOperacaoPendenteNaoEhConsumido() {
        MSitefPaymentTechNativePlugin plugin = new MSitefPaymentTechNativePlugin();
        assertFalse(plugin.onActivityResult(1234, 0, null));
    }
}
