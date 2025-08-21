package com.uteq.turnos.auth_service.dto;

import com.uteq.turnos.auth_service.model.RoleName;

import java.util.List;
import java.util.Set;

public record UserResponse(
    Long id,
    String nombre,
    String email,
    List<String> roles,
    boolean activo
) {}
