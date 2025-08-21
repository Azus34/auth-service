package com.uteq.turnos.auth_service.service;

import com.uteq.turnos.auth_service.dto.JwtResponse;
import com.uteq.turnos.auth_service.dto.LoginRequest;
import com.uteq.turnos.auth_service.dto.RefreshRequest;
import com.uteq.turnos.auth_service.dto.RegisterRequest;
import com.uteq.turnos.auth_service.model.User;
import com.uteq.turnos.auth_service.model.UserRole;
import com.uteq.turnos.auth_service.repo.UserRepo;
import com.uteq.turnos.auth_service.repo.UserRoleRepo;
import com.uteq.turnos.auth_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

  private final UserRepo userRepo;
  private final UserRoleRepo roleRepo;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwt;

  public AuthService(UserRepo userRepo,
                     UserRoleRepo roleRepo,
                     PasswordEncoder passwordEncoder,
                     JwtService jwt) {
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
    this.passwordEncoder = passwordEncoder;
    this.jwt = jwt;
  }

  public JwtResponse login(LoginRequest req) {
    // Usa el método que SÍ existe en el repo:
    User user = userRepo.findByEmailIgnoreCase(req.email())
        .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

    if (user.getActivo() == null || !user.getActivo()) {
      throw new DisabledException("Usuario inactivo");
    }

    if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      throw new BadCredentialsException("Credenciales incorrectas");
    }

    List<String> roles = roleRepo.findByUserId(user.getId())
        .stream().map(UserRole::getRole).toList();

    String access  = jwt.generateAccessToken(user.getId(), user.getEmail(), roles);
    String refresh = jwt.generateRefreshToken(user.getId(), user.getEmail(), roles);

    return new JwtResponse(access, refresh);
  }

  public JwtResponse refresh(RefreshRequest req) {
    String refreshToken = req.refreshToken();
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BadCredentialsException("Refresh token inválido");
    }

    Jws<Claims> jws = jwt.parse(refreshToken);
    Claims claims = jws.getBody();

    String sub = claims.getSubject();
    if (sub == null) throw new BadCredentialsException("Refresh inválido");

    Long userId;
    try { userId = Long.parseLong(sub); }
    catch (NumberFormatException e) { throw new BadCredentialsException("Refresh inválido"); }

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

    if (user.getActivo() == null || !user.getActivo()) {
      throw new DisabledException("Usuario inactivo");
    }

    List<String> roles = roleRepo.findByUserId(userId)
        .stream().map(UserRole::getRole).toList();

    String email = claims.get("email", String.class);
    if (email == null || email.isBlank()) email = user.getEmail();

    String newAccess  = jwt.generateAccessToken(user.getId(), email, roles);
    String newRefresh = jwt.generateRefreshToken(user.getId(), email, roles);

    return new JwtResponse(newAccess, newRefresh);
  }

  public User register(RegisterRequest req) {
    userRepo.findByEmailIgnoreCase(req.email()).ifPresent(u -> {
      throw new IllegalArgumentException("Email ya registrado");
    });

    User u = User.builder()
        .email(req.email().trim().toLowerCase())
        .nombre(req.nombre())
        .passwordHash(passwordEncoder.encode(req.password()))
        .activo(Boolean.TRUE)
        .creadoEn(LocalDateTime.now())
        .build();

    u = userRepo.save(u);

    if (req.roles() != null) {
      for (Object r : req.roles()) {
        String role = String.valueOf(r).trim().toUpperCase();
        if (!role.isEmpty()) {
          roleRepo.save(UserRole.builder().userId(u.getId()).role(role).build());
        }
      }
    } else {
      roleRepo.save(UserRole.builder().userId(u.getId()).role("ALUMNO").build());
    }

    return u;
  }

  public void logout(RefreshRequest req) {
    // opcional: blacklist de refresh tokens
  }
}
