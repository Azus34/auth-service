// src/main/java/com/uteq/turnos/auth_service/dto/AdminCreateUserRequest.java
package com.uteq.turnos.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminCreateUserRequest(
    @NotBlank String nombre,
    @NotBlank @Email String email,
    @NotBlank @Size(min=6) String password,
    List<String> roles,          // p.ej. ["ADMIN"]
    Boolean activo               // opcional
) {}

