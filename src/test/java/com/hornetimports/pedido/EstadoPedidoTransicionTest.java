package com.hornetimports.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.hornetimports.pedido.EstadoPedido.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Verifica la máquina de estados del pedido (lógica idéntica a AdminService).
 * No requiere Spring context ni base de datos.
 */
class EstadoPedidoTransicionTest {

    // Copia exacta del Map en AdminService para testear aislado
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES;
    static {
        Map<EstadoPedido, Set<EstadoPedido>> m = new HashMap<>();
        // Legacy
        m.put(en_proceso,          Set.of(comprado,           cancelado));
        m.put(comprado,             Set.of(en_transito,        cancelado));
        // Nuevo completo
        m.put(esperando_sena,       Set.of(sena_confirmada,    cancelado));
        m.put(sena_confirmada,      Set.of(en_transito,        cancelado));
        // Forwarding
        m.put(confirmado_sin_pago,  Set.of(en_transito,        cancelado));
        // Logística compartida
        m.put(en_transito,          Set.of(en_aduana,          cancelado));
        m.put(en_aduana,            Set.of(esperando_saldo,    esperando_pago, entregado, cancelado));
        // Pago final completo
        m.put(esperando_saldo,      Set.of(saldo_confirmado,   cancelado));
        m.put(saldo_confirmado,     Set.of(entregado));
        // Pago final forwarding
        m.put(esperando_pago,       Set.of(pago_confirmado,    cancelado));
        m.put(pago_confirmado,      Set.of(entregado));
        // Finales
        m.put(entregado,            Set.of());
        m.put(cancelado,            Set.of());
        TRANSICIONES = Collections.unmodifiableMap(m);
    }

    private boolean esValida(EstadoPedido actual, EstadoPedido nuevo) {
        return TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo);
    }

    // ── Flujo legacy ──────────────────────────────────────────────────────────

    @Test @DisplayName("en_proceso → comprado (pago único confirmado)")
    void en_proceso_a_comprado() {
        assertThat(esValida(en_proceso, comprado)).isTrue();
    }

    @Test @DisplayName("comprado → en_transito")
    void comprado_a_en_transito() {
        assertThat(esValida(comprado, en_transito)).isTrue();
    }

    // ── Flujo completo seña/saldo ─────────────────────────────────────────────

    @Test @DisplayName("esperando_sena → sena_confirmada (admin confirma seña)")
    void esperando_sena_a_sena_confirmada() {
        assertThat(esValida(esperando_sena, sena_confirmada)).isTrue();
    }

    @Test @DisplayName("sena_confirmada → en_transito (Hornet envía)")
    void sena_confirmada_a_en_transito() {
        assertThat(esValida(sena_confirmada, en_transito)).isTrue();
    }

    @Test @DisplayName("en_aduana → esperando_saldo (llegada BsAs completo)")
    void en_aduana_a_esperando_saldo() {
        assertThat(esValida(en_aduana, esperando_saldo)).isTrue();
    }

    @Test @DisplayName("esperando_saldo → saldo_confirmado")
    void esperando_saldo_a_saldo_confirmado() {
        assertThat(esValida(esperando_saldo, saldo_confirmado)).isTrue();
    }

    @Test @DisplayName("saldo_confirmado → entregado")
    void saldo_confirmado_a_entregado() {
        assertThat(esValida(saldo_confirmado, entregado)).isTrue();
    }

    // ── Flujo forwarding ──────────────────────────────────────────────────────

    @Test @DisplayName("confirmado_sin_pago → en_transito (forwarding inicia viaje)")
    void confirmado_sin_pago_a_en_transito() {
        assertThat(esValida(confirmado_sin_pago, en_transito)).isTrue();
    }

    @Test @DisplayName("en_aduana → esperando_pago (llegada BsAs forwarding)")
    void en_aduana_a_esperando_pago() {
        assertThat(esValida(en_aduana, esperando_pago)).isTrue();
    }

    @Test @DisplayName("esperando_pago → pago_confirmado")
    void esperando_pago_a_pago_confirmado() {
        assertThat(esValida(esperando_pago, pago_confirmado)).isTrue();
    }

    @Test @DisplayName("pago_confirmado → entregado")
    void pago_confirmado_a_entregado() {
        assertThat(esValida(pago_confirmado, entregado)).isTrue();
    }

    // ── Logística compartida ──────────────────────────────────────────────────

    @Test @DisplayName("en_transito → en_aduana")
    void en_transito_a_en_aduana() {
        assertThat(esValida(en_transito, en_aduana)).isTrue();
    }

    @Test @DisplayName("en_aduana → entregado (legacy directo)")
    void en_aduana_a_entregado() {
        assertThat(esValida(en_aduana, entregado)).isTrue();
    }

    // ── Cancelación desde estados activos ─────────────────────────────────────

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class,
            names = {"en_proceso", "comprado", "esperando_sena", "sena_confirmada",
                     "confirmado_sin_pago", "en_transito", "en_aduana",
                     "esperando_saldo", "esperando_pago"})
    @DisplayName("Estados activos → cancelado (permitido)")
    void estados_activos_pueden_cancelarse(EstadoPedido estado) {
        assertThat(esValida(estado, cancelado)).isTrue();
    }

    // ── Transiciones inválidas ────────────────────────────────────────────────

    @Test @DisplayName("entregado → cualquier otro estado (bloqueado)")
    void entregado_no_puede_cambiar() {
        for (EstadoPedido otro : EstadoPedido.values()) {
            assertThat(esValida(entregado, otro)).isFalse();
        }
    }

    @Test @DisplayName("cancelado → cualquier otro estado (bloqueado)")
    void cancelado_no_puede_cambiar() {
        for (EstadoPedido otro : EstadoPedido.values()) {
            assertThat(esValida(cancelado, otro)).isFalse();
        }
    }

    @Test @DisplayName("en_proceso → en_transito (saltar comprado — inválido)")
    void en_proceso_no_puede_saltar_a_en_transito() {
        assertThat(esValida(en_proceso, en_transito)).isFalse();
    }

    @Test @DisplayName("comprado → en_aduana (saltar en_transito — inválido)")
    void comprado_no_puede_saltar_a_en_aduana() {
        assertThat(esValida(comprado, en_aduana)).isFalse();
    }

    @Test @DisplayName("en_transito → entregado (saltar en_aduana — inválido)")
    void en_transito_no_puede_saltar_a_entregado() {
        assertThat(esValida(en_transito, entregado)).isFalse();
    }

    @Test @DisplayName("No se puede retroceder: comprado → en_proceso")
    void no_puede_retroceder() {
        assertThat(esValida(comprado, en_proceso)).isFalse();
    }

    @Test @DisplayName("esperando_sena → en_transito (saltar sena_confirmada — inválido)")
    void esperando_sena_no_puede_saltar_logistica() {
        assertThat(esValida(esperando_sena, en_transito)).isFalse();
    }

    @Test @DisplayName("Todos los estados tienen entrada en el mapa de transiciones")
    void todos_los_estados_tienen_entrada() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertThat(TRANSICIONES).containsKey(estado);
        }
    }
}
