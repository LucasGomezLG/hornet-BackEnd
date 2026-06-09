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
        String safe = esc(productoNombre);
        sendHtml(to,
                "Tu cotización está lista — " + safe,
                "<h2>¡Buenas noticias!</h2>" +
                "<p>Tu cotización para <strong>" + safe + "</strong> fue aprobada.</p>" +
                "<p>Hacé clic en el botón para confirmar tu pedido:</p>" +
                "<p><a href='" + link + "' style='background:#F5B800;color:#111;padding:12px 24px;" +
                "text-decoration:none;font-weight:bold;border-radius:4px;'>Confirmar pedido →</a></p>" +
                "<p style='color:#6B6B6B;font-size:13px;'>Este enlace es válido por 7 días.</p>");
    }

    public void sendCotizacionRechazada(String to, String productoNombre, String motivo) {
        String safe = esc(productoNombre);
        sendHtml(to,
                "Actualización sobre tu cotización — " + safe,
                "<h2>Sobre tu cotización</h2>" +
                "<p>No pudimos procesar tu solicitud de <strong>" + safe + "</strong>.</p>" +
                "<p><strong>Motivo:</strong> " + esc(motivo) + "</p>" +
                "<p>Podés enviar una nueva cotización en cualquier momento desde nuestra web.</p>");
    }

    // ── Pedidos ──────────────────────────────────────────────────────────────

    public void sendPedidoConfirmado(String to, String productoNombre, String pedidoId) {
        String safe = esc(productoNombre);
        sendHtml(to,
                "Pedido confirmado — " + safe,
                "<h2>Tu pedido fue confirmado</h2>" +
                "<p>Número de pedido: <strong>" + esc(pedidoId) + "</strong></p>" +
                "<p>Producto: <strong>" + safe + "</strong></p>" +
                "<p>Tu pago fue recibido. Te contactamos en menos de 24 hs hábiles para coordinar.</p>");
    }

    public void sendNuevoPedido(String adminEmail, String productoNombre, String pedidoId, String metodoPago) {
        sendHtml(adminEmail,
                "[Hornet] Nuevo pedido — " + esc(pedidoId),
                "<h2>Nuevo pedido recibido</h2>" +
                "<p>Pedido: <strong>" + esc(pedidoId) + "</strong></p>" +
                "<p>Producto: " + esc(productoNombre) + "</p>" +
                "<p>Método de pago: <strong>" + esc(metodoPago) + "</strong></p>");
    }

    // ── Solicitudes ───────────────────────────────────────────────────────────

    public void sendSolicitudCotizada(String to, String nombre, java.util.UUID solicitudId, int cantItems) {
        String link = frontendUrl + "/cotizaciones";
        String productos = cantItems == 1 ? "1 producto" : cantItems + " productos";
        sendHtml(to,
                "Tu solicitud fue cotizada — " + productos,
                "<h2>¡Hola, " + esc(nombre) + "!</h2>" +
                "<p>Revisamos tu solicitud de " + productos + " y ya tenés los precios listos.</p>" +
                "<p><strong>Tenés 3 días para confirmar.</strong> Después de ese plazo, la solicitud expira.</p>" +
                "<p><a href='" + link + "' style='background:#F5B800;color:#111;padding:12px 24px;" +
                "text-decoration:none;font-weight:bold;border-radius:4px;'>Ver cotización →</a></p>" +
                "<p style='color:#6B6B6B;font-size:13px;'>El peso declarado es estimado. Si el peso real " +
                "al llegar a Miami difiere, el costo se ajusta antes del despacho.</p>");
    }

    public void sendSenaConfirmada(String to, String productoNombre, String pedidoId) {
        String safe = esc(productoNombre);
        sendHtml(to,
                "Seña confirmada — " + safe,
                "<h2>¡Seña recibida!</h2>" +
                "<p>Pedido: <strong>" + esc(pedidoId) + "</strong></p>" +
                "<p>Producto: <strong>" + safe + "</strong></p>" +
                "<p>Recibimos tu seña. Hornet ya está gestionando la compra. " +
                "Te avisamos cuando el producto esté en camino.</p>");
    }

    public void sendProductoLlegoABsAs(String to, String productoNombre, String pedidoId,
                                       java.math.BigDecimal montoArs) {
        String link = frontendUrl + "/pedidos";
        String safe = esc(productoNombre);
        sendHtml(to,
                "Tu producto llegó a Buenos Aires — " + safe,
                "<h2>¡Tu producto llegó a BsAs!</h2>" +
                "<p>Pedido: <strong>" + esc(pedidoId) + "</strong></p>" +
                "<p>Producto: <strong>" + safe + "</strong></p>" +
                "<p>Monto a pagar: <strong>$" + montoArs.toPlainString() + " ARS</strong></p>" +
                "<p>Ingresá a tu panel para elegir el método de pago y coordinar la entrega.</p>" +
                "<p><a href='" + link + "' style='background:#F5B800;color:#111;padding:12px 24px;" +
                "text-decoration:none;font-weight:bold;border-radius:4px;'>Ir a mis pedidos →</a></p>");
    }

    public void sendSaldoConfirmado(String to, String productoNombre, String pedidoId) {
        String safe = esc(productoNombre);
        sendHtml(to,
                "Pago confirmado — " + safe,
                "<h2>¡Pago recibido!</h2>" +
                "<p>Pedido: <strong>" + esc(pedidoId) + "</strong></p>" +
                "<p>Producto: <strong>" + safe + "</strong></p>" +
                "<p>Recibimos tu pago. Coordinaremos la entrega a la brevedad.</p>");
    }

    // ── Instrucciones de pago manual ─────────────────────────────────────────

    public void sendInstruccionesCripto(String to, String pedidoId,
                                        String walletAddress, BigDecimal montoUsdt) {
        String safeId = esc(pedidoId);
        sendHtml(to,
                "Instrucciones de pago — Pedido " + safeId,
                "<h2>Instrucciones para pagar con USDT</h2>" +
                "<p>Pedido: <strong>" + safeId + "</strong></p>" +
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
        String safeId = esc(pedidoId);
        sendHtml(to,
                "Instrucciones de pago — Pedido " + safeId,
                "<h2>Instrucciones para pagar por transferencia</h2>" +
                "<p>Pedido: <strong>" + safeId + "</strong></p>" +
                "<p>Monto: <strong>$" + montoArs.toPlainString() + " ARS</strong></p>" +
                "<table style='border-collapse:collapse;'>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Banco:</strong></td><td>" + esc(banco) + "</td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Titular:</strong></td><td>" + esc(titular) + "</td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>CBU:</strong></td><td><code>" + esc(cbu) + "</code></td></tr>" +
                "<tr><td style='padding:4px 12px 4px 0'><strong>Alias:</strong></td><td><code>" + esc(alias) + "</code></td></tr>" +
                "</table>" +
                "<p>Incluí el ID <strong>" + safeId + "</strong> en el concepto de la transferencia.</p>" +
                "<p style='color:#6B6B6B;'>Tu pedido se activa dentro de 1-24 hs hábiles tras validar el pago.</p>");
    }

    public void sendAlertaPagoManualPendiente(String adminEmail, String pedidoId, String metodoPago) {
        String safeId = esc(pedidoId);
        sendHtml(adminEmail,
                "[Hornet] Pago pendiente de validación — " + safeId,
                "<h2>Pago manual pendiente</h2>" +
                "<p>El pedido <strong>" + safeId + "</strong> está esperando validación de pago.</p>" +
                "<p>Método: <strong>" + esc(metodoPago) + "</strong></p>" +
                "<p>Ingresá al panel de admin para confirmar el pago.</p>");
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

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
