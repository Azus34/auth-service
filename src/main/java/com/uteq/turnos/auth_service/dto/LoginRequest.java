package com.uteq.turnos.auth_service.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
  @Email @NotBlank String email,
  @NotBlank String password
) {}
