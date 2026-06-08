package com.hornetimports.cotizador;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CategoriaTest {

    @Test @DisplayName("getById con id existente retorna la categoría correcta")
    void get_by_id_existente() {
        assertThat(Categoria.getById("autopartes")).isEqualTo(Categoria.autopartes);
        assertThat(Categoria.getById("libros")).isEqualTo(Categoria.libros);
        assertThat(Categoria.getById("electronica")).isEqualTo(Categoria.electronica);
    }

    @Test @DisplayName("getById con id inexistente retorna null")
    void get_by_id_inexistente_retorna_null() {
        assertThat(Categoria.getById("noexiste")).isNull();
        assertThat(Categoria.getById("")).isNull();
        assertThat(Categoria.getById(null)).isNull();
    }

    @Test @DisplayName("Categorías blacklist son: electronica, alimentos, cosmeticos, otro")
    void categorias_blacklist() {
        assertThat(Categoria.electronica.blacklist).isTrue();
        assertThat(Categoria.alimentos.blacklist).isTrue();
        assertThat(Categoria.cosmeticos.blacklist).isTrue();
        assertThat(Categoria.otro.blacklist).isTrue();
    }

    @Test @DisplayName("Categorías NO blacklist incluyen autopartes, herramientas, libros")
    void categorias_no_blacklist() {
        assertThat(Categoria.autopartes.blacklist).isFalse();
        assertThat(Categoria.herramientas.blacklist).isFalse();
        assertThat(Categoria.libros.blacklist).isFalse();
        assertThat(Categoria.indumentaria.blacklist).isFalse();
    }

    @Test @DisplayName("Libros tienen arancel 0%")
    void libros_arancel_cero() {
        assertThat(Categoria.libros.tasaArancel).isEqualTo(0.0);
    }

    @Test @DisplayName("Electrónica tiene arancel 16%")
    void electronica_arancel_16() {
        assertThat(Categoria.electronica.tasaArancel).isEqualTo(0.16);
    }

    @Test @DisplayName("La mayoría de categorías tienen arancel 35%")
    void mayoria_arancel_35() {
        assertThat(Categoria.autopartes.tasaArancel).isEqualTo(0.35);
        assertThat(Categoria.hogar.tasaArancel).isEqualTo(0.35);
        assertThat(Categoria.deportes.tasaArancel).isEqualTo(0.35);
    }

    @Test @DisplayName("Existen exactamente 12 categorías")
    void doce_categorias() {
        assertThat(Categoria.values()).hasSize(12);
    }
}
