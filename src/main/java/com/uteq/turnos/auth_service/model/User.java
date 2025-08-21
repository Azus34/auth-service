package com.uteq.turnos.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "ux_users_email", columnList = "email", unique = true)
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 120)
  private String passwordHash;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(nullable = false)
  private Boolean activo = true;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn;

  @PrePersist
  public void onCreate() {
    if (creadoEn == null) creadoEn = LocalDateTime.now();
    if (activo == null) activo = true;
  }


}
