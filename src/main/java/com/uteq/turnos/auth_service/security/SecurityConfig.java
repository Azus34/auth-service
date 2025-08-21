package com.uteq.turnos.auth_service.security;

import com.uteq.turnos.auth_service.security.JwtAuthFilter;
import com.uteq.turnos.auth_service.security.SecurityErrorHandlers;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity // para @PreAuthorize en tus controllers
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final SecurityErrorHandlers errorHandlers; // si ya lo usas

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, SecurityErrorHandlers errorHandlers) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.errorHandlers = errorHandlers;
  }

  @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://turnos-frontend-l425d0pso-sss-projects-1be29493.vercel.app}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable()) // 🔴 clave: API JWT sin CSRF
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        // preflight
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        // público
        .requestMatchers("/auth/**", "/auth/keys/**", "/error", "/actuator/health").permitAll()
        // admin
        .requestMatchers("/admin/**").hasRole("ADMIN")
        // lo demás, autenticado
        .anyRequest().authenticated()
      )
      .exceptionHandling(eh -> eh
        .authenticationEntryPoint((req, res, ex) ->
          res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
        .accessDeniedHandler((req, res, ex) ->
          res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
      )
      .addFilterBefore(jwtAuthFilter,
          org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
    cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
    cfg.setAllowCredentials(true); // no es crítico si no usas cookies
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

 @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // coincide con $2b$12$ de tus hashes actuales
  }

}
