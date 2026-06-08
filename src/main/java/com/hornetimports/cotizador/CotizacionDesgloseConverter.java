package com.hornetimports.cotizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hornetimports.cotizador.dto.CotizacionDesglose;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CotizacionDesgloseConverter implements AttributeConverter<CotizacionDesglose, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CotizacionDesglose desglose) {
        try {
            return desglose == null ? "{}" : mapper.writeValueAsString(desglose);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public CotizacionDesglose convertToEntityAttribute(String json) {
        try {
            return (json == null || json.isBlank()) ? new CotizacionDesglose()
                    : mapper.readValue(json, CotizacionDesglose.class);
        } catch (Exception e) {
            return new CotizacionDesglose();
        }
    }
}