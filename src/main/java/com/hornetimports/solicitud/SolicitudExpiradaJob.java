package com.hornetimports.solicitud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudExpiradaJob {

    private final SolicitudService solicitudService;

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Argentina/Buenos_Aires")
    public void expirar() {
        solicitudService.expirarSolicitudesVencidas();
    }
}
