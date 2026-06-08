package com.hornetimports.cotizador;

import java.util.Arrays;

public enum Categoria {
    autopartes         ("autopartes",          "Autopartes",                  0.35, false),
    herramientas       ("herramientas",         "Herramientas",                0.35, false),
    indumentaria       ("indumentaria",         "Indumentaria y calzado",      0.35, false),
    hogar              ("hogar",                "Hogar y decoración",          0.35, false),
    deportes           ("deportes",             "Deportes y fitness",          0.35, false),
    juguetes           ("juguetes",             "Juguetes",                    0.35, false),
    libros             ("libros",               "Libros",                      0.00, false),
    accesorios         ("accesorios",           "Accesorios y bijouterie",     0.35, false),
    electronica        ("electronica",          "Electrónica / Tecnología",    0.16, true),
    alimentos          ("alimentos",            "Alimentos y bebidas",         0.35, true),
    cosmeticos         ("cosmeticos",           "Cosméticos y perfumería",     0.35, true),
    otro               ("otro",                 "Otro rubro",                  0.35, true);

    public final String id;
    public final String nombre;
    public final double tasaArancel;
    public final boolean blacklist;

    Categoria(String id, String nombre, double tasaArancel, boolean blacklist) {
        this.id = id;
        this.nombre = nombre;
        this.tasaArancel = tasaArancel;
        this.blacklist = blacklist;
    }

    public static Categoria getById(String id) {
        return Arrays.stream(values())
                .filter(c -> c.id.equals(id))
                .findFirst()
                .orElse(null);
    }
}