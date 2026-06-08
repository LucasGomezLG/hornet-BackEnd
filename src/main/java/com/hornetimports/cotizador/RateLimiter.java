package com.hornetimports.cotizador;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter en memoria: 10 req/min por IP.
 * Limitación: no se comparte entre instancias. Para escalar, migrar a
 * bucket4j_jdk17-redis (mismo grupo de artifacts que bucket4j_jdk17-core).
 */
@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS   = 60_000L;
    private static final int  MAX_ENTRIES = 10_000;

    private final Map<String, Deque<Long>> ventanas = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();

        if (ventanas.size() > MAX_ENTRIES) {
            ventanas.entrySet().removeIf(e -> {
                Deque<Long> q = e.getValue();
                return q.isEmpty() || now - q.peekFirst() > WINDOW_MS;
            });
        }

        Deque<Long> timestamps = ventanas.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}