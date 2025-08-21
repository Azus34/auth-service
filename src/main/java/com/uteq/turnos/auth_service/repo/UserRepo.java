package com.uteq.turnos.auth_service.repo;

import com.uteq.turnos.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);

  // Úsalos donde prefieras (uno u otro). En el service abajo uso el *IgnoreCase*.
  Optional<User> findByEmail(String email);
  Optional<User> findByEmailIgnoreCase(String email);
}
