package com.hornetimports.user.dto;

import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(
        @Size(max = 100) String nombre,
        @Size(max = 100) String apellido,
        @Size(max = 30)  String telefono,
        @Size(max = 13)  String cuit
) {}