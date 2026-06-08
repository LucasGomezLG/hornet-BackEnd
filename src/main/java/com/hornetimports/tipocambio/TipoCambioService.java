package com.hornetimports.tipocambio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoCambioService {

    private static final String DOLAR_API_URL = "https://dolarapi.com/v1/dolares/blue";
    private static final double FALLBACK_FRIO = 1320.0;
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L; // 1 hora

    private final RestTemplate restTemplate;

    private double cachedRate = 0;
    private Instant cachedAt = null;

    public TipoCambioResponse obtener() {
        if (cachedAt != null && Instant.now().toEpochMilli() - cachedAt.toEpochMilli() < CACHE_TTL_MS) {
            return new TipoCambioResponse(cachedRate, "cache");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(DOLAR_API_URL, Map.class);
            if (body != null && body.get("venta") != null) {
                double rate = ((Number) body.get("venta")).doubleValue();
                cachedRate = rate;
                cachedAt = Instant.now();
                return new TipoCambioResponse(rate, "live");
            }
        } catch (Exception e) {
            log.warn("DolarAPI no disponible: {}", e.getMessage());
        }
        if (cachedAt != null) {
            return new TipoCambioResponse(cachedRate, "stale");
        }
        return new TipoCambioResponse(FALLBACK_FRIO, "fallback");
    }

    public double obtenerRate() {
        return obtener().rate();
    }
}