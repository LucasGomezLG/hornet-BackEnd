package com.hornetimports.admin;

import com.hornetimports.admin.dto.*;
import com.hornetimports.tienda.TiendaProducto;
import com.hornetimports.user.Profile;
import com.hornetimports.vendedor.CloudinaryService;
import com.hornetimports.vendedor.dto.FirmaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService     adminService;
    private final CloudinaryService cloudinaryService;

    // ── Stats ────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ── Cotizaciones ─────────────────────────────────────────────────────────

    @GetMapping("/cotizaciones")
    public ResponseEntity<Page<CotizacionAdminDTO>> getCotizaciones(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getCotizaciones(estado, pageable));
    }

    @PostMapping("/cotizaciones/{id}/aprobar")
    public ResponseEntity<Map<String, String>> aprobar(@PathVariable UUID id) {
        adminService.aprobarCotizacion(id);
        return ResponseEntity.ok(Map.of("message", "Cotización aprobada. Email enviado."));
    }

    @PostMapping("/cotizaciones/{id}/rechazar")
    public ResponseEntity<Map<String, String>> rechazar(
            @PathVariable UUID id,
            @Valid @RequestBody RechazarCotizacionRequest req) {
        adminService.rechazarCotizacion(id, req.motivo());
        return ResponseEntity.ok(Map.of("message", "Cotización rechazada. Email enviado."));
    }

    // ── Pedidos ──────────────────────────────────────────────────────────────

    @GetMapping("/pedidos")
    public ResponseEntity<Page<PedidoAdminDTO>> getPedidos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "200") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getPedidos(estado, metodoPago, pageable));
    }

    @PatchMapping("/pedidos/{id}")
    public ResponseEntity<PedidoAdminDTO> actualizarPedido(
            @PathVariable String id,
            @Valid @RequestBody ActualizarPedidoRequest req) {
        return ResponseEntity.ok(adminService.actualizarPedido(id, req));
    }

    @PostMapping("/pedidos/{id}/confirmar-pago")
    public ResponseEntity<PedidoAdminDTO> confirmarPago(
            @PathVariable String id,
            @RequestBody ConfirmarPagoRequest req) {
        return ResponseEntity.ok(adminService.confirmarPago(id, req));
    }

    @PostMapping("/pedidos/{id}/confirmar-sena")
    public ResponseEntity<PedidoAdminDTO> confirmarSena(
            @PathVariable String id,
            @RequestBody(required = false) ConfirmarPagoRequest req) {
        return ResponseEntity.ok(adminService.confirmarSena(id, req));
    }

    @PostMapping("/pedidos/{id}/notificar-llegada")
    public ResponseEntity<PedidoAdminDTO> notificarLlegada(@PathVariable String id) {
        return ResponseEntity.ok(adminService.notificarLlegada(id));
    }

    @PostMapping("/pedidos/{id}/confirmar-saldo")
    public ResponseEntity<PedidoAdminDTO> confirmarSaldo(
            @PathVariable String id,
            @RequestBody(required = false) ConfirmarPagoRequest req) {
        return ResponseEntity.ok(adminService.confirmarSaldo(id, req));
    }

    // ── Vendedores ───────────────────────────────────────────────────────────

    @GetMapping("/vendedores")
    public ResponseEntity<List<VendedorDTO>> getVendedores() {
        return ResponseEntity.ok(adminService.getVendedores());
    }

    // ── Tienda CRUD ──────────────────────────────────────────────────────────

    @GetMapping("/tienda")
    public ResponseEntity<Page<TiendaProducto>> getTienda(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getTiendaProductos(pageable));
    }

    @PostMapping("/tienda")
    public ResponseEntity<TiendaProducto> crearTiendaProducto(
            @Valid @RequestBody CrearTiendaProductoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.crearTiendaProducto(req));
    }

    @PutMapping("/tienda/{id}")
    public ResponseEntity<TiendaProducto> actualizarTiendaProducto(
            @PathVariable UUID id,
            @Valid @RequestBody CrearTiendaProductoRequest req) {
        return ResponseEntity.ok(adminService.actualizarTiendaProducto(id, req));
    }

    @PatchMapping("/tienda/{id}/toggle")
    public ResponseEntity<TiendaProducto> toggleTiendaProducto(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.toggleTiendaProducto(id));
    }

    @DeleteMapping("/tienda/{id}")
    public ResponseEntity<Void> eliminarTiendaProducto(@PathVariable UUID id) {
        adminService.eliminarTiendaProducto(id);
        return ResponseEntity.noContent().build();
    }

    // ── Cloudinary firma (admin para tienda) ─────────────────────────────────

    @PostMapping("/imagenes/firma")
    public ResponseEntity<FirmaResponse> generarFirma(
            @RequestParam(required = false) UUID productoId,
            @AuthenticationPrincipal Profile user) {
        long timestamp = System.currentTimeMillis() / 1000;
        String folder  = productoId != null ? "hornet/tienda/" + productoId : "hornet/tienda/temp";
        String firma   = cloudinaryService.generarFirma(timestamp, folder);
        return ResponseEntity.ok(new FirmaResponse(
                firma, timestamp, folder,
                cloudinaryService.getApiKey(),
                cloudinaryService.getCloudName()));
    }
}
