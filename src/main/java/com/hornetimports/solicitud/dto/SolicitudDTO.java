package com.hornetimports.solicitud.dto;

import com.hornetimports.solicitud.Solicitud;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitudDTO(
        UUID id,
        String estado,
        String notaCliente,
        String notaAdmin,
        OffsetDateTime expiresAt,
        List<SolicitudItemDTO> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SolicitudDTO from(Solicitud s) {
        return new SolicitudDTO(
                s.getId(), s.getEstado(),
                s.getNotaCliente(), s.getNotaAdmin(),
                s.getExpiresAt(),
                s.getItems().stream().map(SolicitudItemDTO::from).toList(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
