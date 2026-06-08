package com.hornetimports.user.dto;

import com.hornetimports.user.Profile;
import com.hornetimports.user.TipoCuenta;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PerfilResponse(
        UUID id,
        String email,
        String nombre,
        String apellido,
        String telefono,
        String cuit,
        TipoCuenta tipo,
        OffsetDateTime createdAt
) {
    public static PerfilResponse from(Profile p) {
        return new PerfilResponse(
                p.getId(),
                p.getEmail(),
                p.getNombre(),
                p.getApellido(),
                p.getTelefono(),
                p.getCuit(),
                p.getTipo(),
                p.getCreatedAt()
        );
    }
}