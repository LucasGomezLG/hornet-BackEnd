package com.hornetimports.cotizador;

import com.hornetimports.cotizador.dto.CotizacionDesglose;
import com.hornetimports.cotizador.dto.CotizarRequest;
import com.hornetimports.cotizador.dto.CotizarResponse;
import org.springframework.stereotype.Component;

@Component
public class CotizadorEngine {

    private static final double FLETE_POR_KG     = 18.0;
    private static final double IVA_IMPORTACION  = 0.21;
    private static final double TASA_ESTADISTICA = 0.03;

    private static final double FEE_PARTICULAR_COMPLETO   = 0.15;
    private static final double FEE_MAYORISTA_COMPLETO    = 0.12;
    private static final double FEE_PARTICULAR_FORWARDING = 0.08;
    private static final double FEE_MAYORISTA_FORWARDING  = 0.06;

    private static final double PRECIO_MIN_PARTICULAR_COMPLETO = 25.0;
    private static final double PRECIO_MIN_MAYORISTA_COMPLETO  = 200.0;
    private static final double PRECIO_MIN_FORWARDING          = 10.0;

    /** Redondea al 0.5 más cercano hacia arriba: 1.3 → 1.5, 2.1 → 2.5 */
    private static double redondearAlMedio(double valor) {
        return Math.ceil(valor * 2) / 2;
    }

    public CotizarResponse calcular(CotizarRequest req, double tipoCambio) {
        Categoria cat = Categoria.getById(req.categoriaId());
        if (cat == null) {
            return new CotizarResponse(false, null, null, "categoria_invalida");
        }
        if (cat.blacklist) {
            return new CotizarResponse(false, null, null, "categoria_manual");
        }
        if (req.precioUsd() <= 0) {
            return new CotizarResponse(false, null, null, "precio_invalido");
        }
        if (req.pesoKg() <= 0 || req.pesoKg() > 30) {
            return new CotizarResponse(false, null, null, "peso_invalido");
        }

        boolean forwarding = "forwarding".equals(req.tipoServicio());
        boolean mayorista  = "mayorista".equals(req.tipo());

        double precioMin = forwarding
                ? PRECIO_MIN_FORWARDING
                : (mayorista ? PRECIO_MIN_MAYORISTA_COMPLETO : PRECIO_MIN_PARTICULAR_COMPLETO);
        if (req.precioUsd() < precioMin) {
            return new CotizarResponse(false, null, null, "precio_minimo_" + precioMin);
        }

        double pesoFacturable     = redondearAlMedio(req.pesoKg());
        double costoFlete         = pesoFacturable * FLETE_POR_KG;
        double cif                = req.precioUsd() + costoFlete;

        double arancelImportacion = cif * cat.tasaArancel;
        double ivaImportacion     = (cif + arancelImportacion) * IVA_IMPORTACION;
        double tasaEstadistica    = cif * TASA_ESTADISTICA;

        double feeRatio = forwarding
                ? (mayorista ? FEE_MAYORISTA_FORWARDING  : FEE_PARTICULAR_FORWARDING)
                : (mayorista ? FEE_MAYORISTA_COMPLETO     : FEE_PARTICULAR_COMPLETO);
        double feeServicio = cif * feeRatio;

        double total = forwarding
                ? costoFlete + arancelImportacion + ivaImportacion + tasaEstadistica + feeServicio
                : cif        + arancelImportacion + ivaImportacion + tasaEstadistica + feeServicio;

        double totalArs = total * tipoCambio;

        CotizacionDesglose d = new CotizacionDesglose();
        d.precioProducto    = req.precioUsd();
        d.pesoFacturable    = pesoFacturable;
        d.costoFlete        = costoFlete;
        d.cif               = cif;
        d.arancelImportacion = arancelImportacion;
        d.ivaImportacion    = ivaImportacion;
        d.tasaEstadistica   = tasaEstadistica;
        d.feeServicio       = feeServicio;
        d.feeRatio          = feeRatio;
        d.total             = total;
        d.tipoCambio        = tipoCambio;
        d.totalArs          = totalArs;
        d.tipoImportacion   = req.tipo();
        d.tipoServicio      = req.tipoServicio();
        d.incluyeProducto   = !forwarding;
        d.alertaOrigenEuropa = "europa".equals(req.origen()) && req.precioUsd() > 100;

        return new CotizarResponse(true, null, d, null);
    }
}