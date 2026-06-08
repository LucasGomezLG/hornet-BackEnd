package com.hornetimports.marketplace;

import com.hornetimports.tipocambio.TipoCambioService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrecioUpdateService {

    private final TipoCambioService tipoCambioService;
    private final EntityManager entityManager;

    @Scheduled(cron = "0 0 9 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void actualizarPreciosArs() {
        double rate = tipoCambioService.obtenerRate();
        BigDecimal tipoCambio = BigDecimal.valueOf(rate);

        int updated = entityManager.createQuery("""
                UPDATE Listing l
                SET l.precioArs = l.precioUsd * :tipoCambio
                WHERE l.precioUsd IS NOT NULL AND l.activo = true
                """)
                .setParameter("tipoCambio", tipoCambio)
                .executeUpdate();

        log.info("Precios ARS actualizados: {} listings al tipo de cambio {}", updated, rate);
    }
}
