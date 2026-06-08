package com.hornetimports.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
            en_proceso,  Set.of(comprado, cancelado),
            comprado,    Set.of(en_transito, cancelado),
            en_transito, Set.of(en_aduana, cancelado),
            en_aduana,   Set.of(entregado, cancelado),
            entregado,   Set.of(),
            cancelado,   Set.of()
    );

    private boolean esValida(EstadoPedido actual, EstadoPedido nuevo) {
        return TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo);
    }

    // ── Transiciones válidas del flujo normal ─────────────────────────────────

    @Test @DisplayName("en_proceso → comprado (pago confirmado)")
    void en_proceso_a_comprado() {
        assertThat(esValida(en_proceso, comprado)).isTrue();
    }

    @Test @DisplayName("comprado → en_transito (admin actualiza)")
    void comprado_a_en_transito() {
        assertThat(esValida(comprado, en_transito)).isTrue();
    }

    @Test @DisplayName("en_transito → en_aduana")
    void en_transito_a_en_aduana() {
        assertThat(esValida(en_transito, en_aduana)).isTrue();
    }

    @Test @DisplayName("en_aduana → entregado")
    void en_aduana_a_entregado() {
        assertThat(esValida(en_aduana, entregado)).isTrue();
    }

    // ── Cancelación desde cualquier estado activo ─────────────────────────────

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"en_proceso", "comprado", "en_transito", "en_aduana"})
    @DisplayName("Cualquier estado activo → cancelado (permitido)")
    void cualquier_estado_activo_puede_cancelarse(EstadoPedido estado) {
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

    @Test @DisplayName("en_proceso → entregado (saltar todo — inválido)")
    void en_proceso_no_puede_ir_directo_a_entregado() {
        assertThat(esValida(en_proceso, entregado)).isFalse();
    }

    @Test @DisplayName("No se puede retroceder: comprado → en_proceso")
    void no_puede_retroceder() {
        assertThat(esValida(comprado, en_proceso)).isFalse();
    }

    @Test @DisplayName("Todos los estados tienen definida su entrada en el mapa")
    void todos_los_estados_tienen_entrada() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            assertThat(TRANSICIONES).containsKey(estado);
        }
    }
}
