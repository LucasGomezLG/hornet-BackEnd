package com.hornetimports.marketplace;

import com.hornetimports.marketplace.dto.CrearListingRequest;
import com.hornetimports.marketplace.dto.ListingDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/vendedor/productos")
@RequiredArgsConstructor
public class VendedorController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ResponseEntity<Page<ListingDTO>> getMisProductos(
            @AuthenticationPrincipal Profile profile,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "24") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(marketplaceService.getListingsVendedor(profile, pageable));
    }

    @PostMapping
    public ResponseEntity<ListingDTO> crear(
            @Valid @RequestBody CrearListingRequest request,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(marketplaceService.crearListing(request, profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CrearListingRequest request,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(marketplaceService.actualizarListing(id, request, profile));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ListingDTO> toggle(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(marketplaceService.toggleListing(id, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Profile profile) {
        marketplaceService.eliminarListing(id, profile);
        return ResponseEntity.noContent().build();
    }
}
