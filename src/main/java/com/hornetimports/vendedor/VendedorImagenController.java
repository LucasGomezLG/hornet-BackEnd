package com.hornetimports.vendedor;

import com.hornetimports.user.Profile;
import com.hornetimports.vendedor.dto.FirmaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendedor/imagenes")
@RequiredArgsConstructor
public class VendedorImagenController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/firma")
    public ResponseEntity<FirmaResponse> generarFirma(
            @AuthenticationPrincipal Profile user) {
        long timestamp = System.currentTimeMillis() / 1000;
        String folder = "listings/" + user.getId();
        String firma = cloudinaryService.generarFirma(timestamp, folder);
        return ResponseEntity.ok(new FirmaResponse(
                firma, timestamp, folder,
                cloudinaryService.getApiKey(),
                cloudinaryService.getCloudName()));
    }
}
