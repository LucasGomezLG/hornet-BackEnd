package com.hornetimports.cotizador;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CotizacionExpiradaJob {

    private final CotizacionRepository cotizacionRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Argentina/Buenos_Aires")
    public void expirarCotizaciones() {
        OffsetDateTime limite = OffsetDateTime.now().minusDays(7);
        int expiradas = cotizacionRepository.expirarAntiguas(
                EstadoCotizacion.expirada, EstadoCotizacion.pendiente, limite);
        if (expiradas > 0) {
            log.info("Cotizaciones expiradas: {}", expiradas);
        }
    }
}