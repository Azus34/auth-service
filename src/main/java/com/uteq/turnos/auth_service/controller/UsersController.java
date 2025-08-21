// auth_service: src/main/java/com/uteq/turnos/auth_service/controller/UsersController.java
package com.uteq.turnos.auth_service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {

  private final JdbcTemplate jdbc;

  public UsersController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getUser(@PathVariable Long id) {
    var row = jdbc.queryForList(
      "SELECT id, nombre, email, activo FROM users WHERE id = ?",
      id
    );
    if (row.isEmpty()) return ResponseEntity.notFound().build();
    Map<String, Object> r = row.get(0);
    return ResponseEntity.ok(Map.of(
      "id", ((Number)r.get("id")).longValue(),
      "nombre", (String) r.get("nombre"),
      "email", (String) r.get("email"),
      "activo", ((Number)r.get("activo")).intValue() == 1
    ));
  }
}
