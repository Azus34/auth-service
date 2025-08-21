// src/main/java/com/uteq/turnos/auth_service/security/JwtService.java
package com.uteq.turnos.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

  private final KeyLoader keyLoader;

  @Value("${security.jwt.issuer:auth-service}")
  private String issuer;

  @Value("${security.jwt.access-token-minutes:30}")
  private long accessMinutes;

  @Value("${security.jwt.refresh-token-days:7}")
  private long refreshDays;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  public JwtService(KeyLoader keyLoader) {
    this.keyLoader = keyLoader;
  }

  @PostConstruct
  public void init() {
    try {
      this.privateKey = keyLoader.loadPrivateKey();
      this.publicKey  = keyLoader.loadPublicKey();
    } catch (Exception e) {
      throw new IllegalStateException("No se pudieron cargar las llaves JWT", e);
    }
  }

  public String generateAccessToken(Long userId, String email, List<String> roles) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuer(issuer)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessMinutes * 60)))
        .claim("email", email)
        // 👉 Enviamos roles como LISTA (jjwt serializa a JSON array)
        .claim("roles", roles)
        .claim("typ", "access")
        // jjwt 0.11.x
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  public String generateRefreshToken(Long userId, String email, List<String> roles) {
    Instant now = Instant.now();
    long seconds = refreshDays * 24 * 60 * 60;
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuer(issuer)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(seconds)))
        .claim("email", email)
        .claim("roles", roles) // idem lista
        .claim("typ", "refresh")
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    // jjwt 0.11.x
    return Jwts.parserBuilder()
        .requireIssuer(issuer)
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token);
  }
}
