package com.hornetimports.cotizador;

import com.hornetimports.cotizador.dto.CotizacionDesglose;
import com.hornetimports.cotizador.dto.CotizarRequest;
import com.hornetimports.cotizador.dto.CotizarResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class CotizadorEngineTest {

    private CotizadorEngine engine;

    private static CotizarRequest req(double precio, double peso,
                                      String categoria, String tipo,
                                      String tipoServicio, String origen) {
        return new CotizarRequest("https://example.com", "Test",
                BigDecimal.valueOf(precio), BigDecimal.valueOf(peso),
                categoria, tipo, tipoServicio, origen);
    }

    @BeforeEach
    void setUp() {
        engine = new CotizadorEngine();
    }

    // ── Algoritmo completo ────────────────────────────────────────────────────

    @Test
    @DisplayName("Particular completo autopartes — verificar todos los campos del desglose")
    void particular_completo_autopartes_calcula_correctamente() {
        // precio=100, peso=1.3kg → pesoFacturable=1.5, flete=27, CIF=127
        // arancel=127*0.35=44.45, IVA=(127+44.45)*0.21=36.0045
        // estadistica=127*0.03=3.81, fee=127*0.15=19.05
        // total=127+44.45+36.0045+3.81+19.05=230.3145
        CotizarResponse r = engine.calcular(req(100, 1.3, "autopartes", "particular", "completo", "asia"), 1350);

        assertThat(r.ok()).isTrue();
        CotizacionDesglose d = r.desglose();

        assertThat(d.pesoFacturable).isEqualTo(1.5);
        assertThat(d.costoFlete).isEqualTo(27.0);
        assertThat(d.cif).isEqualTo(127.0);
        assertThat(d.arancelImportacion).isCloseTo(44.45, within(0.001));
        assertThat(d.ivaImportacion).isCloseTo(36.0045, within(0.001));
        assertThat(d.tasaEstadistica).isCloseTo(3.81, within(0.001));
        assertThat(d.feeServicio).isCloseTo(19.05, within(0.001));
        assertThat(d.feeRatio).isEqualTo(0.15);
        assertThat(d.total).isCloseTo(230.3145, within(0.01));
        assertThat(d.totalArs).isCloseTo(230.3145 * 1350, within(1.0));
        assertThat(d.incluyeProducto).isTrue();
        assertThat(d.tipoServicio).isEqualTo("completo");
        assertThat(d.tipoImportacion).isEqualTo("particular");
    }

    @Test
    @DisplayName("Peso exacto 1.0 kg — no redondea")
    void peso_exacto_no_redondea() {
        CotizarResponse r = engine.calcular(req(100, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.desglose().pesoFacturable).isEqualTo(1.0);
        assertThat(r.desglose().costoFlete).isEqualTo(18.0);
    }

    @Test
    @DisplayName("Peso 0.3 kg → redondea al medio → 0.5 kg")
    void peso_menor_a_0_5_redondea_a_0_5() {
        CotizarResponse r = engine.calcular(req(100, 0.3, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.desglose().pesoFacturable).isEqualTo(0.5);
        assertThat(r.desglose().costoFlete).isEqualTo(9.0);
    }

    @Test
    @DisplayName("Peso 1.1 kg → redondea a 1.5 kg")
    void peso_1_1_redondea_a_1_5() {
        CotizarResponse r = engine.calcular(req(100, 1.1, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.desglose().pesoFacturable).isEqualTo(1.5);
    }

    @Test
    @DisplayName("Peso exacto 2.5 kg — no redondea")
    void peso_2_5_no_redondea() {
        CotizarResponse r = engine.calcular(req(100, 2.5, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.desglose().pesoFacturable).isEqualTo(2.5);
    }

    // ── Libros (arancel 0%) ───────────────────────────────────────────────────

    @Test
    @DisplayName("Libros — arancel 0%, IVA calculado solo sobre CIF (arancel=0)")
    void libros_arancel_cero() {
        // precio=50, peso=1.0 → CIF=68, arancel=0
        // IVA = (68 + 0) * 0.21 = 14.28
        // estadistica = 68 * 0.03 = 2.04 (estadística se aplica siempre)
        CotizarResponse r = engine.calcular(req(50, 1.0, "libros", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isTrue();
        assertThat(r.desglose().arancelImportacion).isEqualTo(0.0);
        assertThat(r.desglose().ivaImportacion).isCloseTo(68 * 0.21, within(0.001));
        assertThat(r.desglose().tasaEstadistica).isCloseTo(68 * 0.03, within(0.001));
    }

    // ── Blacklist ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Electrónica — blacklist → respuesta manual")
    void electronica_blacklist_retorna_categoria_manual() {
        CotizarResponse r = engine.calcular(req(100, 1.0, "electronica", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isFalse();
        assertThat(r.razon()).isEqualTo("categoria_manual");
    }

    @Test
    @DisplayName("Alimentos — blacklist → respuesta manual")
    void alimentos_blacklist() {
        CotizarResponse r = engine.calcular(req(100, 1.0, "alimentos", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isFalse();
        assertThat(r.razon()).isEqualTo("categoria_manual");
    }

    // ── Forwarding ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Forwarding — total no incluye precio del producto")
    void forwarding_no_incluye_precio_producto() {
        // precio=100, peso=1.0 → CIF=118
        // forwarding total = flete+arancel+IVA+estadistica+fee (SIN CIF completo)
        CotizarResponse r = engine.calcular(req(100, 1.0, "autopartes", "particular", "forwarding", "asia"), 1000);
        assertThat(r.ok()).isTrue();
        assertThat(r.desglose().incluyeProducto).isFalse();
        // flete=18, arancel=118*0.35=41.3, IVA=159.3*0.21=33.453, estad=3.54, fee=118*0.08=9.44
        // total = 18+41.3+33.453+3.54+9.44 ≈ 105.733 (menos que completo que sería 118+...)
        assertThat(r.desglose().total).isLessThan(r.desglose().cif);
    }

    @Test
    @DisplayName("Forwarding fee ratio — particular=8%, mayorista=6%")
    void forwarding_fee_ratios() {
        CotizarResponse particular = engine.calcular(
                req(100, 1.0, "autopartes", "particular", "forwarding", "asia"), 1000);
        CotizarResponse mayorista = engine.calcular(
                req(200, 1.0, "autopartes", "mayorista", "forwarding", "asia"), 1000);

        assertThat(particular.desglose().feeRatio).isEqualTo(0.08);
        assertThat(mayorista.desglose().feeRatio).isEqualTo(0.06);
    }

    @Test
    @DisplayName("Completo fee ratio — particular=15%, mayorista=12%")
    void completo_fee_ratios() {
        CotizarResponse particular = engine.calcular(
                req(100, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        CotizarResponse mayorista = engine.calcular(
                req(200, 1.0, "autopartes", "mayorista", "completo", "asia"), 1000);

        assertThat(particular.desglose().feeRatio).isEqualTo(0.15);
        assertThat(mayorista.desglose().feeRatio).isEqualTo(0.12);
    }

    // ── Alertas ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Origen europa + precio > 100 → alerta Europa activa")
    void alerta_origen_europa_mayor_100() {
        CotizarResponse r = engine.calcular(req(101, 1.0, "autopartes", "particular", "completo", "europa"), 1000);
        assertThat(r.desglose().alertaOrigenEuropa).isTrue();
    }

    @Test
    @DisplayName("Origen europa + precio <= 100 → sin alerta")
    void sin_alerta_europa_menor_igual_100() {
        CotizarResponse r = engine.calcular(req(100, 1.0, "autopartes", "particular", "completo", "europa"), 1000);
        assertThat(r.desglose().alertaOrigenEuropa).isFalse();
    }

    @Test
    @DisplayName("Origen asia con precio alto — sin alerta Europa")
    void sin_alerta_si_origen_asia() {
        CotizarResponse r = engine.calcular(req(500, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.desglose().alertaOrigenEuropa).isFalse();
    }

    // ── Errores de validación ─────────────────────────────────────────────────

    @Test
    @DisplayName("Categoría inválida → errorCode categoria_invalida")
    void categoria_invalida() {
        CotizarResponse r = engine.calcular(req(100, 1.0, "noexiste", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isFalse();
        assertThat(r.razon()).isEqualTo("categoria_invalida");
    }

    @Test
    @DisplayName("Precio menor al mínimo particular completo (25 USD) → error")
    void precio_menor_minimo_particular_completo() {
        CotizarResponse r = engine.calcular(req(24, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isFalse();
        assertThat(r.razon()).contains("precio_minimo");
    }

    @Test
    @DisplayName("Precio exactamente en el mínimo — pasa la validación")
    void precio_exactamente_en_minimo_pasa() {
        CotizarResponse r = engine.calcular(req(25, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        assertThat(r.ok()).isTrue();
    }

    @Test
    @DisplayName("Precio menor al mínimo mayorista completo (200 USD) → error")
    void precio_menor_minimo_mayorista_completo() {
        CotizarResponse r = engine.calcular(req(199, 1.0, "autopartes", "mayorista", "completo", "asia"), 1000);
        assertThat(r.ok()).isFalse();
    }

    @Test
    @DisplayName("Forwarding precio mínimo 10 USD — precio=9 → error")
    void precio_menor_minimo_forwarding() {
        CotizarResponse r = engine.calcular(req(9, 1.0, "autopartes", "particular", "forwarding", "asia"), 1000);
        assertThat(r.ok()).isFalse();
    }

    @Test
    @DisplayName("Tipo de cambio afecta totalArs proporcional")
    void tipo_cambio_proporcional() {
        CotizarResponse r1 = engine.calcular(req(100, 1.0, "autopartes", "particular", "completo", "asia"), 1000);
        CotizarResponse r2 = engine.calcular(req(100, 1.0, "autopartes", "particular", "completo", "asia"), 2000);

        assertThat(r1.desglose().total).isCloseTo(r2.desglose().total, within(0.001));
        assertThat(r2.desglose().totalArs).isCloseTo(r1.desglose().totalArs * 2, within(0.1));
    }
}
