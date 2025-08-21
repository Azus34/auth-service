package com.uteq.turnos.auth_service.config;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Mapea excepciones comunes a códigos HTTP y mensajes claros para el frontend.
 */
@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<?> onBadCred(BadCredentialsException ex) {
    // Credenciales incorrectas → 401
    return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<?> onDisabled(DisabledException ex) {
    // Usuario inactivo → 403
    return ResponseEntity.status(403).body(Map.of("error", "Usuario inactivo"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> onDenied(AccessDeniedException ex) {
    // Autenticado pero sin permisos → 403
    return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado"));
  }
}
