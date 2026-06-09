package com.hornetimports.cotizador;

import com.hornetimports.cotizador.dto.CotizarRequest;
import com.hornetimports.cotizador.dto.CotizarResponse;
import com.hornetimports.tipocambio.TipoCambioService;
import com.hornetimports.user.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizadorEngine       cotizadorEngine;
    private final TipoCambioService     tipoCambioService;
    private final CotizacionRepository  cotizacionRepository;
    private final RateLimiter           rateLimiter;

    @Transactional
    public CotizarResponse cotizar(CotizarRequest request, String ip, Profile user) {
        if (!rateLimiter.isAllowed(ip)) {
            return new CotizarResponse(false, null, null, "rate_limit");
        }

        double tipoCambio = tipoCambioService.obtenerRate();
        CotizarResponse resultado = cotizadorEngine.calcular(request, tipoCambio);

        if (!resultado.ok()) {
            return resultado;
        }

        Cotizacion c = new Cotizacion();
        c.setUserId(user != null ? user.getId() : null);
        c.setProductoUrl(request.productoUrl());
        c.setNombreProducto(request.nombreProducto());
        c.setPrecioUsd(request.precioUsd());
        c.setPesoKg(request.pesoKg());
        c.setCategoria(request.categoriaId());
        c.setCostoTotalArs(BigDecimal.valueOf(resultado.desglose().totalArs));
        c.setDesglose(resultado.desglose());
        c.setTipoServicio(request.tipoServicio());
        c.setEstado(EstadoCotizacion.pendiente);
        Cotizacion saved = cotizacionRepository.save(c);

        return new CotizarResponse(true, saved.getId(), resultado.desglose(), null);
    }

    public Cotizacion getCotizacion(UUID id, Profile user) {
        Cotizacion c = cotizacionRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cotización no encontrada"));

        boolean esAdmin    = user.getTipo() == com.hornetimports.user.TipoCuenta.admin;
        boolean esDueno    = c.getUserId() != null && c.getUserId().equals(user.getId());
        boolean esAnonima  = c.getUserId() == null;

        if (!esAdmin && !esDueno && !esAnonima) {
            throw new AccessDeniedException("Sin acceso a esta cotización");
        }
        return c;
    }

    public Page<Cotizacion> getCotizacionesUsuario(Profile user, Pageable pageable) {
        return cotizacionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    @Transactional
    public Cotizacion reclamarCotizacion(UUID cotizacionId, Profile user) {
        Cotizacion c = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cotización no encontrada"));

        if (c.getUserId() != null && !c.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("Esta cotización pertenece a otro usuario");
        }
        c.setUserId(user.getId());
        return cotizacionRepository.save(c);
    }
}