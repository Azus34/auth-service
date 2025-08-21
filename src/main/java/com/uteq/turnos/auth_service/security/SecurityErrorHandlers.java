package com.uteq.turnos.auth_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Configuration
public class SecurityErrorHandlers {

  private static Map<String,Object> base(HttpServletRequest req, int status, String error, String message){
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("timestamp", Instant.now().toString());
    m.put("status", status);
    m.put("error", error);
    m.put("message", message);
    m.put("path", req.getRequestURI());
    m.put("method", req.getMethod());
    return m;
  }

  @Bean
  public AuthenticationEntryPoint restAuthEntryPoint(ObjectMapper om) {
    return (request, response, authException) -> {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      var body = base(request, 401, "UNAUTHORIZED",
          Optional.ofNullable(authException.getMessage()).orElse("No autenticado"));

      om.writeValue(response.getOutputStream(), body);
    };
  }

  @Bean
  public AccessDeniedHandler restAccessDeniedHandler(ObjectMapper om) {
    return (request, response, ex) -> {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      var body = base(request, 403, "FORBIDDEN",
          Optional.ofNullable(ex.getMessage()).orElse("Acceso denegado"));

      Authentication auth = org.springframework.security.core.context.SecurityContextHolder
          .getContext().getAuthentication();

      if (auth != null) {
        body.put("user", auth.getName());
        List<String> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).toList();
        body.put("roles", roles);
      } else {
        body.put("user", null);
        body.put("roles", List.of());
      }

      // (opcional) alguna pista
      body.put("hint", "Verifica el rol requerido para este endpoint");

      om.writeValue(response.getOutputStream(), body);
    };
  }
}
