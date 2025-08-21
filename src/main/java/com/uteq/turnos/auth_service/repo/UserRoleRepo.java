// src/main/java/com/uteq/turnos/auth_service/repo/UserRoleRepo.java
package com.uteq.turnos.auth_service.repo;

import com.uteq.turnos.auth_service.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.transaction.Transactional;
import java.util.List;

public interface UserRoleRepo extends JpaRepository<UserRole, Long> {
  List<UserRole> findByUserId(Long userId);
  boolean existsByUserIdAndRole(Long userId, String role);
  @Transactional void deleteByUserId(Long userId);
}

