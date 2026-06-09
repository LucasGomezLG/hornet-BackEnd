package com.hornetimports.solicitud;

import com.hornetimports.cotizador.dto.CotizarResponse;
import com.hornetimports.solicitud.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/solicitudes")
@RequiredArgsConstructor
public class AdminSolicitudController {

    private final SolicitudService solicitudService;

    @GetMapping
    public ResponseEntity<Page<SolicitudAdminDTO>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(solicitudService.listarAdmin(estado, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudAdminDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(solicitudService.getByIdAdmin(id));
    }

    @PostMapping("/{id}/cotizar")
    public ResponseEntity<SolicitudAdminDTO> cotizar(
            @PathVariable UUID id,
            @Valid @RequestBody CotizarSolicitudRequest req) {
        return ResponseEntity.ok(solicitudService.cotizar(id, req));
    }

    @GetMapping("/{id}/items/{itemId}/sugerencia")
    public ResponseEntity<CotizarResponse> sugerencia(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestParam(defaultValue = "particular")  String tipo,
            @RequestParam(defaultValue = "completo")    String tipoServicio,
            @RequestParam double precioUsd,
            @RequestParam double pesoKg,
            @RequestParam String categoria,
            @RequestParam(defaultValue = "otro") String origen) {
        return ResponseEntity.ok(solicitudService.calcularSugerencia(
                id, itemId, tipo, tipoServicio, precioUsd, pesoKg, categoria, origen));
    }
}
