package com.uteq.turnos.auth_service.controller;

import com.uteq.turnos.auth_service.dto.*;
import com.uteq.turnos.auth_service.model.User;
import com.uteq.turnos.auth_service.security.KeyLoader;
import com.uteq.turnos.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService auth;
  private final KeyLoader keys;
  public AuthController(AuthService auth, KeyLoader keys){ this.auth = auth; this.keys = keys; }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody @Valid RegisterRequest req){
    return ResponseEntity.ok(auth.register(req));
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req){
    return ResponseEntity.ok(auth.login(req));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refresh(@RequestBody @Valid RefreshRequest req){
    return ResponseEntity.ok(auth.refresh(req));
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String,String>> logout(@RequestBody @Valid RefreshRequest req){
    auth.logout(req);
    return ResponseEntity.ok(Map.of("status","ok"));
  }

  // Para otros microservicios: recuperar la clave pública en PEM
  @GetMapping(value = "/keys/public", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> publicKeyPem(){
    try {
      PublicKey pk = keys.loadPublicKey();
      String base64 = java.util.Base64.getEncoder().encodeToString(pk.getEncoded());
      String pem = "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n";
      return ResponseEntity.ok(pem);
    } catch (Exception e){
      return ResponseEntity.internalServerError().body("Error");
    }
  }
}
