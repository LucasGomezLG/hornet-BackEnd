package com.hornetimports.cotizador;

import com.hornetimports.cotizador.dto.CotizarRequest;
import com.hornetimports.cotizador.dto.CotizarResponse;
import com.hornetimports.user.Profile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @PostMapping("/api/cotizar")
    public ResponseEntity<CotizarResponse> cotizar(
            @Valid @RequestBody CotizarRequest request,
            @AuthenticationPrincipal Profile profile,
            HttpServletRequest httpRequest) {

        String ip = extraerIp(httpRequest);
        CotizarResponse resp = cotizacionService.cotizar(request, ip, profile);

        if (!resp.ok() && "rate_limit".equals(resp.razon())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resp);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/cotizar/{id}")
    public ResponseEntity<Cotizacion> getCotizacion(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(cotizacionService.getCotizacion(id, profile));
    }

    @GetMapping("/api/cotizaciones")
    public ResponseEntity<Page<Cotizacion>> getCotizaciones(
            @AuthenticationPrincipal Profile profile,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(cotizacionService.getCotizacionesUsuario(profile, pageable));
    }

    @PostMapping("/api/cotizaciones/{id}/reclamar")
    public ResponseEntity<Cotizacion> reclamar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(cotizacionService.reclamarCotizacion(id, profile));
    }

    private String extraerIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}