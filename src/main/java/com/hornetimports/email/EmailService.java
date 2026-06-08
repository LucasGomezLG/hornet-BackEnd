package com.hornetimports.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from:noreply@hornetimports.com}")
    private String emailFrom;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ── Cotizaciones ─────────────────────────────────────────────────────────

    public void sendCotizacionAprobada(String to, String productoNombre, UUID cotizacionId) {
        String link = frontendUrl + "/solicitar/" + cotizacionId;
        sendHtml(to,
                "Tu cotización está lista — " + productoNombre,
                "<h2>¡Buenas noticias!</h2>" +
                "<p>Tu cotización para <strong>" + productoNombre + "</strong> fue aprobada.</p>" +
                "<p>Hacé clic en el botón para confirmar tu pedido:</p>" +
                "<p><a href='" + link + "' style='background:#F5B800;color:#111;padding:12px 24px;" +
                "text-decoration:none;font-weight:bold;border-radius:4px;'>Confirmar pedido →</a></p>" +
                "<p style='color:#6B6B6B;font-size:13px;'>Este enlace es válido por 7 días.</p>");
    }

    public void sendCotizacionRechazada(String to, String productoNombre, String motivo) {
        sendHtml(to,
                "Actualización sobre tu cotización — " + productoNombre,
                "<h2>Sobre tu cotización</h2>" +
                "<p>No pudimos procesar tu solicitud de <strong>" + productoNombre + "</strong>.</p>" +
                "<p><strong>Motivo:</strong> " + motivo + "</p>" +
                "<p>Podés enviar una nueva cotización en cualquier momento desde nuestra web.</p>");
    }

    // ── Pedidos ──────────────────────────────────────────────────────────────

    public void sendPedidoConfirmado(String to, String productoNombre, String pedidoId) {
        sendHtml(to,
                "Pedido confirmado — " + productoNombre,
                "<h2>Tu pedido fue confirmado</h2>" +
                "<p>Número de pedido: <strong>" + pedidoId + "</strong></p>" +
                "<p>Producto: <strong>" + productoNombre + "</strong></p>" +
                "<p>Tu pago fue recibido. Te contactamos en menos de 24 hs hábiles para coordinar.</p>");
    }

    public void sendNuevoPedido(String adminEmail, String productoNombre, String pedidoId, String metodoPago) {
        sendHtml(adminEmail,
                "[Hornet] Nuevo pedido — " + pedidoId,
                "<h2>Nuevo pedido recibido</h2>" +
                "<p>Pedido: <strong>" + pedidoId + "</strong></p>" +
                "<p>Producto: " + productoNombre + "</p>" +
                "<p>Método de pago: <strong>" + metodoPago + "</strong></p>");
    }

    // ── Instrucciones de pago manual ─────────────────────────────────────────

    public void sendInstruccionesCripto(String to, String pedidoId,
                                        String walletAddress, BigDecimal montoUsdt) {
        sendHtml(to,
                "Instrucciones de pago — Pedido " + pedidoId,
                "<h2>Instrucciones para pagar con USDT</h2>" +
                "<p>Pedido: <strong>" + pedidoId + "</strong></p>" +
                "<p>Monto: <strong>" + montoUsdt + " USDT</strong></p>" +
                "<p>Red: <strong>TRC-20 (TRON)</strong></p>" +
                "<p>Dirección: <code style='background:#f5f5f5;padding:4px 8px;'>" + walletAddress + "</code></p>" +
                "<p>Incluí el ID del pedido en el memo si tu billetera lo permite.</p>" +
                "<p style='color:#6B6B6B;'>Tu pedido se activa dentro de 1-24 hs hábiles tras validar el pago.</p>");
    }

    public void sendInstruccionesTransferencia(String to, String pedidoId,
                                               String cbu, String alias,
                                               String banco, String titular,
                                               BigDecimal montoArs) {
        sendHtml(to,
                "Instrucciones de pago — Pedido " + pedidoId,
                "<h2>Instrucciones para pagar por transferencia</h2>" +
                "<p>Pedido: <strong>" + pedidoId + "</strong></p>" +
                "<p>Monto: <strong>$" + montoArs + " ARS</strong></p>" +
                "<table style='border-collapse:collapse;'>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Banco:</strong></td><td>" + banco + "</td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Titular:</strong></td><td>" + titular + "</td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>CBU:</strong></td><td><code>" + cbu + "</code></td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Alias:</strong></td><td><code>" + alias + "</code></td></tr>" +
                "</table>" +
                "<p>Incluí el ID <strong>" + pedidoId + "</strong> en el concepto de la transferencia.</p>" +
                "<p style='color:#6B6B6B;'>Tu pedido se activa dentro de 1-24 hs hábiles tras validar el pago.</p>");
    }

    public void sendAlertaPagoManualPendiente(String adminEmail, String pedidoId, String metodoPago) {
        sendHtml(adminEmail,
                "[Hornet] Pago pendiente de validación — " + pedidoId,
                "<h2>Pago manual pendiente</h2>" +
                "<p>El pedido <strong>" + pedidoId + "</strong> está esperando validación de pago.</p>" +
                "<p>Método: <strong>" + metodoPago + "</strong></p>" +
                "<p>Ingresá al panel de admin para confirmar el pago.</p>");
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(wrapHtml(subject, htmlBody), true);
            mailSender.send(message);
            log.info("Email enviado → {} | {}", to, subject);
        } catch (Exception e) {
            log.warn("No se pudo enviar email a {} [{}]: {}", to, subject, e.getMessage());
        }
    }

    private String wrapHtml(String title, String body) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"><title>%s</title></head>
            <body style="font-family:sans-serif;max-width:600px;margin:40px auto;color:#111;">
              <div style="border-bottom:3px solid #F5B800;padding-bottom:16px;margin-bottom:24px;">
                <span style="font-size:22px;font-weight:900;letter-spacing:-1px;">HORNET</span>
                <span style="font-size:22px;font-weight:300;"> IMPORTS</span>
              </div>
              %s
              <div style="margin-top:40px;padding-top:16px;border-top:1px solid #eee;
                          font-size:12px;color:#6B6B6B;">
                Hornet Imports — Importaciones desde Asia, Europa y EEUU a Argentina.
              </div>
            </body>
            </html>
            """.formatted(title, body);
    }
}
