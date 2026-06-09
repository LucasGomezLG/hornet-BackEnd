package com.hornetimports.solicitud.dto;

import com.hornetimports.solicitud.Solicitud;
import com.hornetimports.user.Profile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitudAdminDTO(
        UUID id,
        String estado,
        String notaCliente,
        String notaAdmin,
        OffsetDateTime expiresAt,
        List<SolicitudItemDTO> items,
        OffsetDateTime createdAt,
        // user info
        UUID userId,
        String userNombre,
        String userEmail,
        String userTelefono
) {
    public static SolicitudAdminDTO from(Solicitud s) {
        Profile u = s.getUser();
        String nombre = u.getNombre() != null
                ? (u.getNombre() + (u.getApellido() != null ? " " + u.getApellido() : ""))
                : u.getEmail();
        return new SolicitudAdminDTO(
                s.getId(), s.getEstado(),
                s.getNotaCliente(), s.getNotaAdmin(),
                s.getExpiresAt(),
                s.getItems().stream().map(SolicitudItemDTO::from).toList(),
                s.getCreatedAt(),
                u.getId(), nombre, u.getEmail(), u.getTelefono());
    }
}
