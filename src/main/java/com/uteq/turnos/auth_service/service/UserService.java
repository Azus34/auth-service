package com.uteq.turnos.auth_service.service;

import com.uteq.turnos.auth_service.dto.UserResponse;
import com.uteq.turnos.auth_service.model.UserRole;
import com.uteq.turnos.auth_service.repo.UserRepo;
import com.uteq.turnos.auth_service.repo.UserRoleRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
  private final UserRepo users;
  private final UserRoleRepo roles;

  public UserService(UserRepo users, UserRoleRepo roles) {
    this.users = users;
    this.roles = roles;
  }

  public List<UserResponse> list() {
    return users.findAll().stream()
        .map(u -> {
          List<String> rs = roles.findByUserId(u.getId()).stream()
              .map(UserRole::getRole).toList();
          return new UserResponse(
              u.getId(),
              u.getNombre(),
              u.getEmail(),
              rs,
              Boolean.TRUE.equals(u.getActivo())
          );
        })
        .toList();
  }
}