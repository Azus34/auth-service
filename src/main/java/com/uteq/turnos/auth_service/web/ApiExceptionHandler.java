// src/main/java/com/uteq/turnos/auth_service/web/ApiExceptionHandler.java
package com.uteq.turnos.auth_service.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  private Map<String,Object> body(HttpStatus status, String message, String path) {
    return Map.of("timestamp", Instant.now().toString(),
                  "status", status.value(),
                  "error",  message,
                  "path",   path);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String,Object> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return body(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String,Object> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
    String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
    return body(HttpStatus.CONFLICT, msg, req.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String,Object> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg = ex.getBindingResult().getAllErrors().stream()
        .findFirst().map(e -> e.getDefaultMessage()).orElse("Datos inválidos");
    return body(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String,Object> generic(Exception ex, HttpServletRequest req) {
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", req.getRequestURI());
  }
}
