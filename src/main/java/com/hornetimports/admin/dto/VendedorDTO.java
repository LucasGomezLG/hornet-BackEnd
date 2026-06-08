package com.hornetimports.admin.dto;

import com.hornetimports.user.Profile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VendedorDTO(
        UUID id,
        String email,
        String nombre,
        long cantidadListings,
        OffsetDateTime createdAt
) {
    public static VendedorDTO from(Profile p, long cantidadListings) {
        return new VendedorDTO(p.getId(), p.getEmail(), p.getNombre(),
                cantidadListings, p.getCreatedAt());
    }
}
