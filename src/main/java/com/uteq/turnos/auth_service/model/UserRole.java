package com.uteq.turnos.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_user_roles_user_role", columnNames = {"user_id","role"})
    },
    indexes = {
        @Index(name = "ix_user_roles_user_id", columnList = "user_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String role; // Ej: ADMIN, DOCENTE, ALUMNO, KIOSCO, MONITOR
}
