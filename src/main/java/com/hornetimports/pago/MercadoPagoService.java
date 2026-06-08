package com.hornetimports.pago;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String crearPreferencia(String pedidoId, String productoNombre, BigDecimal costoTotalArs) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(productoNombre)
                    .quantity(1)
                    .unitPrice(costoTotalArs)
                    .currencyId("ARS")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/pago/exitoso")
                    .pending(frontendUrl + "/pago/pendiente")
                    .failure(frontendUrl + "/pago/fallido")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .externalReference(pedidoId)
                    .notificationUrl(baseUrl + "/api/pagos/webhook")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);
            return preference.getInitPoint();
        } catch (Exception e) {
            log.error("Error creando preferencia MP para pedido {}: {}", pedidoId, e.getMessage());
            throw new RuntimeException("Error al crear preferencia de pago", e);
        }
    }

    public boolean validarFirma(String body, String signature, String requestId) {
        if (signature == null || signature.isBlank() || webhookSecret.isBlank()) return false;
        try {
            String[] parts = signature.split(",");
            String ts = Arrays.stream(parts)
                    .filter(p -> p.startsWith("ts=")).findFirst()
                    .map(p -> p.substring(3)).orElse("");
            String v1 = Arrays.stream(parts)
                    .filter(p -> p.startsWith("v1=")).findFirst()
                    .map(p -> p.substring(3)).orElse("");

            JsonNode root = objectMapper.readTree(body);
            String dataId = root.path("data").path("id").asText("");

            String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256"));
            String computed = HexFormat.of().formatHex(mac.doFinal(manifest.getBytes()));

            return computed.equals(v1);
        } catch (Exception e) {
            log.warn("Error validando firma MP: {}", e.getMessage());
            return false;
        }
    }

    public record PagoInfo(String status, String externalReference, String paymentId) {}

    public PagoInfo obtenerPago(Long paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            com.mercadopago.client.payment.PaymentClient client =
                    new com.mercadopago.client.payment.PaymentClient();
            com.mercadopago.resources.payment.Payment payment = client.get(paymentId);
            return new PagoInfo(payment.getStatus(), payment.getExternalReference(),
                    String.valueOf(paymentId));
        } catch (Exception e) {
            log.error("Error obteniendo pago MP {}: {}", paymentId, e.getMessage());
            return null;
        }
    }

    public Long extraerDataId(String body) {
        try {
            return objectMapper.readTree(body).path("data").path("id").asLong(0);
        } catch (Exception e) {
            return null;
        }
    }

    public String extraerTipo(String body) {
        try {
            return objectMapper.readTree(body).path("type").asText("");
        } catch (Exception e) {
            return "";
        }
    }
}
