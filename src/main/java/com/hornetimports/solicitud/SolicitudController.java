package com.hornetimports.solicitud;

import com.hornetimports.solicitud.dto.*;
import com.hornetimports.user.Profile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudDTO> crear(
            @Valid @RequestBody CrearSolicitudRequest req,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crear(req, user));
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudDTO>> listar(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Profile user) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(solicitudService.listarPorUsuario(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.ok(solicitudService.getById(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile user) {
        solicitudService.cancelar(id, user);
        return ResponseEntity.ok(Map.of("message", "Solicitud cancelada"));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<SolicitudDTO> agregarItem(
            @PathVariable UUID id,
            @Valid @RequestBody AgregarItemRequest req,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.agregarItem(id, req, user));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<SolicitudDTO> editarItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody AgregarItemRequest req,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.ok(solicitudService.editarItem(id, itemId, req, user));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<SolicitudDTO> eliminarItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.ok(solicitudService.eliminarItem(id, itemId, user));
    }

    @PostMapping("/{id}/items/{itemId}/confirmar")
    public ResponseEntity<ConfirmarItemResponse> confirmarItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody ConfirmarItemRequest req,
            @AuthenticationPrincipal Profile user) {
        return ResponseEntity.ok(solicitudService.confirmarItem(id, itemId, req, user));
    }
}
