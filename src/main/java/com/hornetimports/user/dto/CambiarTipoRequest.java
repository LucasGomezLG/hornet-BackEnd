package com.hornetimports.user.dto;

import com.hornetimports.user.TipoCuenta;
import jakarta.validation.constraints.NotNull;

public record CambiarTipoRequest(@NotNull TipoCuenta tipo) {}