package com.hornetimports.tienda;

import com.hornetimports.tienda.dto.TiendaProductoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    @GetMapping
    public ResponseEntity<Page<TiendaProductoDTO>> getProductos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) UUID subcategoriaId,
            @RequestParam(required = false) Boolean destacado,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "24") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tiendaService.getProductos(categoria, subcategoriaId, destacado, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiendaProductoDTO> getProducto(@PathVariable UUID id) {
        return ResponseEntity.ok(tiendaService.getProducto(id));
    }
}
