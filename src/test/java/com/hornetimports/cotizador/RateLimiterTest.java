package com.hornetimports.cotizador;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new RateLimiter();
    }

    @Test
    @DisplayName("Los primeros 10 requests de una IP son permitidos")
    void diez_requests_permitidos() {
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.isAllowed("1.2.3.4"))
                    .as("Request %d debe ser permitido", i + 1)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("El request número 11 es rechazado")
    void undecimo_request_rechazado() {
        for (int i = 0; i < 10; i++) limiter.isAllowed("1.2.3.4");
        assertThat(limiter.isAllowed("1.2.3.4")).isFalse();
    }

    @Test
    @DisplayName("El request 12 también es rechazado (no hay recuperación parcial)")
    void duodecimo_request_rechazado() {
        for (int i = 0; i < 12; i++) limiter.isAllowed("1.2.3.4");
        assertThat(limiter.isAllowed("1.2.3.4")).isFalse();
    }

    @Test
    @DisplayName("IPs distintas tienen ventanas independientes")
    void diferentes_ips_no_se_afectan_entre_si() {
        for (int i = 0; i < 10; i++) limiter.isAllowed("10.0.0.1");

        // ip1 agotada, ip2 libre
        assertThat(limiter.isAllowed("10.0.0.1")).isFalse();
        assertThat(limiter.isAllowed("10.0.0.2")).isTrue();
    }

    @Test
    @DisplayName("Exactamente 10 IPs distintas, cada una hace 10 requests — todos pasan")
    void muchas_ips_independientes() {
        for (int ip = 0; ip < 10; ip++) {
            String addr = "192.168.0." + ip;
            for (int req = 0; req < 10; req++) {
                assertThat(limiter.isAllowed(addr)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Ventana expirada — requests vuelven a ser permitidos")
    @SuppressWarnings("unchecked")
    void ventana_expirada_permite_nuevos_requests() throws Exception {
        // Llenar la ventana
        for (int i = 0; i < 10; i++) limiter.isAllowed("5.5.5.5");
        assertThat(limiter.isAllowed("5.5.5.5")).isFalse();

        // Simular que los timestamps son muy viejos (>60s) via reflexión
        Field ventanasField = RateLimiter.class.getDeclaredField("ventanas");
        ventanasField.setAccessible(true);
        Map<String, Deque<Long>> ventanas = (Map<String, Deque<Long>>) ventanasField.get(limiter);

        Deque<Long> viejo = new ArrayDeque<>();
        long pasado = System.currentTimeMillis() - 70_000L; // 70 segundos atrás
        for (int i = 0; i < 10; i++) viejo.addLast(pasado + i);
        ventanas.put("5.5.5.5", viejo);

        // Ahora debe estar permitido nuevamente
        assertThat(limiter.isAllowed("5.5.5.5")).isTrue();
    }

    @Test
    @DisplayName("IP nueva — primer request siempre permitido")
    void primer_request_siempre_permitido() {
        assertThat(limiter.isAllowed("nuevo.ip.test")).isTrue();
    }

    @Test
    @DisplayName("Límite exacto: 10 peticiones pasan, la 11 falla, igual para otra IP")
    void limite_exacto_multiple_ips() {
        String ip1 = "100.0.0.1";
        String ip2 = "100.0.0.2";

        for (int i = 0; i < 10; i++) {
            assertThat(limiter.isAllowed(ip1)).isTrue();
            assertThat(limiter.isAllowed(ip2)).isTrue();
        }
        assertThat(limiter.isAllowed(ip1)).isFalse();
        assertThat(limiter.isAllowed(ip2)).isFalse();
    }
}
