package com.hornetimports.marketplace;

import com.hornetimports.marketplace.dto.CrearListingRequest;
import com.hornetimports.marketplace.dto.ListingDTO;
import com.hornetimports.tipocambio.TipoCambioService;
import com.hornetimports.user.Profile;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final ListingRepository listingRepository;
    private final TipoCambioService tipoCambioService;

    // ── Público ──────────────────────────────────────────────────────────────

    public Page<ListingDTO> getListings(String categoria, String search, Pageable pageable) {
        String cat  = (categoria != null && !categoria.isBlank()) ? categoria : null;
        String srch = (search    != null && !search.isBlank())    ? search    : null;
        return (srch != null)
                ? listingRepository.buscarConTexto(cat, srch, pageable).map(ListingDTO::from)
                : listingRepository.buscarSinTexto(cat, pageable).map(ListingDTO::from);
    }

    public ListingDTO getListing(UUID id) {
        Listing listing = listingRepository.findActivoById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing no encontrado"));
        return ListingDTO.from(listing);
    }

    // ── Vendedor ─────────────────────────────────────────────────────────────

    public Page<ListingDTO> getListingsVendedor(Profile vendedor, Pageable pageable) {
        return listingRepository.findByVendedorIdOrderByCreatedAtDesc(vendedor.getId(), pageable)
                .map(ListingDTO::from);
    }

    @Transactional
    public ListingDTO crearListing(CrearListingRequest req, Profile vendedor) {
        BigDecimal precioArs = calcularPrecioArs(req.precioUsd());

        Listing l = new Listing();
        l.setVendedor(vendedor);
        l.setNombre(req.nombre());
        l.setDescripcion(req.descripcion());
        l.setCategoria(req.categoria());
        l.setPrecioUsd(req.precioUsd());
        l.setPrecioArs(precioArs);
        l.setStock(req.stock());
        l.setImagenUrl(req.imagenUrl());
        l.setActivo(true);

        return ListingDTO.from(listingRepository.save(l));
    }

    @Transactional
    public ListingDTO actualizarListing(UUID id, CrearListingRequest req, Profile vendedor) {
        Listing l = getOwnedListing(id, vendedor);

        l.setNombre(req.nombre());
        l.setDescripcion(req.descripcion());
        l.setCategoria(req.categoria());
        l.setPrecioUsd(req.precioUsd());
        l.setPrecioArs(calcularPrecioArs(req.precioUsd()));
        l.setStock(req.stock());
        if (req.imagenUrl() != null) l.setImagenUrl(req.imagenUrl());

        return ListingDTO.from(listingRepository.save(l));
    }

    @Transactional
    public ListingDTO toggleListing(UUID id, Profile vendedor) {
        Listing l = getOwnedListing(id, vendedor);
        l.setActivo(!l.isActivo());
        return ListingDTO.from(listingRepository.save(l));
    }

    @Transactional
    public void eliminarListing(UUID id, Profile vendedor) {
        Listing l = getOwnedListing(id, vendedor);
        listingRepository.delete(l);
    }

    // ── Internos ─────────────────────────────────────────────────────────────

    private Listing getOwnedListing(UUID id, Profile vendedor) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Listing no encontrado"));
        if (!l.getVendedor().getId().equals(vendedor.getId())) {
            throw new AccessDeniedException("Este listing no te pertenece");
        }
        return l;
    }

    private BigDecimal calcularPrecioArs(BigDecimal precioUsd) {
        if (precioUsd == null) return BigDecimal.ZERO;
        double rate = tipoCambioService.obtenerRate();
        return precioUsd.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }
}
