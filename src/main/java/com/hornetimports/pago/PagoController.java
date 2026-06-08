package com.hornetimports.pago;

import com.hornetimports.pedido.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PedidoService pedidoService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String body,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {

        try {
            boolean firmaValida = mercadoPagoService.validarFirma(body, signature, requestId);
            if (!firmaValida) {
                log.warn("Webhook MP con firma inválida — procesado de todas formas");
            }

            String tipo = mercadoPagoService.extraerTipo(body);
            if (!"payment".equals(tipo)) {
                return ResponseEntity.ok().build();
            }

            Long paymentId = mercadoPagoService.extraerDataId(body);
            if (paymentId == null || paymentId == 0) {
                log.warn("Webhook MP sin data.id válido");
                return ResponseEntity.ok().build();
            }

            MercadoPagoService.PagoInfo pago = mercadoPagoService.obtenerPago(paymentId);
            if (pago == null || pago.externalReference() == null) {
                log.warn("No se pudo obtener externalReference para payment {}", paymentId);
                return ResponseEntity.ok().build();
            }

            pedidoService.actualizarEstadoPorPago(
                    pago.externalReference(), pago.status(), pago.paymentId());

        } catch (Exception e) {
            log.error("Error procesando webhook MP: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }
}
