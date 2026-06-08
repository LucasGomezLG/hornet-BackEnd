package com.hornetimports.pago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.*;

class MercadoPagoServiceTest {

    private MercadoPagoService service;

    private static final String SECRET     = "test_webhook_secret";
    private static final String REQUEST_ID = "req-abc-123";
    private static final String TS         = "1717000000";
    private static final String DATA_ID    = "987654321";
    private static final String BODY       = "{\"type\":\"payment\",\"data\":{\"id\":\"" + DATA_ID + "\"}}";

    @BeforeEach
    void setUp() throws Exception {
        service = new MercadoPagoService();
        setField(service, "webhookSecret", SECRET);
        setField(service, "accessToken", "");
        setField(service, "baseUrl", "http://localhost:8080");
        setField(service, "frontendUrl", "http://localhost:5173");
    }

    private static void setField(Object obj, String name, String value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static String hmac(String secret, String manifest) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(manifest.getBytes()));
    }

    private String buildSignature(String ts, String v1) {
        return "ts=" + ts + ",v1=" + v1;
    }

    // ── validarFirma ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Firma válida — retorna true")
    void firma_valida_retorna_true() throws Exception {
        String manifest = "id:" + DATA_ID + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
        String v1 = hmac(SECRET, manifest);
        String signature = buildSignature(TS, v1);

        assertThat(service.validarFirma(BODY, signature, REQUEST_ID)).isTrue();
    }

    @Test
    @DisplayName("Firma con v1 incorrecto — retorna false")
    void firma_incorrecta_retorna_false() {
        String signature = buildSignature(TS, "aaaa1111bbbb2222cccc3333dddd4444eeee5555ffff6666aaaa1111bbbb2222");
        assertThat(service.validarFirma(BODY, signature, REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("Signature nula — retorna false")
    void signature_nula_retorna_false() {
        assertThat(service.validarFirma(BODY, null, REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("Signature vacía — retorna false")
    void signature_vacia_retorna_false() {
        assertThat(service.validarFirma(BODY, "", REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("Secret vacío — retorna false")
    void secret_vacio_retorna_false() throws Exception {
        setField(service, "webhookSecret", "");
        String signature = buildSignature(TS, "cualquiervalor");
        assertThat(service.validarFirma(BODY, signature, REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("Timestamp diferente en firma — retorna false (manifest distinto)")
    void timestamp_diferente_retorna_false() throws Exception {
        String manifest = "id:" + DATA_ID + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
        String v1 = hmac(SECRET, manifest);
        // Firma dice ts diferente
        String signature = buildSignature("9999999999", v1);
        assertThat(service.validarFirma(BODY, signature, REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("RequestId diferente — retorna false")
    void request_id_diferente_retorna_false() throws Exception {
        String manifest = "id:" + DATA_ID + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
        String v1 = hmac(SECRET, manifest);
        String signature = buildSignature(TS, v1);
        // Se pasa un requestId distinto al que se usó para firmar
        assertThat(service.validarFirma(BODY, signature, "otro-request-id")).isFalse();
    }

    @Test
    @DisplayName("Body con data.id diferente — retorna false")
    void data_id_diferente_retorna_false() throws Exception {
        String manifest = "id:" + DATA_ID + ";request-id:" + REQUEST_ID + ";ts:" + TS + ";";
        String v1 = hmac(SECRET, manifest);
        String signature = buildSignature(TS, v1);
        // Body tiene un data.id distinto
        String otroBody = "{\"type\":\"payment\",\"data\":{\"id\":\"111111\"}}";
        assertThat(service.validarFirma(otroBody, signature, REQUEST_ID)).isFalse();
    }

    @Test
    @DisplayName("Body inválido (no es JSON) — no lanza excepción, retorna false")
    void body_invalido_no_lanza_excepcion() {
        assertThatCode(() -> service.validarFirma("not-json", "ts=1,v1=abc", REQUEST_ID))
                .doesNotThrowAnyException();
        assertThat(service.validarFirma("not-json", "ts=1,v1=abc", REQUEST_ID)).isFalse();
    }

    // ── extraerTipo ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("extraerTipo — extrae el campo type correctamente")
    void extrae_tipo_payment() {
        assertThat(service.extraerTipo("{\"type\":\"payment\",\"data\":{\"id\":\"1\"}}"))
                .isEqualTo("payment");
    }

    @Test
    @DisplayName("extraerTipo — tipo merchant_order no es payment")
    void extrae_tipo_merchant_order() {
        assertThat(service.extraerTipo("{\"type\":\"merchant_order\",\"data\":{\"id\":\"1\"}}"))
                .isEqualTo("merchant_order");
    }

    @Test
    @DisplayName("extraerTipo — JSON vacío retorna string vacío")
    void extrae_tipo_json_vacio() {
        assertThat(service.extraerTipo("{}")).isEmpty();
    }

    // ── extraerDataId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("extraerDataId — extrae el id correctamente")
    void extrae_data_id() {
        assertThat(service.extraerDataId("{\"data\":{\"id\":\"123456789\"}}"))
                .isEqualTo(123456789L);
    }

    @Test
    @DisplayName("extraerDataId — JSON sin data.id retorna 0")
    void extrae_data_id_sin_campo_retorna_0() {
        assertThat(service.extraerDataId("{}")).isEqualTo(0L);
    }
}
