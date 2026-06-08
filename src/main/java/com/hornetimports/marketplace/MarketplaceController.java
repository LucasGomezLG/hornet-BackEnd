package com.hornetimports.marketplace;

import com.hornetimports.marketplace.dto.ListingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public ResponseEntity<Page<ListingDTO>> getListings(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "24") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(marketplaceService.getListings(categoria, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListing(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.getListing(id));
    }
}
