package com.hornetimports.user;

import com.hornetimports.user.dto.ActualizarPerfilRequest;
import com.hornetimports.user.dto.CambiarTipoRequest;
import com.hornetimports.user.dto.PerfilResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<PerfilResponse> getPerfil(@AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(PerfilResponse.from(profile));
    }

    @PatchMapping
    public ResponseEntity<PerfilResponse> actualizarPerfil(
            @AuthenticationPrincipal Profile profile,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        Profile updated = profileService.actualizar(profile, request);
        return ResponseEntity.ok(PerfilResponse.from(updated));
    }

    @PatchMapping("/tipo")
    public ResponseEntity<PerfilResponse> cambiarTipo(
            @AuthenticationPrincipal Profile profile,
            @Valid @RequestBody CambiarTipoRequest request) {
        Profile updated = profileService.cambiarTipo(profile, request);
        return ResponseEntity.ok(PerfilResponse.from(updated));
    }
}