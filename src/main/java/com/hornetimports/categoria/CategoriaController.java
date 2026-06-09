package com.hornetimports.categoria;

import com.hornetimports.categoria.dto.CategoriaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> getCategorias() {
        List<CategoriaDTO> result = categoriaRepository.findActivasConSubcategorias()
                .stream()
                .map(CategoriaDTO::from)
                .toList();
        return ResponseEntity.ok(result);
    }
}
