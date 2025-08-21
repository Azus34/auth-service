package com.uteq.turnos.auth_service.dto;

public record JwtResponse(String accessToken, String refreshToken, String tokenType) {
  public JwtResponse(String accessToken, String refreshToken) { this(accessToken, refreshToken, "Bearer"); }
}
