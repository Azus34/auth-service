package com.uteq.turnos.auth_service.controller;

import com.uteq.turnos.auth_service.dto.AdminCreateUserRequest;
import com.uteq.turnos.auth_service.model.User;
import com.uteq.turnos.auth_service.model.UserRole;
import com.uteq.turnos.auth_service.repo.UserRepo;
import com.uteq.turnos.auth_service.repo.UserRoleRepo;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserRepo users;
  private final UserRoleRepo roles;
  private final PasswordEncoder enc;

  public AdminUserController(UserRepo users, UserRoleRepo roles, PasswordEncoder enc) {
    this.users = users;
    this.roles = roles;
    this.enc   = enc;
  }

  @GetMapping(produces = "application/json")
  public List<User> list() {
    return users.findAll();
  }

  @PostMapping(consumes = "application/json", produces = "application/json")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public User create(@Valid @RequestBody AdminCreateUserRequest req) {
    final String email = req.email().trim().toLowerCase();
    if (users.existsByEmail(email)) {
      throw new IllegalArgumentException("Email ya registrado");
    }

    User u = User.builder()
        .nombre(req.nombre().trim())
        .email(email)
        .passwordHash(enc.encode(req.password()))
        .activo(req.activo() != null ? req.activo() : Boolean.TRUE)
        .creadoEn(LocalDateTime.now())
        .build();

    try {
      u = users.save(u);
    } catch (DataIntegrityViolationException e) {
      throw new DataIntegrityViolationException("Email duplicado", e);
    }

    Collection<String> raw = req.roles();
    if (raw != null) {
      for (String r : raw) {
        if (r == null) continue;
        String role = r.trim().toUpperCase();
        if (!role.isEmpty() && !roles.existsByUserIdAndRole(u.getId(), role)) {
          roles.save(UserRole.builder().userId(u.getId()).role(role).build());
        }
      }
    }

    return u;
  }

  @PutMapping(value="/{id}", consumes="application/json", produces="application/json")
  public User update(@PathVariable Long id, @RequestBody Map<String,Object> patch) {
    User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("No existe"));

    if (patch.containsKey("nombre")) u.setNombre(String.valueOf(patch.get("nombre")));
    if (patch.containsKey("email")) {
      String newEmail = String.valueOf(patch.get("email")).trim().toLowerCase();
      if (!newEmail.equals(u.getEmail()) && users.existsByEmail(newEmail))
        throw new IllegalArgumentException("Email ya registrado");
      u.setEmail(newEmail);
    }
    if (patch.containsKey("activo")) u.setActivo(Boolean.parseBoolean(String.valueOf(patch.get("activo"))));

    // Manejo de roles (sumar o reemplazar si viene replaceRoles=true)
    if (patch.containsKey("roles")) {
      @SuppressWarnings("unchecked")
      List<Object> raw = (List<Object>) patch.get("roles");
      Set<String> incoming = raw == null ? Set.of() :
          raw.stream().filter(Objects::nonNull)
              .map(String::valueOf).map(s -> s.trim().toUpperCase())
              .filter(s -> !s.isEmpty()).collect(Collectors.toSet());

      boolean replace = Boolean.parseBoolean(String.valueOf(patch.getOrDefault("replaceRoles", false)));

      if (replace) {
        roles.deleteByUserId(id);
        for (String r : incoming) {
          roles.save(UserRole.builder().userId(id).role(r).build());
        }
      } else {
        Set<String> existing = roles.findByUserId(id).stream()
            .map(UserRole::getRole).collect(Collectors.toSet());
        for (String r : incoming) {
          if (!existing.contains(r)) {
            roles.save(UserRole.builder().userId(id).role(r).build());
          }
        }
      }
    }

    try {
      return users.save(u);
    } catch (DataIntegrityViolationException dive) {
      throw new DataIntegrityViolationException("Email duplicado", dive);
    }
  }

  /** Buscar usuario por email (útil para enlazar desde el frontend) */
  @GetMapping(value="/by-email", produces="application/json")
  public User findByEmail(@RequestParam String email) {
    return users.findByEmailIgnoreCase(email.trim().toLowerCase())
        .orElseThrow(() -> new IllegalArgumentException("No existe usuario con ese email"));
  }

  /** Usuarios que tengan un rol (filtrado en memoria para evitar método repo inexistente). */
  @GetMapping(value="/by-role", produces="application/json")
  public List<User> byRole(@RequestParam String role) {
    String r = role.trim().toUpperCase();
    return roles.findAll().stream()
        .filter(ur -> r.equalsIgnoreCase(ur.getRole()))
        .map(ur -> users.findById(ur.getUserId()).orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }

  @DeleteMapping("/{id}")
  @Transactional
  public void delete(@PathVariable Long id) {
    roles.deleteByUserId(id); // primero roles
    users.deleteById(id);     // luego usuario
  }
}
