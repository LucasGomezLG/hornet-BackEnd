package com.hornetimports.tienda;

import com.hornetimports.tienda.dto.TiendaProductoDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TiendaService {

    private final TiendaRepository tiendaRepository;

    public Page<TiendaProductoDTO> getProductos(String categoria, UUID subcategoriaId, Boolean destacado, String search, Pageable pageable) {
        String cat  = (categoria != null && !categoria.isBlank()) ? categoria : null;
        String srch = (search    != null && !search.isBlank())    ? search    : null;

        if (srch != null) {
            return tiendaRepository.buscarConTexto(cat, srch, pageable).map(TiendaProductoDTO::from);
        }
        if (Boolean.TRUE.equals(destacado)) {
            return tiendaRepository.findByActivoTrueAndDestacadoTrue(pageable).map(TiendaProductoDTO::from);
        }
        if (subcategoriaId != null && cat != null) {
            return tiendaRepository.findByActivoTrueAndCategoriaAndSubcategoriaId(cat, subcategoriaId, pageable).map(TiendaProductoDTO::from);
        }
        if (subcategoriaId != null) {
            return tiendaRepository.findByActivoTrueAndSubcategoriaId(subcategoriaId, pageable).map(TiendaProductoDTO::from);
        }
        if (cat != null) {
            return tiendaRepository.findByActivoTrueAndCategoria(cat, pageable).map(TiendaProductoDTO::from);
        }
        return tiendaRepository.findByActivoTrue(pageable).map(TiendaProductoDTO::from);
    }

    public TiendaProductoDTO getProducto(UUID id) {
        TiendaProducto p = tiendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        if (!p.isActivo()) throw new EntityNotFoundException("Producto no encontrado");
        return TiendaProductoDTO.from(p);
    }
}
