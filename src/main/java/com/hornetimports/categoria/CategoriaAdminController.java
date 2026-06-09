package com.hornetimports.categoria;

import com.hornetimports.categoria.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categorias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class CategoriaAdminController {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getAll() {
        return ResponseEntity.ok(
                categoriaRepository.findAllConSubcategorias().stream()
                        .map(CategoriaDTO::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CrearCategoriaRequest req) {
        if (categoriaRepository.existsById(req.id())) {
            return ResponseEntity.badRequest().build();
        }
        Categoria c = new Categoria();
        c.setId(req.id().toLowerCase().trim().replace(" ", "_"));
        c.setNombre(req.nombre().trim());
        c.setOrden(req.orden());
        c.setActivo(true);
        return ResponseEntity.ok(CategoriaDTO.from(categoriaRepository.save(c)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ActualizarCategoriaRequest req) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        c.setNombre(req.nombre().trim());
        c.setOrden(req.orden());
        c.setActivo(req.activo());
        return ResponseEntity.ok(CategoriaDTO.from(categoriaRepository.save(c)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        c.setActivo(false);
        categoriaRepository.save(c);
        return ResponseEntity.noContent().build();
    }

    // ── Subcategorías ──────────────────────────────────────────────────────────

    @PostMapping("/{categoriaId}/subcategorias")
    public ResponseEntity<SubcategoriaDTO> crearSubcategoria(
            @PathVariable String categoriaId,
            @Valid @RequestBody CrearSubcategoriaRequest req) {
        Categoria cat = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        Subcategoria s = new Subcategoria();
        s.setCategoria(cat);
        s.setNombre(req.nombre().trim());
        s.setOrden(req.orden());
        s.setActivo(true);
        return ResponseEntity.ok(SubcategoriaDTO.from(subcategoriaRepository.save(s)));
    }

    @PutMapping("/subcategorias/{id}")
    public ResponseEntity<SubcategoriaDTO> actualizarSubcategoria(
            @PathVariable UUID id,
            @Valid @RequestBody CrearSubcategoriaRequest req) {
        Subcategoria s = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));
        s.setNombre(req.nombre().trim());
        s.setOrden(req.orden());
        return ResponseEntity.ok(SubcategoriaDTO.from(subcategoriaRepository.save(s)));
    }

    @DeleteMapping("/subcategorias/{id}")
    public ResponseEntity<Void> eliminarSubcategoria(@PathVariable UUID id) {
        Subcategoria s = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoría no encontrada"));
        s.setActivo(false);
        subcategoriaRepository.save(s);
        return ResponseEntity.noContent().build();
    }
}
