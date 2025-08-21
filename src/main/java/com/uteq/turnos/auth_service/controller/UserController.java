package com.uteq.turnos.auth_service.controller;

import com.uteq.turnos.auth_service.dto.UserResponse;
import com.uteq.turnos.auth_service.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService users;
  public UserController(UserService users){ this.users = users; }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> list(){ return users.list(); }
}
