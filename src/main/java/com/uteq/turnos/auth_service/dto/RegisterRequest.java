package com.uteq.turnos.auth_service.dto;

import com.uteq.turnos.auth_service.model.RoleName;
import jakarta.validation.constraints.*;
import java.util.Set;

public record RegisterRequest(
  @NotBlank String nombre,
  @Email @NotBlank String email,
  @Size(min = 8) String password,
  @NotEmpty Set<RoleName> roles
) {}