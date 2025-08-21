// src/main/java/com/uteq/turnos/auth_service/security/JwtAuthFilter.java
package com.uteq.turnos.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  // Endpoints públicos (sin token)
  private static final String[] PUBLIC_PATHS = new String[]{
      "/auth/login",
      "/auth/register",
      "/auth/refresh",
      "/auth/keys/public",
      "/actuator/health"
  };

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    final String path = request.getRequestURI();
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // preflight
    for (String p : PUBLIC_PATHS) {
      if (PATH_MATCHER.match(p, path)) return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain) throws ServletException, IOException {

    try {
      String auth = request.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7).trim();
        if (!token.isEmpty()) {
          Jws<Claims> jws = jwtService.parse(token);
          Claims claims = jws.getBody();

          // (Opcional) valida que sea un access token
          Object typ = claims.get("typ");
          if (typ != null && !"access".equals(String.valueOf(typ))) {
            // Si no es access (por ejemplo refresh), no autenticar
            chain.doFilter(request, response);
            return;
          }

          String subject = claims.getSubject();         // userId como String
          String email   = claims.get("email", String.class);

          // roles puede venir como List o como String CSV
          Object rolesClaim = claims.get("roles");
          List<SimpleGrantedAuthority> authorities;

          if (rolesClaim instanceof String s) {
            authorities = Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
          } else if (rolesClaim instanceof Collection<?> coll) {
            authorities = coll.stream()
                .flatMap(o -> o == null ? Stream.empty() : Stream.of(o.toString()))
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
          } else {
            authorities = List.of();
          }

          // Autenticamos el contexto
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  // principal: email si viene, si no el subject (id)
                  (email != null && !email.isBlank()) ? email : subject,
                  null,
                  authorities
              );

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception ex) {
      // Token inválido o error de parseo → no autenticar, que siga la cadena
      // (No responder aquí; el manejador de excepciones se encarga si la ruta requiere auth)
    }

    chain.doFilter(request, response);
  }
}
